package im.actor.server.user

import java.time.{ LocalDateTime, ZoneOffset }

import scala.concurrent.Future
import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.util.{ Failure, Success }

import akka.actor.Status
import akka.pattern.pipe
import org.joda.time.DateTime
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc.contacts.UpdateContactRegistered
import im.actor.api.rpc.messaging.{ Message ⇒ ApiMessage, _ }
import im.actor.api.rpc.peers.{ Peer, PeerType }
import im.actor.api.rpc.users.{ Sex, UpdateUserAboutChanged, UpdateUserNameChanged, UpdateUserNickChanged }
import im.actor.server.office.PeerProcessor.MessageSentComplete
import im.actor.server.push.SeqUpdatesManager
import im.actor.server.sequence.{ SeqState, SeqStateDate }
import im.actor.server.social.SocialManager._
import im.actor.server.user.UserCommands._
import im.actor.server.user.UserOffice.InvalidAccessHash
import im.actor.server.util.HistoryUtils._
import im.actor.server.util.UserUtils._
import im.actor.server.util.{ ACLUtils, HistoryUtils }
import im.actor.server.{ models, persist ⇒ p }
import im.actor.utils.cache.CacheHelpers._

private object ServiceMessages {
  def contactRegistered(userId: Int) = ServiceMessage("Contact registered", Some(ServiceExContactRegistered(userId)))
}

private[user] trait UserCommandHandlers {
  this: UserProcessor ⇒

  protected def create(accessSalt: String, name: String, countryCode: String, sex: Sex.Sex, authId: Long): Unit = {
    val createEvent = UserEvents.Created(userId, accessSalt, name, countryCode)
    persistStashingReply(createEvent)(workWith(_, User(createEvent))) { evt ⇒
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
        _ ← p.AuthId.setUserData(authId, userId)
      } yield CreateAck())
    }
  }

  protected def addAuth(user: User, authId: Long): Unit = {
    persistStashingReply(UserEvents.AuthAdded(authId))(workWith(_, user)) { _ ⇒
      db.run(p.AuthId.setUserData(authId, user.id)) map (_ ⇒ NewAuthAck())
    }
  }

  protected def removeAuth(user: User, authId: Long): Unit =
    persistStashingReply(UserEvents.AuthRemoved(authId))(workWith(_, user)) { _ ⇒
      db.run(p.AuthId.delete(authId) map (_ ⇒ RemoveAuthAck()))
    }

  protected def changeCountryCode(user: User, countryCode: String): Unit =
    persistStashingReply(UserEvents.CountryCodeChanged(countryCode))(workWith(_, user)) { _ ⇒
      db.run(p.User.setCountryCode(userId, countryCode) map (_ ⇒ ChangeCountryCodeAck()))
    }

  protected def changeName(user: User, name: String, clientAuthId: Long): Unit =
    persistStashingReply(UserEvents.NameChanged(name))(workWith(_, user)) { _ ⇒
      val update = UpdateUserNameChanged(userId, name)
      for {
        relatedUserIds ← getRelations(userId)
        _ ← UserOffice.broadcastUsersUpdate(relatedUserIds, update, None)
        _ ← SeqUpdatesManager.persistAndPushUpdatesF(user.authIds.filterNot(_ == clientAuthId), update, None)
        SeqState(seq, state) ← SeqUpdatesManager.persistAndPushUpdateF(clientAuthId, update, None)
      } yield ChangeNameAck(seq, state)
    }

  protected def delete(user: User): Unit =
    persistStashingReply(UserEvents.Deleted())(workWith(_, user)) { _ ⇒
      db.run(p.User.setDeletedAt(userId) map (_ ⇒ DeleteAck()))
    }

  protected def addPhone(user: User, phone: Long): Unit =
    persistStashingReply(UserEvents.PhoneAdded(phone))(workWith(_, user)) { _ ⇒
      val rng = ThreadLocalRandom.current()
      val action = for {
        _ ← p.UserPhone.create(rng.nextInt(), userId, ACLUtils.nextAccessSalt(rng), phone, "Mobile phone")
        _ ← markContactRegistered(user, phone, false)
      } yield AddPhoneAck()
      db.run(action)
    }

  protected def addEmail(user: User, email: String): Unit =
    persistStashingReply(UserEvents.EmailAdded(email))(workWith(_, user)) { _ ⇒
      val rng = ThreadLocalRandom.current()
      val action = for {
        _ ← p.UserEmail.create(rng.nextInt(), userId, ACLUtils.nextAccessSalt(rng), email, "Email")
        _ ← markContactRegistered(user, email, false)
      } yield AddEmailAck()
      db.run(action)
    }

  protected def deliverMessage(user: User, peer: Peer, senderUserId: Int, randomId: Long, date: DateTime, message: ApiMessage, isFat: Boolean): Future[Seq[SeqState]] = {
    val update = UpdateMessage(
      peer = peer,
      senderUserId = senderUserId,
      date = date.getMillis,
      randomId = randomId,
      message = message
    )
    db.run {
      for {
        senderUser ← getUserUnsafe(senderUserId)
        pushText ← getPushText(message, senderUser, userId)
        seqs ← SeqUpdatesManager.persistAndPushUpdates(user.authIds, update, Some(pushText), isFat)
      } yield seqs
    }
  }

  protected def deliverOwnMessage(user: User, peer: Peer, senderAuthId: Long, randomId: Long, date: DateTime, message: ApiMessage, isFat: Boolean): Future[SeqState] = {
    val update = UpdateMessage(
      peer = peer,
      senderUserId = userId,
      date = date.getMillis,
      randomId = randomId,
      message = message
    )

    SeqUpdatesManager.persistAndPushUpdates(user.authIds filterNot (_ == senderAuthId), update, None, isFat)

    val ownUpdate = UpdateMessageSent(peer, randomId, date.getMillis)
    db.run(SeqUpdatesManager.persistAndPushUpdate(senderAuthId, ownUpdate, None, isFat)) pipeTo sender()
  }

  protected def sendMessage(user: User, senderUserId: Int, senderAuthId: Long, accessHash: Long, randomId: Long, message: ApiMessage, isFat: Boolean): Unit = {
    if (accessHash == ACLUtils.userAccessHash(senderAuthId, userId, user.accessSalt)) {
      val replyTo = sender()
      context become {
        case MessageSentComplete ⇒
          unstashAll()
          context become working(user)
        case msg ⇒ stash()
      }
      val date = new DateTime
      val dateMillis = date.getMillis

      val sendFuture: Future[SeqStateDate] =
        withCachedFuture[AuthIdRandomId, SeqStateDate](senderAuthId → randomId) { () ⇒
          for {
            _ ← Future.successful(UserOffice.deliverMessage(userId, privatePeerStruct(senderUserId), senderUserId, randomId, date, message, isFat))
            SeqState(seq, state) ← UserOffice.deliverOwnMessage(senderUserId, privatePeerStruct(userId), senderAuthId, randomId, date, message, isFat)
            _ ← Future.successful(recordRelation(senderUserId, userId))
          } yield {
            db.run(writeHistoryMessage(models.Peer.privat(senderUserId), models.Peer.privat(userId), date, randomId, message.header, message.toByteArray))
            SeqStateDate(seq, state, dateMillis)
          }
        }
      sendFuture onComplete {
        case Success(seqstate) ⇒
          replyTo ! seqstate
          context.self ! MessageSentComplete
        case Failure(e) ⇒
          replyTo ! Status.Failure(e)
          log.error(e, "Failed to send message")
          context.self ! MessageSentComplete
      }
    } else {
      sender() ! Status.Failure(InvalidAccessHash)
    }
  }

  protected def messageReceived(user: User, receiverUserId: Int, date: Long, receivedDate: Long): Unit =
    if (!user.lastReceivedDate.exists(_ > date)) {
      persistStashingReply(UserEvents.MessageReceived(date))(workWith(_, user)) { _ ⇒
        val update = UpdateMessageReceived(Peer(PeerType.Private, receiverUserId), date, receivedDate)
        for {
          _ ← SeqUpdatesManager.persistAndPushUpdatesF(user.authIds, update, None)
        } yield {
          // TODO: report errors
          db.run(markMessagesReceived(models.Peer.privat(receiverUserId), models.Peer.privat(userId), new DateTime(date)))
        }
      }
    }

  protected def messageRead(user: User, readerUserId: Int, date: Long, readDate: Long): Unit =
    if (!user.lastReadDate.exists(_ > date)) {
      persistStashingReply(UserEvents.MessageRead(date))(workWith(_, user)) { _ ⇒
        val update = UpdateMessageRead(Peer(PeerType.Private, readerUserId), date, readDate)
        val readerUpdate = UpdateMessageReadByMe(Peer(PeerType.Private, userId), date)
        for {
          _ ← SeqUpdatesManager.persistAndPushUpdatesF(user.authIds, update, None)
          _ ← UserOffice.broadcastUserUpdate(readerUserId, readerUpdate, None) //todo: may be replace with MessageReadOwn
        } yield {
          // TODO: report errors
          db.run(markMessagesRead(models.Peer.privat(readerUserId), models.Peer.privat(userId), new DateTime(date)))
        }
      }
    }

  protected def changeNickname(user: User, clientAuthId: Long, nickname: Option[String]): Unit = {
    persistStashingReply(UserEvents.NicknameChanged(nickname))(workWith(_, user)) { _ ⇒
      val update = UpdateUserNickChanged(userId, nickname)
      for {
        _ ← db.run(p.User.setNickname(userId, nickname))
        relatedUserIds ← getRelations(userId)
        _ = println("broadcasting update")
        (seqstate, _) ← UserOffice.broadcastClientAndUsersUpdate(userId, clientAuthId, relatedUserIds, update, None, isFat = false)
        _ = println("broadcasted update")
      } yield seqstate
    }
  }

  protected def changeAbout(user: User, clientAuthId: Long, about: Option[String]): Unit = {
    persistStashingReply(UserEvents.AboutChanged(about))(workWith(_, user)) { _ ⇒
      val update = UpdateUserAboutChanged(userId, about)
      for {
        _ ← db.run(p.User.setAbout(userId, about))
        relatedUserIds ← getRelations(userId)
        (seqstate, _) ← UserOffice.broadcastClientAndUsersUpdate(userId, clientAuthId, relatedUserIds, update, None, isFat = false)
      } yield seqstate
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
        for {
          _ ← p.contact.UserPhoneContact.createOrRestore(contact.ownerUserId, user.id, phoneNumber, Some(user.name), user.accessSalt)
          _ ← DBIO.from(UserOffice.broadcastUserUpdate(contact.ownerUserId, update, Some(s"${contact.name.getOrElse(user.name)} registered")))
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
        for {
          _ ← p.contact.UserEmailContact.createOrRestore(contact.ownerUserId, user.id, email, Some(user.name), user.accessSalt)
          _ ← DBIO.from(UserOffice.broadcastUserUpdate(contact.ownerUserId, update, Some(s"${contact.name.getOrElse(user.name)} registered")))
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

}
