package im.actor.server.user

import java.time.{ LocalDateTime, ZoneOffset }

import akka.actor.{ ActorSystem, Status }
import akka.pattern.pipe
import im.actor.api.rpc.contacts.{ UpdateContactRegistered, UpdateContactsAdded }
import im.actor.api.rpc.messaging._
import im.actor.api.rpc.misc.ApiExtension
import im.actor.api.rpc.users._
import im.actor.concurrent.FutureExt
import im.actor.config.ActorConfig
import im.actor.server.ApiConversions._
import im.actor.server.acl.ACLUtils
import im.actor.server.event.TSEvent
import im.actor.server.file.{ Avatar, ImageUtils }
import im.actor.server.history.HistoryUtils
import im.actor.server.models.contact.{ UserContact, UserEmailContact, UserPhoneContact }
import im.actor.server.persist.UserRepo
import im.actor.server.persist.contact.{ UserContactRepo, UserEmailContactRepo, UserPhoneContactRepo }
import im.actor.server.sequence.SeqUpdatesManager
import im.actor.server.social.SocialManager._
import im.actor.server.user.UserCommands._
import im.actor.server.{ ApiConversions, models, persist ⇒ p }
import org.joda.time.DateTime
import slick.driver.PostgresDriver.api._

import scala.concurrent.Future
import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.util.control.NoStackTrace

sealed trait UserException extends RuntimeException

object UserExceptions {

  final case object NicknameTaken extends RuntimeException with NoStackTrace

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
        replyTo ! Status.Failure(UserExceptions.NicknameTaken)
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
      db.run(p.AuthIdRepo.setUserData(authId, user.id)) map (_ ⇒ NewAuthAck())
    }
  }

  protected def removeAuth(user: User, authId: Long): Unit =
    persistStashingReply(TSEvent(now(), UserEvents.AuthRemoved(authId)), user) { _ ⇒
      db.run(p.AuthIdRepo.delete(authId) map (_ ⇒ RemoveAuthAck()))
    }

  protected def changeCountryCode(user: User, countryCode: String): Unit =
    persistReply(TSEvent(now(), UserEvents.CountryCodeChanged(countryCode)), user) { _ ⇒
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
        _ ← markContactRegistered(user, phone, false)
      } yield AddPhoneAck())
    }

  protected def addEmail(user: User, email: String): Unit =
    persistReply(TSEvent(now(), UserEvents.EmailAdded(email)), user) { event ⇒
      val rng = ThreadLocalRandom.current()
      db.run(for {
        _ ← p.UserEmailRepo.create(rng.nextInt(), userId, ACLUtils.nextAccessSalt(rng), email, "Email")
        _ ← markContactRegistered(user, email, false)
      } yield AddEmailAck())
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
        replyTo ! Status.Failure(UserExceptions.NicknameTaken)
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

  protected def updateAvatar(user: User, clientAuthId: Long, avatarOpt: Option[Avatar]): Unit = {
    persistReply(TSEvent(now(), UserEvents.AvatarUpdated(avatarOpt)), user) { evt ⇒
      val avatarData = avatarOpt map (getAvatarData(models.AvatarData.OfUser, user.id, _)) getOrElse (models.AvatarData.empty(models.AvatarData.OfUser, user.id.toLong))

      val update = UpdateUserAvatarChanged(user.id, avatarOpt)

      val relationsF = getRelations(user.id)

      for {
        _ ← db.run(p.AvatarDataRepo.createOrUpdate(avatarData))
        relatedUserIds ← relationsF
        (seqstate, _) ← userExt.broadcastClientAndUsersUpdate(user.id, clientAuthId, relatedUserIds, update, None, isFat = false, deliveryId = None)
      } yield UpdateAvatarAck(avatarOpt, seqstate)
    }
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
        case (contactUserId, localName) ⇒ ContactsUtils.registerLocalName(user.id, contactUserId, localName)
      }
      update = UpdateContactsAdded(idsLocalNames.map(_._1).toVector)
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

  private def markContactRegistered(user: User, phoneNumber: Long, isSilent: Boolean): DBIO[Unit] = {
    val date = new DateTime

    p.contact.UnregisteredPhoneContactRepo.find(phoneNumber) flatMap { contacts ⇒
      log.debug(s"Unregistered ${phoneNumber} is in contacts of users: $contacts")
      val randomId = ThreadLocalRandom.current().nextLong()
      val update = UpdateContactRegistered(user.id, isSilent, date.getMillis, randomId)
      // FIXME: #perf broadcast updates using broadcastUpdateAll to serialize update once
      val actions = contacts map { contact ⇒
        val localName = contact.name
        val serviceMessage = ServiceMessages.contactRegistered(user.id, localName.getOrElse(user.name))
        for {
          _ ← DBIO.from(ContactsUtils.registerLocalName(contact.ownerUserId, user.id, localName))
          _ ← ContactsUtils.addContact(contact.ownerUserId, user.id, phoneNumber, localName)
          _ ← DBIO.from(userExt.broadcastUserUpdate(contact.ownerUserId, update, Some(s"${localName.getOrElse(user.name)} registered"), isFat = true, deliveryId = None))
          _ ← HistoryUtils.writeHistoryMessage(
            models.Peer.privat(user.id),
            models.Peer.privat(contact.ownerUserId),
            date,
            randomId,
            serviceMessage.header,
            serviceMessage.toByteArray
          )
        } yield {
          recordRelation(user.id, contact.ownerUserId)
        }
      }
      for {
        _ ← DBIO.sequence(actions)
        _ ← p.contact.UnregisteredPhoneContactRepo.deleteAll(phoneNumber)
      } yield ()
    }
  }

  private def markContactRegistered(user: User, email: String, isSilent: Boolean): DBIO[Unit] = {
    val date = new DateTime
    for {
      contacts ← p.contact.UnregisteredEmailContactRepo.find(email)
      _ = log.debug(s"Unregistered $email is in contacts of users: $contacts")
      _ ← DBIO.sequence(contacts.map { contact ⇒
        val randomId = ThreadLocalRandom.current().nextLong()
        val update = UpdateContactRegistered(user.id, isSilent, date.getMillis, randomId)
        val localName = contact.name
        val serviceMessage = ServiceMessages.contactRegistered(user.id, localName.getOrElse(user.name))
        for {
          _ ← ContactsUtils.addContact(contact.ownerUserId, user.id, email, localName)
          _ ← DBIO.from(userExt.broadcastUserUpdate(contact.ownerUserId, update, Some(serviceMessage.text), isFat = true, deliveryId = None))
          _ ← HistoryUtils.writeHistoryMessage(
            models.Peer.privat(user.id),
            models.Peer.privat(contact.ownerUserId),
            date,
            randomId,
            serviceMessage.header,
            serviceMessage.toByteArray
          )
        } yield recordRelation(user.id, contact.ownerUserId)
      })
      _ ← p.contact.UnregisteredEmailContactRepo.deleteAll(email)
    } yield ()
  }
}
