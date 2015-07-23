package im.actor.server.user

import scala.concurrent.ExecutionContext
import scala.util.{ Failure, Success }

import akka.actor.{ ActorLogging, Props, Status }
import akka.pattern.pipe
import akka.persistence.RecoveryFailure
import com.google.protobuf.ByteString
import org.joda.time.DateTime
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc.messaging._
import im.actor.api.rpc.peers.{ Peer, PeerType }
import im.actor.server.models
import im.actor.server.office.PeerOffice
import im.actor.server.office.user.{ UserEnvelope, UserEvents }
import im.actor.server.push.{ SeqUpdatesManager, SeqUpdatesManagerRegion }
import im.actor.server.sequence.{ SeqState, SeqStateDate }
import im.actor.server.social.{ SocialManager, SocialManagerRegion }
import im.actor.server.util.{ HistoryUtils, UserUtils }

object UserOfficeActor {
  def props(
    implicit
    db:                  Database,
    seqUpdManagerRegion: SeqUpdatesManagerRegion,
    socialManagerRegion: SocialManagerRegion
  ): Props =
    Props(classOf[UserOfficeActor], db, seqUpdManagerRegion, socialManagerRegion)
}

class UserOfficeActor(
  implicit
  db:                  Database,
  seqUpdManagerRegion: SeqUpdatesManagerRegion,
  socialManagerRegion: SocialManagerRegion
) extends PeerOffice with ActorLogging {

  import HistoryUtils._
  import SeqUpdatesManager._
  import SocialManager._
  import UserEnvelope._
  import UserOffice._
  import UserUtils._

  implicit private val ec: ExecutionContext = context.dispatcher

  private val userId = self.path.name.toInt

  override def persistenceId = persistenceIdFor(userId)

  private[this] var lastReceivedDate: Option[Long] = None
  private[this] var lastReadDate: Option[Long] = None
  private[this] var authIds = Set.empty[Long]

  def receiveCommand = {
    case Payload.NewAuth(NewAuth(authId)) ⇒
      persist(UserEvents.AuthAdded(authId)) { _ ⇒
        authIds += authId
        sender() ! Status.Success(())
      }
    case Payload.DeliverGroupMessage(DeliverGroupMessage(groupId, senderUserId, randomId, date, message, isFat)) ⇒
      val update = UpdateMessage(
        peer = Peer(PeerType.Group, groupId),
        senderUserId = senderUserId,
        date = date.getMillis,
        randomId = randomId,
        message = message
      )

      persistAndPushUpdates(authIds, update, None, isFat)
    case Payload.DeliverOwnGroupMessage(DeliverOwnGroupMessage(groupId, senderAuthId, randomId, date, message, isFat)) ⇒
      val groupPeer = Peer(PeerType.Group, groupId)

      val update = UpdateMessage(
        peer = groupPeer,
        senderUserId = userId,
        date = date.getMillis,
        randomId = randomId,
        message = message
      )

      persistAndPushUpdates(authIds filterNot (_ == senderAuthId), update, None, isFat)

      val ownUpdate = UpdateMessageSent(groupPeer, randomId, date.getMillis)
      db.run(for {
        (seq, state) ← persistAndPushUpdate(senderAuthId, ownUpdate, None, isFat)
      } yield (SeqState(seq, ByteString.copyFrom(state)))) pipeTo sender()
    /*
      db.run {
        for {
          pushText <- getPushText(message, userId, senderUserId)
        } yield {
          persistAndPushUpdates(authIds, update, pushText, isFat)
        }
      }*/
    case Payload.SendMessage(SendMessage(senderUserId, senderAuthId, accessHash, randomId, message, _)) ⇒
      context become {
        case MessageSentComplete ⇒
          unstashAll()
          context become receiveCommand
        case msg ⇒ stash()
      }

      val date = new DateTime
      val dateMillis = date.getMillis

      val replyTo = sender()

      val peerUpdate = UpdateMessage(
        peer = privatePeerStruct(senderUserId),
        senderUserId = senderUserId,
        date = dateMillis,
        randomId = randomId,
        message = message
      )

      val senderUpdate = UpdateMessage(
        peer = privatePeerStruct(userId),
        senderUserId = senderUserId,
        date = dateMillis,
        randomId = randomId,
        message = message
      )

      val clientUpdate = UpdateMessageSent(privatePeerStruct(userId), randomId, dateMillis)

      val sendFuture = db.run(for {

        clientUser ← getUserUnsafe(senderUserId)
        pushText ← getPushText(message, clientUser, userId)

        _ ← broadcastUserUpdate(userId, peerUpdate, Some(pushText))

        _ ← notifyUserUpdate(senderUserId, senderAuthId, senderUpdate, None)
        (seq, state) ← persistAndPushUpdate(senderAuthId, clientUpdate, None)
      } yield {
        recordRelation(senderUserId, userId)
        db.run(writeHistoryMessage(models.Peer.privat(senderUserId), models.Peer.privat(userId), date, randomId, message.header, message.toByteArray))
        SeqStateDate(seq, ByteString.copyFrom(state), dateMillis)
      })

      sendFuture onComplete {
        case Success(seqstate) ⇒
          replyTo ! seqstate
          self ! MessageSentComplete
        case Failure(e) ⇒
          replyTo ! Status.Failure(e)
          log.error(e, "Failed to send message")
          self ! MessageSentComplete
      }
    case Payload.MessageReceived(MessageReceived(receiverUserId, _, date, receivedDate)) ⇒
      if (!lastReceivedDate.exists(_ > date)) {
        lastReceivedDate = Some(date)
        val update = UpdateMessageReceived(Peer(PeerType.Private, receiverUserId), date, receivedDate)

        db.run(for {
          _ ← broadcastUserUpdate(userId, update, None)
        } yield {
          // TODO: report errors
          db.run(markMessagesReceived(models.Peer.privat(receiverUserId), models.Peer.privat(userId), new DateTime(date)))
        }) onFailure {
          case e ⇒
            log.error(e, "Failed to mark messages received")
        }
      }
    case Payload.MessageRead(MessageRead(readerUserId, _, date, readDate)) ⇒
      if (!lastReadDate.exists(_ > date)) {
        lastReadDate = Some(date)
        val update = UpdateMessageRead(Peer(PeerType.Private, readerUserId), date, readDate)
        val readerUpdate = UpdateMessageReadByMe(Peer(PeerType.Private, userId), date)

        db.run(for {
          _ ← broadcastUserUpdate(userId, update, None)
          _ ← broadcastUserUpdate(readerUserId, readerUpdate, None)
        } yield {
          // TODO: report errors
          db.run(markMessagesRead(models.Peer.privat(readerUserId), models.Peer.privat(userId), new DateTime(date)))
        }) onFailure {
          case e ⇒
            log.error(e, "Failed to mark messages read")
        }
      }
  }

  override def receiveRecover = {
    case UserEvents.AuthAdded(authId) ⇒
      authIds += authId
    case RecoveryFailure(e) ⇒
      log.error(e, "Failed to recover")
  }
}