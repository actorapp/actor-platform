package im.actor.server.user

import java.time.{ LocalDateTime, ZoneOffset }
import java.util.TimeZone

import akka.actor.{ ActorSystem, Status }
import akka.pattern.pipe
import im.actor.api.rpc.contacts.{ UpdateContactRegistered, UpdateContactsAdded }
import im.actor.api.rpc.messaging._
import im.actor.api.rpc.misc.ApiExtension
import im.actor.api.rpc.peers.{ ApiPeer, ApiPeerType }
import im.actor.api.rpc.users._
import im.actor.concurrent.FutureExt
import im.actor.config.ActorConfig
import im.actor.server.ApiConversions._
import im.actor.server.acl.ACLUtils
import im.actor.server.event.TSEvent
import im.actor.server.file.{ Avatar, ImageUtils }
import im.actor.server.models.contact.{ UserContact, UserEmailContact, UserPhoneContact }
import im.actor.server.persist.contact.{ UserContactRepo, UserEmailContactRepo, UserPhoneContactRepo }
import im.actor.server.persist.{ AuthSessionRepo, UserRepo }
import im.actor.server.sequence.{ SeqUpdatesManager, SequenceErrors }
import im.actor.server.social.SocialManager._
import im.actor.server.user.UserCommands._
import im.actor.server.{ models, persist ⇒ p }
import org.joda.time.DateTime
import slick.driver.PostgresDriver.api._

import scala.concurrent.Future
import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.util.Failure
import scala.util.control.NoStackTrace

abstract class UserError(message: String) extends RuntimeException(message) with NoStackTrace

object UserErrors {

  case object NicknameTaken extends UserError("Nickname taken")

  final case class InvalidTimeZone(tz: String) extends UserError(s"Invalid time zone: $tz")

  final case class InvalidLocale(locale: String) extends UserError(s"Invalid locale: $locale")

  case object EmptyLocalesList extends UserError("Empty locale list")

}

private object ServiceMessages {
  def contactRegistered(userId: Int, name: String)(implicit system: ActorSystem) = {
    val systemName = ActorConfig.systemName
    ApiServiceMessage(s"$name joined $systemName", Some(ApiServiceExContactRegistered(userId)))
  }
}

private[user] trait UserCommandHandlers {
  this: UserProcessor ⇒

  import ImageUtils._

  protected def create(
    accessSalt:  String,
    nickname:    Option[String],
    name:        String,
    countryCode: String,
    sex:         ApiSex.ApiSex,
    isBot:       Boolean,
    isAdmin:     Boolean,
    extensions:  Seq[ApiExtension],
    external:    Option[String]
  ): Unit = {
    log.debug("Creating user {} {}", userId, name)

    val replyTo = sender()

    onSuccess(checkNicknameExists(nickname)) { exists ⇒
      if (!exists) {
        val ts = now()
        val e = UserEvents.Created(userId, accessSalt, nickname, name, countryCode, sex, isBot, extensions, external, isAdmin = Some(isAdmin))
        val createEvent = TSEvent(ts, e)
        val user = UserBuilder(ts, e)

        persistStashingReply(createEvent, user, replyTo) { evt ⇒
          val user = models.User(
            id = userId,
            accessSalt = accessSalt,
            nickname = nickname,
            name = name,
            countryCode = countryCode,
            sex = models.Sex.fromInt(sex.id),
            state = models.UserState.Registered,
            createdAt = LocalDateTime.now(ZoneOffset.UTC),
            external = external,
            isBot = isBot
          )
          db.run(for (_ ← p.UserRepo.create(user)) yield CreateAck())
        }
      } else {
        replyTo ! Status.Failure(UserErrors.NicknameTaken)
      }
    }
  }

  protected def updateIsAdmin(state: User, isAdmin: Option[Boolean]): Unit = {
    persist(TSEvent(now(), UserEvents.IsAdminUpdated(isAdmin))) { e ⇒
      context become working(updatedState(e, state))

      sender() ! UpdateIsAdminAck()
    }
  }

  protected def addAuth(user: User, authId: Long): Unit = {
    persistStashingReply(TSEvent(now(), UserEvents.AuthAdded(authId)), user) { _ ⇒
      db.run(AuthSessionRepo.findByAuthId(authId)) foreach {
        case Some(authSession) ⇒
          userExt.hooks.afterAuth.runAll(user.id, authSession.appId, authSession.deviceTitle)
        case None ⇒ log.error("AuthSession was not found")
      }

      db.run(p.AuthIdRepo.setUserData(authId, user.id)) map (_ ⇒ NewAuthAck())
    }
  }

  protected def removeAuth(user: User, authId: Long): Unit =
    persistStashingReply(TSEvent(now(), UserEvents.AuthRemoved(authId)), user) { _ ⇒
      db.run(p.AuthIdRepo.delete(authId) map (_ ⇒ RemoveAuthAck()))
    }

  protected def changeCountryCode(user: User, countryCode: String): Unit =
    persistReply(TSEvent(now(), UserEvents.CountryCodeChanged(countryCode)), user) { e ⇒
      db.run(p.UserRepo.setCountryCode(userId, countryCode) map (_ ⇒ ChangeCountryCodeAck()))
    }

  protected def changeName(user: User, name: String, clientAuthId: Long): Unit =
    persistReply(TSEvent(now(), UserEvents.NameChanged(name)), user) { _ ⇒
      val update = UpdateUserNameChanged(userId, name)
      for {
        relatedUserIds ← getRelations(userId)
        _ ← userExt.broadcastUsersUpdate(relatedUserIds, update, pushText = None, isFat = false, deliveryId = None)
        _ ← SeqUpdatesManager.persistAndPushUpdates(user.authIds.filterNot(_ == clientAuthId).toSet, update, pushText = None, isFat = false, deliveryId = None)
        seqstate ← SeqUpdatesManager.persistAndPushUpdate(clientAuthId, update, pushText = None, isFat = false)
      } yield seqstate
    }

  protected def delete(user: User): Unit =
    persistStashingReply(TSEvent(now(), UserEvents.Deleted()), user) { _ ⇒
      db.run(p.UserRepo.setDeletedAt(userId) map (_ ⇒ DeleteAck()))
    }

  protected def addPhone(user: User, phone: Long): Unit =
    persistReply(TSEvent(now(), UserEvents.PhoneAdded(phone)), user) { _ ⇒
      val rng = ThreadLocalRandom.current()
      db.run(for {
        _ ← p.UserPhoneRepo.create(rng.nextInt(), userId, ACLUtils.nextAccessSalt(rng), phone, "Mobile phone")
        _ ← DBIO.from(markContactRegistered(user, phone, false))
      } yield {
        AddPhoneAck()
      }) andThen {
        case Failure(e) ⇒ log.error(e, "Failed to add phone")
      }
    }

  protected def addEmail(user: User, email: String): Unit =
    persistReply(TSEvent(now(), UserEvents.EmailAdded(email)), user) { event ⇒
      val rng = ThreadLocalRandom.current()
      db.run(for {
        _ ← p.UserEmailRepo.create(rng.nextInt(), userId, ACLUtils.nextAccessSalt(rng), email, "Email")
        _ ← DBIO.from(markContactRegistered(user, email, false))
      } yield {
        AddEmailAck()
      }) andThen {
        case Failure(e) ⇒ log.error(e, "Failed to add email")
      }
    }

  protected def addSocialContact(user: User, contact: SocialContact): Unit =
    persistReply(TSEvent(now(), UserEvents.SocialContactAdded(contact)), user) { _ ⇒
      Future.successful(AddSocialContactAck())
    }

  protected def changeNickname(user: User, clientAuthId: Long, nicknameOpt: Option[String]): Unit = {
    val replyTo = sender()

    onSuccess(checkNicknameExists(nicknameOpt)) { exists ⇒
      if (!exists) {
        persistReply(TSEvent(now(), UserEvents.NicknameChanged(nicknameOpt)), user, replyTo) { _ ⇒
          val update = UpdateUserNickChanged(userId, nicknameOpt)

          for {
            _ ← db.run(p.UserRepo.setNickname(userId, nicknameOpt))
            relatedUserIds ← getRelations(userId)
            (seqstate, _) ← userExt.broadcastClientAndUsersUpdate(userId, clientAuthId, relatedUserIds, update, None, isFat = false, deliveryId = None)
          } yield seqstate
        }
      } else {
        replyTo ! Status.Failure(UserErrors.NicknameTaken)
      }
    }
  }

  protected def changeAbout(user: User, clientAuthId: Long, about: Option[String]): Unit = {
    persistReply(TSEvent(now(), UserEvents.AboutChanged(about)), user) { _ ⇒
      val update = UpdateUserAboutChanged(userId, about)
      for {
        _ ← db.run(p.UserRepo.setAbout(userId, about))
        relatedUserIds ← getRelations(userId)
        (seqstate, _) ← userExt.broadcastClientAndUsersUpdate(userId, clientAuthId, relatedUserIds, update, None, isFat = false, deliveryId = None)
      } yield seqstate
    }
  }

  protected def changeTimeZone(user: User, authId: Long, timeZone: String): Unit = {
    def validTimeZone(tz: String): Boolean = TimeZone.getAvailableIDs.contains(tz)

    if (validTimeZone(timeZone)) {
      if (!user.timeZone.contains(timeZone)) {
        persistReply(TSEvent(now(), UserEvents.TimeZoneChanged(Some(timeZone))), user) { _ ⇒
          val update = UpdateUserTimeZoneChanged(user.id, Some(timeZone))
          for {
            relatedUserIds ← getRelations(user.id)
            (seqstate, _) ← userExt.broadcastClientAndUsersUpdate(user.id, authId, relatedUserIds, update, pushText = None, isFat = false, deliveryId = None)
          } yield seqstate
        }
      } else sender() ! Status.Failure(SequenceErrors.UpdateAlreadyApplied(UserFields.TimeZone))
    } else {
      val e = UserErrors.InvalidTimeZone(timeZone)
      if (timeZone.nonEmpty)
        log.error(e, "Invalid time zone")
      sender() ! Status.Failure(e)
    }
  }

  protected def changePreferredLanguages(user: User, authId: Long, preferredLanguages: Seq[String]): Unit = {
    def validLocale(l: String): Boolean = l matches "^[a-z]{2}(?:-[A-Z]{2})?$"

    preferredLanguages.find(l ⇒ !validLocale(l)) match {
      case Some(invalid) ⇒
        val e = UserErrors.InvalidLocale(invalid)
        log.error(e, "Invalid preferred language")
        sender() ! Status.Failure(e)
      case None ⇒
        preferredLanguages match {
          case Nil ⇒ sender() ! Status.Failure(UserErrors.EmptyLocalesList)
          case pl if pl == user.preferredLanguages ⇒
            sender() ! Status.Failure(SequenceErrors.UpdateAlreadyApplied(UserFields.PreferredLanguages))
          case _ ⇒
            persistReply(TSEvent(now(), UserEvents.PreferredLanguagesChanged(preferredLanguages)), user) { _ ⇒
              val update = UpdateUserPreferredLanguagesChanged(user.id, preferredLanguages.toVector)
              for {
                relatedUserIds ← getRelations(user.id)
                (seqstate, _) ← userExt.broadcastClientAndUsersUpdate(user.id, authId, relatedUserIds, update, pushText = None, isFat = false, deliveryId = None)
              } yield seqstate
            }
        }
    }
  }

  protected def updateAvatar(user: User, clientAuthId: Long, avatarOpt: Option[Avatar]): Unit = {
    persistReply(TSEvent(now(), UserEvents.AvatarUpdated(avatarOpt)), user) { evt ⇒
      val avatarData = avatarOpt map (getAvatarData(models.AvatarData.OfUser, user.id, _)) getOrElse models.AvatarData.empty(models.AvatarData.OfUser, user.id.toLong)

      val update = UpdateUserAvatarChanged(user.id, avatarOpt)

      val relationsF = getRelations(user.id)

      for {
        _ ← db.run(p.AvatarDataRepo.createOrUpdate(avatarData))
        relatedUserIds ← relationsF
        (seqstate, _) ← userExt.broadcastClientAndUsersUpdate(user.id, clientAuthId, relatedUserIds, update, None, isFat = false, deliveryId = None)
      } yield UpdateAvatarAck(avatarOpt, seqstate)
    }
  }

  protected def notifyDialogsChanged(user: User, clientAuthId: Long): Unit = {
    (for {
      shortDialogs ← dialogExt.getGroupedDialogs(user.id)
      seqstate ← userExt.broadcastClientUpdate(user.id, clientAuthId, UpdateChatGroupsChanged(shortDialogs), pushText = None, isFat = false, deliveryId = None)
    } yield seqstate) pipeTo sender()
  }

  protected def addContacts(
    user:          User,
    clientAuthId:  Long,
    contactsToAdd: Seq[UserCommands.ContactToAdd]
  ): Unit = {
    val (idsLocalNames, plains, phones, emails) = contactsToAdd.view.map {
      case UserCommands.ContactToAdd(contactUserId, localNameOpt, phoneOpt, emailOpt) ⇒
        val phone = phoneOpt map (UserPhoneContact(_, user.id, contactUserId, localNameOpt, isDeleted = false))
        val email = emailOpt map (UserEmailContact(_, user.id, contactUserId, localNameOpt, isDeleted = false))
        val plain =
          if (phone.isDefined || email.isDefined)
            None
          else Some(UserContact(user.id, contactUserId, localNameOpt, isDeleted = false))

        ((contactUserId, localNameOpt), plain, phone, email)
    }.foldLeft(Map.empty[Int, Option[String]], Seq.empty[UserContact], Seq.empty[UserPhoneContact], Seq.empty[UserEmailContact]) {
      case ((idsLocalNames, plains, phones, emails), (idLocalName, plain, phone, email)) ⇒
        (
          idsLocalNames + idLocalName,
          plain.map(plains :+ _).getOrElse(plains),
          phone.map(phones :+ _).getOrElse(phones),
          email.map(emails :+ _).getOrElse(emails)
        )
    }

    (for {
      _ ← FutureExt.ftraverse(plains)(c ⇒ db.run(UserContactRepo.insertOrUpdate(c)))
      _ ← FutureExt.ftraverse(phones)(c ⇒ db.run(UserPhoneContactRepo.insertOrUpdate(c)))
      _ ← FutureExt.ftraverse(emails)(c ⇒ db.run(UserEmailContactRepo.insertOrUpdate(c)))
      _ ← FutureExt.ftraverse(idsLocalNames.toSeq) {
        case (contactUserId, localName) ⇒
          contacts.editLocalName(clientAuthId, contactUserId, localName, supressUpdate = true)
      }
      update = UpdateContactsAdded(idsLocalNames.keys.toVector)
      seqstate ← userExt.broadcastClientUpdate(user.id, clientAuthId, update, pushText = None, isFat = true, deliveryId = None)
    } yield seqstate) pipeTo sender()
  }

  private def getName(userId: Int, localNameOpt: Option[String]): Future[String] =
    localNameOpt match {
      case Some(localName) ⇒ Future.successful(localName)
      case None ⇒
        db.run(UserRepo.findName(userId)) map (_.getOrElse(throw new RuntimeException(s"User $userId not found")))
    }

  private def checkNicknameExists(nicknameOpt: Option[String]): Future[Boolean] = {
    nicknameOpt match {
      case Some(nickname) ⇒ db.run(p.UserRepo.nicknameExists(nickname))
      case None           ⇒ Future.successful(false)
    }
  }

  // TODO: DRY it, finally!
  private def markContactRegistered(user: User, phoneNumber: Long, isSilent: Boolean): Future[Unit] = {
    val date = new DateTime
    for {
      contacts ← db.run(p.contact.UnregisteredPhoneContactRepo.find(phoneNumber))
      _ = log.debug(s"Unregistered $phoneNumber is in contacts of users: $contacts")
      _ ← Future.sequence(contacts map { contact ⇒
        val randomId = ThreadLocalRandom.current().nextLong()
        val updateContactRegistered = UpdateContactRegistered(user.id, isSilent, date.getMillis, randomId)
        val updateContactsAdded = UpdateContactsAdded(Vector(user.id))
        val localName = contact.name
        val serviceMessage = ServiceMessages.contactRegistered(user.id, localName.getOrElse(user.name))
        for {
          _ ← userExt.addContact(contact.ownerUserId, 0, user.id, localName, Some(phoneNumber), None)
          _ ← userExt.broadcastUserUpdate(contact.ownerUserId, updateContactRegistered, Some(s"${localName.getOrElse(user.name)} registered"), isFat = true, deliveryId = None)
          _ ← userExt.broadcastUserUpdate(contact.ownerUserId, updateContactsAdded, None, isFat = false, deliveryId = None)
          _ ← dialogExt.writeMessage(
            ApiPeer(ApiPeerType.Private, contact.ownerUserId),
            user.id,
            date,
            randomId,
            serviceMessage
          )
        } yield {
          recordRelation(user.id, contact.ownerUserId)
        }
      })
      _ ← db.run(p.contact.UnregisteredPhoneContactRepo.deleteAll(phoneNumber))
    } yield ()
  }

  private def markContactRegistered(user: User, email: String, isSilent: Boolean): Future[Unit] = {
    val date = new DateTime
    for {
      _ ← userExt.hooks.beforeEmailContactRegistered.runAll(user.id, email)
      contacts ← db.run(p.contact.UnregisteredEmailContactRepo.find(email))
      _ = log.debug(s"Unregistered $email is in contacts of users: $contacts")
      _ ← Future.sequence(contacts.map { contact ⇒
        val randomId = ThreadLocalRandom.current().nextLong()
        val updateContactRegistered = UpdateContactRegistered(user.id, isSilent, date.getMillis, randomId)
        val updateContactsAdded = UpdateContactsAdded(Vector(user.id))
        val localName = contact.name
        val serviceMessage = ServiceMessages.contactRegistered(user.id, localName.getOrElse(user.name))
        for {
          _ ← userExt.addContact(contact.ownerUserId, 0, user.id, localName, None, Some(email))
          _ ← userExt.broadcastUserUpdate(contact.ownerUserId, updateContactRegistered, Some(serviceMessage.text), isFat = true, deliveryId = None)
          _ ← userExt.broadcastUserUpdate(contact.ownerUserId, updateContactsAdded, None, isFat = false, deliveryId = None)
          _ ← dialogExt.writeMessage(
            ApiPeer(ApiPeerType.Private, contact.ownerUserId),
            user.id,
            date,
            randomId,
            serviceMessage
          )
        } yield recordRelation(user.id, contact.ownerUserId)
      })
      _ ← db.run(p.contact.UnregisteredEmailContactRepo.deleteAll(email))
    } yield ()
  }
}
