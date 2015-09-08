package im.actor.server.user

import java.time.{ LocalDateTime, ZoneOffset }

import akka.pattern.pipe
import im.actor.api.rpc.contacts.UpdateContactRegistered
import im.actor.api.rpc.messaging._
import im.actor.api.rpc.peers.ApiPeer
import im.actor.api.rpc.users._
import im.actor.server.acl.ACLUtils
import im.actor.server.history.HistoryUtils
import im.actor.server.{ persist ⇒ p, ApiConversions, models }
import ApiConversions._
import im.actor.server.{ persist ⇒ p, models }
import im.actor.server.event.TSEvent
import im.actor.server.file.{ ImageUtils, Avatar }
import im.actor.server.sequence.SeqUpdatesManager
import im.actor.server.sequence.SeqState
import im.actor.server.social.SocialManager._
import im.actor.server.user.UserCommands._
import ContactsUtils.addContact
import org.joda.time.DateTime
import slick.driver.PostgresDriver.api._

import scala.concurrent.Future
import scala.concurrent.forkjoin.ThreadLocalRandom

private object ServiceMessages {
  def contactRegistered(userId: Int) = ApiServiceMessage("Contact registered", Some(ApiServiceExContactRegistered(userId)))
}

private[user] trait UserCommandHandlers {
  this: UserProcessor ⇒

  import ImageUtils._

  protected def create(accessSalt: String, name: String, countryCode: String, sex: ApiSex.ApiSex, isBot: Boolean): Unit = {
    log.debug("Creating user {} {}", userId, name)

    val ts = now()
    val e = UserEvents.Created(userId, accessSalt, name, countryCode, sex, isBot)
    val createEvent = TSEvent(ts, e)
    val user = User(ts, e)

    persistStashingReply(createEvent, user) { evt ⇒
      val user = models.User(
        id = userId,
        accessSalt = accessSalt,
        name = name,
        countryCode = countryCode,
        sex = models.Sex.fromInt(sex.id),
        state = models.UserState.Registered,
        createdAt = LocalDateTime.now(ZoneOffset.UTC)
      )
      db.run(for {
        _ ← p.User.create(user)
      } yield CreateAck())
    }
  }

  protected def addAuth(user: User, authId: Long): Unit = {
    persistStashingReply(TSEvent(now(), UserEvents.AuthAdded(authId)), user) { _ ⇒
      db.run(p.AuthId.setUserData(authId, user.id)) map (_ ⇒ NewAuthAck())
    }
  }

  protected def removeAuth(user: User, authId: Long): Unit =
    persistStashingReply(TSEvent(now(), UserEvents.AuthRemoved(authId)), user) { _ ⇒
      db.run(p.AuthId.delete(authId) map (_ ⇒ RemoveAuthAck()))
    }

  protected def changeCountryCode(user: User, countryCode: String): Unit =
    persistReply(TSEvent(now(), UserEvents.CountryCodeChanged(countryCode)), user) { _ ⇒
      db.run(p.User.setCountryCode(userId, countryCode) map (_ ⇒ ChangeCountryCodeAck()))
    }

  protected def changeName(user: User, name: String, clientAuthId: Long): Unit =
    persistReply(TSEvent(now(), UserEvents.NameChanged(name)), user) { _ ⇒
      val update = UpdateUserNameChanged(userId, name)
      for {
        relatedUserIds ← getRelations(userId)
        _ ← UserOffice.broadcastUsersUpdate(relatedUserIds, update, pushText = None, isFat = false, deliveryId = None)
        _ ← SeqUpdatesManager.persistAndPushUpdatesF(user.authIds.filterNot(_ == clientAuthId), update, pushText = None, isFat = false, deliveryId = None)
        seqstate ← SeqUpdatesManager.persistAndPushUpdateF(clientAuthId, update, pushText = None, isFat = false)
      } yield seqstate
    }

  protected def delete(user: User): Unit =
    persistStashingReply(TSEvent(now(), UserEvents.Deleted()), user) { _ ⇒
      db.run(p.User.setDeletedAt(userId) map (_ ⇒ DeleteAck()))
    }

  protected def addPhone(user: User, phone: Long): Unit =
    persistReply(TSEvent(now(), UserEvents.PhoneAdded(phone)), user) { _ ⇒
      val rng = ThreadLocalRandom.current()
      db.run(for {
        _ ← p.UserPhone.create(rng.nextInt(), userId, ACLUtils.nextAccessSalt(rng), phone, "Mobile phone")
        _ ← markContactRegistered(user, phone, false)
      } yield AddPhoneAck())
    }

  protected def addEmail(user: User, email: String): Unit =
    persistReply(TSEvent(now(), UserEvents.EmailAdded(email)), user) { _ ⇒
      val rng = ThreadLocalRandom.current()
      db.run(for {
        _ ← p.UserEmail.create(rng.nextInt(), userId, ACLUtils.nextAccessSalt(rng), email, "Email")
        _ ← markContactRegistered(user, email, false)
      } yield AddEmailAck())
    }

  protected def deliverMessage(user: User, peer: ApiPeer, senderUserId: Int, randomId: Long, date: DateTime, message: ApiMessage, isFat: Boolean): Unit = {
    val update = UpdateMessage(
      peer = peer,
      senderUserId = senderUserId,
      date = date.getMillis,
      randomId = randomId,
      message = message
    )

    val result = if (user.authIds.nonEmpty) {
      for {
        senderUser ← UserOffice.getApiStruct(senderUserId, userId, getAuthIdUnsafe(user))
        senderName = senderUser.localName.getOrElse(senderUser.name)
        pushText ← getPushText(peer, userId, senderName, message)
        _ ← SeqUpdatesManager.persistAndPushUpdatesF(user.authIds, update, Some(pushText), isFat, deliveryId = Some(s"msg_${peer.toString}_${randomId}"))
      } yield DeliverMessageAck()
    } else {
      Future.successful(DeliverMessageAck())
    }

    result pipeTo sender()
  }

  protected def deliverOwnMessage(user: User, peer: ApiPeer, senderAuthId: Long, randomId: Long, date: DateTime, message: ApiMessage, isFat: Boolean): Future[SeqState] = {
    val update = UpdateMessage(
      peer = peer,
      senderUserId = userId,
      date = date.getMillis,
      randomId = randomId,
      message = message
    )

    SeqUpdatesManager.persistAndPushUpdatesF(user.authIds filterNot (_ == senderAuthId), update, None, isFat, deliveryId = Some(s"msg_${peer.toString}_${randomId}"))

    val ownUpdate = UpdateMessageSent(peer, randomId, date.getMillis)
    SeqUpdatesManager.persistAndPushUpdateF(senderAuthId, ownUpdate, None, isFat, deliveryId = Some(s"msgsent_${peer.toString}_${randomId}")) pipeTo sender()
  }

  protected def changeNickname(user: User, clientAuthId: Long, nickname: Option[String]): Unit = {
    persistReply(TSEvent(now(), UserEvents.NicknameChanged(nickname)), user) { _ ⇒
      val update = UpdateUserNickChanged(userId, nickname)
      for {
        _ ← db.run(p.User.setNickname(userId, nickname))
        relatedUserIds ← getRelations(userId)
        (seqstate, _) ← UserOffice.broadcastClientAndUsersUpdate(userId, clientAuthId, relatedUserIds, update, None, isFat = false, deliveryId = None)
      } yield seqstate
    }
  }

  protected def changeAbout(user: User, clientAuthId: Long, about: Option[String]): Unit = {
    persistReply(TSEvent(now(), UserEvents.AboutChanged(about)), user) { _ ⇒
      val update = UpdateUserAboutChanged(userId, about)
      for {
        _ ← db.run(p.User.setAbout(userId, about))
        relatedUserIds ← getRelations(userId)
        (seqstate, _) ← UserOffice.broadcastClientAndUsersUpdate(userId, clientAuthId, relatedUserIds, update, None, isFat = false, deliveryId = None)
      } yield seqstate
    }
  }

  protected def updateAvatar(user: User, clientAuthId: Long, avatarOpt: Option[Avatar]): Unit = {
    persistReply(TSEvent(now(), UserEvents.AvatarUpdated(avatarOpt)), user) { evt ⇒
      val avatarData = avatarOpt map (getAvatarData(models.AvatarData.OfUser, user.id, _)) getOrElse (models.AvatarData.empty(models.AvatarData.OfUser, user.id.toLong))

      val update = UpdateUserAvatarChanged(user.id, avatarOpt)

      val relationsF = getRelations(user.id)

      for {
        _ ← db.run(p.AvatarData.createOrUpdate(avatarData))
        relatedUserIds ← relationsF
        (seqstate, _) ← UserOffice.broadcastClientAndUsersUpdate(user.id, clientAuthId, relatedUserIds, update, None, isFat = false, deliveryId = None)
      } yield UpdateAvatarAck(avatarOpt, seqstate)
    }
  }

  private def markContactRegistered(user: User, phoneNumber: Long, isSilent: Boolean): DBIO[Unit] = {
    val date = new DateTime

    p.contact.UnregisteredPhoneContact.find(phoneNumber) flatMap { contacts ⇒
      log.debug(s"Unregistered ${phoneNumber} is in contacts of users: $contacts")
      val randomId = ThreadLocalRandom.current().nextLong()
      val update = UpdateContactRegistered(user.id, isSilent, date.getMillis, randomId)
      val serviceMessage = ServiceMessages.contactRegistered(user.id)
      // FIXME: #perf broadcast updates using broadcastUpdateAll to serialize update once
      val actions = contacts map { contact ⇒
        val localName = contact.name
        for {
          _ ← addContact(contact.ownerUserId, user.id, phoneNumber, localName, user.accessSalt)
          _ ← DBIO.from(UserOffice.broadcastUserUpdate(contact.ownerUserId, update, Some(s"${localName.getOrElse(user.name)} registered"), isFat = true, deliveryId = None))
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
        _ ← p.contact.UnregisteredPhoneContact.deleteAll(phoneNumber)
      } yield ()
    }
  }

  private def markContactRegistered(user: User, email: String, isSilent: Boolean): DBIO[Unit] = {
    val date = new DateTime
    for {
      contacts ← p.contact.UnregisteredEmailContact.find(email)
      _ = log.debug(s"Unregistered $email is in contacts of users: $contacts")
      _ ← DBIO.sequence(contacts.map { contact ⇒
        val randomId = ThreadLocalRandom.current().nextLong()
        val serviceMessage = ServiceMessages.contactRegistered(user.id)
        val update = UpdateContactRegistered(user.id, isSilent, date.getMillis, randomId)
        val localName = contact.name
        for {
          _ ← addContact(contact.ownerUserId, user.id, email, localName, user.accessSalt)
          _ ← DBIO.from(UserOffice.broadcastUserUpdate(contact.ownerUserId, update, Some(s"${localName.getOrElse(user.name)} registered"), isFat = true, deliveryId = None))
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
      _ ← p.contact.UnregisteredEmailContact.deleteAll(email)
    } yield ()
  }

  private def getAuthIdUnsafe(user: User): Long = {
    user.authIds.headOption.getOrElse(throw new scala.Exception(s"There was no authId for user ${user.id}"))
  }

}
