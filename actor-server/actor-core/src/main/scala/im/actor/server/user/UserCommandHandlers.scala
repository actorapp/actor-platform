package im.actor.server.user

import java.time.{ LocalDateTime, ZoneOffset }
import java.util.TimeZone

import akka.actor.{ ActorSystem, Status }
import akka.http.scaladsl.util.FastFuture
import akka.pattern.pipe
import im.actor.api.rpc.contacts.{ UpdateContactRegistered, UpdateContactsAdded, UpdateContactsRemoved }
import im.actor.api.rpc.messaging._
import im.actor.api.rpc.misc.ApiExtension
import im.actor.api.rpc.peers.{ ApiPeer, ApiPeerType }
import im.actor.api.rpc.users._
import im.actor.concurrent.FutureExt
import im.actor.config.ActorConfig
import im.actor.server.ApiConversions._
import im.actor.server.acl.ACLUtils
import im.actor.server.bots.BotCommand
import im.actor.server.file.{ Avatar, ImageUtils }
import im.actor.server.model.{ AvatarData, Sex, User }
import im.actor.server.model.contact.{ UserContact, UserEmailContact, UserPhoneContact }
import im.actor.server.office.EntityNotFound
import im.actor.server.persist.contact._
import im.actor.server.persist._
import im.actor.server.sequence.{ PushRules, SequenceErrors }
import im.actor.server.social.SocialManager._
import im.actor.server.user.UserCommands._
import im.actor.server.user.UserErrors.{ BotCommandAlreadyExists, InvalidBotCommand }
import im.actor.util.misc.StringUtils
import im.actor.util.ThreadLocalSecureRandom
import org.joda.time.DateTime
import slick.driver.PostgresDriver.api._

import scala.concurrent.Future
import scala.util.{ Failure, Success }
import scala.util.control.NoStackTrace

abstract class UserError(message: String) extends RuntimeException(message) with NoStackTrace

object UserErrors {

  final case class UserNotFound(id: Int) extends EntityNotFound(s"User $id not found")

  case object NicknameTaken extends UserError("Nickname taken")

  case object InvalidNickname extends UserError("Invalid nickname")

  case object InvalidName extends UserError("Invalid name")

  final case class InvalidTimeZone(tz: String) extends UserError(s"Invalid time zone: $tz")

  final case class InvalidLocale(locale: String) extends UserError(s"Invalid locale: $locale")

  case object EmptyLocalesList extends UserError("Empty locale list")

  final case class InvalidBotCommand(slashCommand: String)
    extends UserError(s"Invalid slash command: $slashCommand")

  final case class BotCommandAlreadyExists(slashCommand: String)
    extends UserError(s"Bot command already exists: $slashCommand")

  case object ContactNotFound extends UserError("Contact not found")

}

private object ServiceMessages {
  def contactRegistered(userId: Int, name: String)(implicit system: ActorSystem) = {
    ApiServiceMessage(s"$name joined ${ActorConfig.projectName}", Some(ApiServiceExContactRegistered(userId)))
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
        val e = UserEvents.Created(ts, userId, accessSalt, nickname, name, countryCode, sex, isBot, extensions, external, isAdmin = Some(isAdmin))
        val user = UserBuilder(e)

        persistStashingReply(e, user, replyTo) { evt ⇒
          val user = User(
            id = userId,
            accessSalt = accessSalt,
            nickname = nickname,
            name = name,
            countryCode = countryCode,
            sex = Sex.fromInt(sex.id),
            state = im.actor.server.model.UserState.Registered,
            createdAt = LocalDateTime.now(ZoneOffset.UTC),
            external = external,
            isBot = isBot
          )
          db.run(for {
            _ ← UserRepo.create(user)
          } yield CreateAck()) andThen {
            case Success(_) ⇒ userExt.hooks.afterCreate.runAll(user.id)
          }
        }
      } else {
        replyTo ! Status.Failure(UserErrors.NicknameTaken)
      }
    }
  }

  protected def updateIsAdmin(state: UserState, isAdmin: Option[Boolean]): Unit = {
    persist(UserEvents.IsAdminUpdated(now(), isAdmin)) { e ⇒
      context become working(updatedState(e, state))

      sender() ! UpdateIsAdminAck()
    }
  }

  protected def addAuth(user: UserState, authId: Long): Unit = {
    persistStashingReply(UserEvents.AuthAdded(now(), authId), user) { _ ⇒
      db.run(AuthSessionRepo.findByAuthId(authId)) foreach {
        case Some(authSession) ⇒
          userExt.hooks.afterAuth.runAll(user.id, authSession.appId, authSession.deviceTitle)
        case None ⇒ log.error("AuthSession for {} was not found", authId)
      }

      db.run(AuthIdRepo.setUserData(authId, user.id)) map (_ ⇒ NewAuthAck())
    }
  }

  protected def removeAuth(user: UserState, authId: Long): Unit =
    persistStashingReply(UserEvents.AuthRemoved(now(), authId), user) { _ ⇒
      db.run(AuthIdRepo.delete(authId) map (_ ⇒ RemoveAuthAck()))
    }

  protected def changeCountryCode(user: UserState, countryCode: String): Unit =
    persistReply(UserEvents.CountryCodeChanged(now(), countryCode), user) { e ⇒
      db.run(UserRepo.setCountryCode(userId, countryCode) map (_ ⇒ ChangeCountryCodeAck()))
    }

  protected def changeName(user: UserState, name: String): Unit = {
    val replyTo = sender()

    if (StringUtils.validName(name).fold(l ⇒ false, r ⇒ true)) {
      persistReply(UserEvents.NameChanged(now(), name), user) { _ ⇒
        val update = UpdateUserNameChanged(userId, name)
        for {
          relatedUserIds ← getRelations(userId)
          (seqstate, _) ← seqUpdatesExt.broadcastOwnSingleUpdate(userId, relatedUserIds, update)
          _ ← db.run(UserRepo.setName(userId, name))
        } yield seqstate
      }
    } else {
      replyTo ! Status.Failure(UserErrors.InvalidName)
    }
  }

  protected def delete(user: UserState): Unit =
    persistStashingReply(UserEvents.Deleted(now()), user) { _ ⇒
      db.run(UserRepo.setDeletedAt(userId) map (_ ⇒ DeleteAck()))
    }

  protected def addPhone(user: UserState, phone: Long): Unit =
    if (user.phones.contains(phone))
      sender() ! AddPhoneAck()
    else {
      persistReply(UserEvents.PhoneAdded(now(), phone), user) { _ ⇒
        val rng = ThreadLocalSecureRandom.current()
        db.run(for {
          _ ← UserPhoneRepo.create(rng.nextInt(), userId, ACLUtils.nextAccessSalt(rng), phone, "Mobile phone")
          _ ← DBIO.from(markContactRegistered(user, phone, false))
        } yield {
          AddPhoneAck()
        }) andThen {
          case Failure(e) ⇒ log.error(e, "Failed to add phone")
        }
      }
    }

  protected def addEmail(user: UserState, email: String): Unit =
    if (user.emails.contains(email))
      sender() ! AddEmailAck()
    else {
      persistReply(UserEvents.EmailAdded(now(), email), user) { event ⇒
        val rng = ThreadLocalSecureRandom.current()
        db.run(for {
          _ ← UserEmailRepo.create(rng.nextInt(), userId, ACLUtils.nextAccessSalt(rng), email, "Email")
          _ ← DBIO.from(markContactRegistered(user, email, false))
        } yield {
          AddEmailAck()
        }) andThen {
          case Failure(e) ⇒ log.error(e, "Failed to add email")
        }
      }
    }

  protected def addSocialContact(user: UserState, contact: SocialContact): Unit =
    persistReply(UserEvents.SocialContactAdded(now(), contact), user) { _ ⇒
      Future.successful(AddSocialContactAck())
    }

  protected def changeNickname(user: UserState, nicknameOpt: Option[String]): Unit = {
    val replyTo = sender()

    onSuccess(checkNicknameExists(nicknameOpt)) { exists ⇒
      if (!exists) {
        if (nicknameOpt map StringUtils.validUsername getOrElse true) {
          persistReply(UserEvents.NicknameChanged(now(), nicknameOpt), user, replyTo) { _ ⇒
            val update = UpdateUserNickChanged(userId, nicknameOpt)

            for {
              _ ← db.run(UserRepo.setNickname(userId, nicknameOpt))
              relatedUserIds ← getRelations(userId)
              (seqstate, _) ← seqUpdatesExt.broadcastOwnSingleUpdate(userId, relatedUserIds, update)
            } yield seqstate
          }
        } else {
          replyTo ! Status.Failure(UserErrors.InvalidNickname)
        }
      } else {
        replyTo ! Status.Failure(UserErrors.NicknameTaken)
      }
    }
  }

  protected def changeAbout(user: UserState, about: Option[String]): Unit = {
    persistReply(UserEvents.AboutChanged(now(), about), user) { _ ⇒
      val update = UpdateUserAboutChanged(userId, about)
      for {
        _ ← db.run(UserRepo.setAbout(userId, about))
        relatedUserIds ← getRelations(userId)
        (seqstate, _) ← seqUpdatesExt.broadcastOwnSingleUpdate(userId, relatedUserIds, update)
      } yield seqstate
    }
  }

  protected def changeTimeZone(user: UserState, timeZone: String): Unit = {
    def validTimeZone(tz: String): Boolean = TimeZone.getAvailableIDs.contains(tz)

    if (validTimeZone(timeZone)) {
      if (!user.timeZone.contains(timeZone)) {
        persistReply(UserEvents.TimeZoneChanged(now(), Some(timeZone)), user) { _ ⇒
          val update = UpdateUserTimeZoneChanged(user.id, Some(timeZone))
          for {
            relatedUserIds ← getRelations(user.id)
            (seqstate, _) ← seqUpdatesExt.broadcastOwnSingleUpdate(user.id, relatedUserIds, update)
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

  protected def changePreferredLanguages(user: UserState, preferredLanguages: Seq[String]): Unit = {
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
            persistReply(UserEvents.PreferredLanguagesChanged(now(), preferredLanguages), user) { _ ⇒
              val update = UpdateUserPreferredLanguagesChanged(user.id, preferredLanguages.toVector)
              for {
                relatedUserIds ← getRelations(user.id)
                (seqstate, _) ← seqUpdatesExt.broadcastOwnSingleUpdate(user.id, relatedUserIds, update)
              } yield seqstate
            }
        }
    }
  }

  protected def addBotCommand(user: UserState, rawCommand: BotCommand): Unit = {
    val command = rawCommand.copy(slashCommand = rawCommand.slashCommand.trim)
    def isValid(command: BotCommand) = command.slashCommand.matches("^[0-9a-zA-Z_]{2,32}")

    if (user.botCommands.exists(_.slashCommand == command.slashCommand)) {
      sender() ! Status.Failure(BotCommandAlreadyExists(command.slashCommand))
    } else {
      if (isValid(command)) {
        persistReply(UserEvents.BotCommandAdded(now(), command), user) { _ ⇒
          val update = UpdateUserBotCommandsChanged(user.id, user.botCommands :+ command)
          for {
            relatedUserIds ← getRelations(user.id)
            _ ← seqUpdatesExt.broadcastOwnSingleUpdate(user.id, relatedUserIds, update)
          } yield AddBotCommandAck()
        }
      } else {
        sender() ! Status.Failure(InvalidBotCommand(command.slashCommand))
      }
    }
  }

  protected def removeBotCommand(user: UserState, slashCommand: String) =
    if (user.botCommands.exists(_.slashCommand == slashCommand)) {
      persistReply(UserEvents.BotCommandRemoved(now(), slashCommand), user) { _ ⇒
        val update = UpdateUserBotCommandsChanged(user.id, user.botCommands.filterNot(_.slashCommand == slashCommand))
        for {
          relatedUserIds ← getRelations(user.id)
          _ ← seqUpdatesExt.broadcastOwnSingleUpdate(user.id, relatedUserIds, update)
        } yield RemoveBotCommandAck()
      }
    } else {
      sender() ! RemoveBotCommandAck()
    }

  protected def addExt(user: UserState, ext: UserExt) = {
    persist(UserEvents.ExtAdded(now(), ext)) { e ⇒
      val newState = updatedState(e, user)
      context become working(newState)
      val update = UpdateUserExtChanged(userId, Some(extToApi(newState.ext)))
      (for {
        relatedUserIds ← getRelations(user.id)
        _ ← seqUpdatesExt.broadcastSingleUpdate(relatedUserIds + user.id, update)
      } yield AddExtAck()) pipeTo sender()
    }
  }

  protected def removeExt(user: UserState, key: String) = {
    persist(UserEvents.ExtRemoved(now(), key)) { e ⇒
      val newState = updatedState(e, user)
      context become working(newState)
      val update = UpdateUserExtChanged(userId, Some(extToApi(newState.ext)))
      (for {
        relatedUserIds ← getRelations(user.id)
        _ ← seqUpdatesExt.broadcastSingleUpdate(relatedUserIds + user.id, update)
      } yield RemoveExtAck()) pipeTo sender()
    }
  }

  protected def updateAvatar(user: UserState, avatarOpt: Option[Avatar]): Unit = {
    persistReply(UserEvents.AvatarUpdated(now(), avatarOpt), user) { evt ⇒
      val avatarData = avatarOpt map (getAvatarData(AvatarData.OfUser, user.id, _)) getOrElse AvatarData.empty(AvatarData.OfUser, user.id.toLong)

      val update = UpdateUserAvatarChanged(user.id, avatarOpt)

      val relationsF = getRelations(user.id)

      for {
        _ ← db.run(AvatarDataRepo.createOrUpdate(avatarData))
        relatedUserIds ← relationsF
        (seqstate, _) ← seqUpdatesExt.broadcastOwnSingleUpdate(user.id, relatedUserIds, update)
      } yield UpdateAvatarAck(avatarOpt, seqstate)
    }
  }

  protected def addContacts(
    user:          UserState,
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
          contacts.editLocalName(contactUserId, localName, supressUpdate = true)
      }
      update = UpdateContactsAdded(idsLocalNames.keys.toVector)
      seqstate ← seqUpdatesExt.deliverSingleUpdate(user.id, update, PushRules(isFat = true))
    } yield seqstate) pipeTo sender()
  }

  protected def removeContact(
    user:          UserState,
    contactUserId: Int
  ): Unit = {
    val updLocalName = UpdateUserLocalNameChanged(contactUserId, None)
    val updContact = UpdateContactsRemoved(Vector(contactUserId))

    (db.run(UserContactRepo.find(user.id, contactUserId)) flatMap {
      case Some(_) ⇒
        for {
          _ ← db.run(UserContactRepo.delete(user.id, contactUserId))
          _ ← seqUpdatesExt.deliverSingleUpdate(user.id, updLocalName)
          seqstate ← seqUpdatesExt.deliverSingleUpdate(user.id, updContact)
        } yield seqstate
      case None ⇒ Future.failed(UserErrors.ContactNotFound)
    }) pipeTo sender()
  }

  private def checkNicknameExists(nicknameOpt: Option[String]): Future[Boolean] = {
    nicknameOpt match {
      case Some(nickname) ⇒ db.run(UserRepo.nicknameExists(nickname))
      case None           ⇒ Future.successful(false)
    }
  }

  // TODO: DRY it, finally!
  private def markContactRegistered(user: UserState, phoneNumber: Long, isSilent: Boolean): Future[Unit] = {
    val date = new DateTime
    for {
      contacts ← db.run(UnregisteredPhoneContactRepo.find(phoneNumber))
      _ = log.debug(s"Unregistered $phoneNumber is in contacts of users: $contacts")
      _ ← Future.sequence(contacts map { contact ⇒
        val randomId = ThreadLocalSecureRandom.current().nextLong()
        val updateContactRegistered = UpdateContactRegistered(user.id, isSilent, date.getMillis, randomId)
        val updateContactsAdded = UpdateContactsAdded(Vector(user.id))
        val localName = contact.name
        val serviceMessage = ServiceMessages.contactRegistered(user.id, localName.getOrElse(user.name))
        for {
          _ ← userExt.addContact(contact.ownerUserId, user.id, localName, Some(phoneNumber), None)
          _ ← userExt.broadcastUserUpdate(contact.ownerUserId, updateContactRegistered, Some(s"${localName.getOrElse(user.name)} registered"), isFat = true, reduceKey = None, deliveryId = None)
          _ ← userExt.broadcastUserUpdate(contact.ownerUserId, updateContactsAdded, None, isFat = false, reduceKey = None, deliveryId = None)
          _ ← dialogExt.writeMessageSelf(
            contact.ownerUserId,
            ApiPeer(ApiPeerType.Private, user.id),
            user.id,
            date,
            randomId,
            serviceMessage
          )
        } yield {
          recordRelation(user.id, contact.ownerUserId)
        }
      })
      _ ← db.run(UnregisteredPhoneContactRepo.deleteAll(phoneNumber))
    } yield ()
  }

  private def markContactRegistered(user: UserState, email: String, isSilent: Boolean): Future[Unit] = {
    val date = new DateTime
    for {
      _ ← userExt.hooks.beforeEmailContactRegistered.runAll(user.id, email)
      contacts ← db.run(UnregisteredEmailContactRepo.find(email))
      _ = log.debug(s"Unregistered $email is in contacts of users: $contacts")
      _ ← Future.sequence(contacts.map { contact ⇒
        val randomId = ThreadLocalSecureRandom.current().nextLong()
        val updateContactRegistered = UpdateContactRegistered(user.id, isSilent, date.getMillis, randomId)
        val updateContactsAdded = UpdateContactsAdded(Vector(user.id))
        val localName = contact.name
        val serviceMessage = ServiceMessages.contactRegistered(user.id, localName.getOrElse(user.name))
        for {
          _ ← userExt.addContact(contact.ownerUserId, user.id, localName, None, Some(email))
          _ ← userExt.broadcastUserUpdate(contact.ownerUserId, updateContactRegistered, Some(serviceMessage.text), isFat = true, reduceKey = None, deliveryId = None)
          _ ← userExt.broadcastUserUpdate(contact.ownerUserId, updateContactsAdded, None, isFat = false, reduceKey = None, deliveryId = None)
          _ ← dialogExt.writeMessageSelf(
            contact.ownerUserId,
            ApiPeer(ApiPeerType.Private, user.id),
            user.id,
            date,
            randomId,
            serviceMessage
          )
        } yield recordRelation(user.id, contact.ownerUserId)
      })
      _ ← db.run(UnregisteredEmailContactRepo.deleteAll(email))
    } yield ()
  }
}
