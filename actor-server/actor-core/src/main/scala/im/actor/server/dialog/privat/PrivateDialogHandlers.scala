package im.actor.server.dialog.privat

import akka.actor.Status
import akka.pattern.pipe
import im.actor.api.rpc.messaging.{ Message ⇒ ApiMessage, UpdateMessageRead, UpdateMessageReadByMe, UpdateMessageReceived }
import im.actor.api.rpc.peers.{ Peer, PeerType }
import im.actor.server.dialog.PrivateDialogCommands.Origin.{ LEFT, RIGHT }
import im.actor.server.dialog.{ ReadFailed, ReceiveFailed, AuthIdRandomId, PrivateDialogCommands }
import im.actor.server.misc.UpdateCounters
import im.actor.server.models
import im.actor.server.push.SeqUpdatesManager
import im.actor.server.sequence.{ SeqState, SeqStateDate }
import im.actor.server.social.SocialManager._
import im.actor.server.user.UserOffice
import im.actor.server.util.HistoryUtils._
import im.actor.utils.cache.CacheHelpers._
import org.joda.time.DateTime

import scala.concurrent.Future

trait PrivateDialogHandlers extends UpdateCounters {
  this: PrivateDialog ⇒

  import PrivateDialogCommands._
  import PrivateDialogEvents._

  protected def sendMessage(
    state:        PrivateDialogState,
    origin:       Origin,
    senderAuthId: Long,
    randomId:     Long,
    message:      ApiMessage,
    isFat:        Boolean
  ): Unit = {
    val date = new DateTime
    val dateMillis = date.getMillis
    deferStashingReply(LastMessageDate(dateMillis, reverse(origin)), state) { e ⇒
      withCachedFuture[AuthIdRandomId, SeqStateDate](senderAuthId → randomId) {
        val userState = state.get(origin)
        for {
          _ ← UserOffice.deliverMessage(userState.peerId, privatePeerStruct(userState.userId), userState.userId, randomId, date, message, isFat)
          SeqState(seq, state) ← UserOffice.deliverOwnMessage(userState.userId, privatePeerStruct(userState.peerId), senderAuthId, randomId, date, message, isFat)
          _ = recordRelation(userState.userId, userState.peerId)
          _ ← db.run(writeHistoryMessage(models.Peer.privat(userState.userId), models.Peer.privat(userState.peerId), date, randomId, message.header, message.toByteArray))
        } yield SeqStateDate(seq, state, dateMillis)
      } recover {
        case e ⇒
          log.error(e, "Failed to send message")
          throw e
      }
    }
  }

  protected def messageReceived(state: PrivateDialogState, origin: Origin, date: Long): Unit = {
    val replyTo = sender()

    val userState = state.get(origin)
    val now = System.currentTimeMillis
    (if (!userState.lastReceiveDate.exists(_ >= date) &&
      !(date > now) &&
      (userState.lastMessageDate.isEmpty || userState.lastMessageDate.exists(_ >= date))) {
      workWith(LastReceiveDate(date, origin), state)

      val update = UpdateMessageReceived(privatePeerStruct(userState.userId), date, now)
      for {
        _ ← UserOffice.broadcastUserUpdate(userState.peerId, update, None, isFat = false)
        _ ← db.run(markMessagesReceived(models.Peer.privat(userState.userId), models.Peer.privat(userState.peerId), new DateTime(date)))
      } yield MessageReceivedAck()
    } else {
      Future.successful(MessageReceivedAck())
    }) pipeTo replyTo onFailure {
      case e ⇒
        replyTo ! Status.Failure(ReceiveFailed)
        log.error(e, "Failed to mark messages received")
    }
  }

  protected def messageRead(state: PrivateDialogState, origin: Origin, readerAuthId: Long, date: Long): Unit = {
    val replyTo = sender()

    val userState = state.get(origin)
    val now = System.currentTimeMillis
    (if (!userState.lastReadDate.exists(_ >= date) &&
      !(date > now) &&
      (userState.lastMessageDate.isEmpty || userState.lastMessageDate.exists(_ >= date))) {
      workWith(LastReadDate(date, origin), state)

      val update = UpdateMessageRead(privatePeerStruct(userState.userId), date, now)
      val readerUpdate = UpdateMessageReadByMe(privatePeerStruct(userState.peerId), date)
      for {
        _ ← UserOffice.broadcastUserUpdate(userState.peerId, update, None, isFat = false)
        _ ← db.run(markMessagesRead(models.Peer.privat(userState.userId), models.Peer.privat(userState.peerId), new DateTime(date)))
        counterUpdate ← db.run(getUpdateCountersChanged(userState.userId))
        _ ← UserOffice.broadcastUserUpdate(userState.userId, counterUpdate, None, isFat = false)
        _ ← db.run(SeqUpdatesManager.notifyUserUpdate(userState.userId, readerAuthId, readerUpdate, None, isFat = false))
      } yield MessageReadAck()
    } else {
      Future.successful(MessageReadAck())
    }) pipeTo replyTo onFailure {
      case e ⇒
        replyTo ! Status.Failure(ReadFailed)
        log.error(e, "Failed to mark messages read")
    }
  }

  private def privatePeerStruct(userId: Int): Peer = Peer(PeerType.Private, userId)

  private def reverse: PartialFunction[Origin, Origin] = {
    case LEFT  ⇒ RIGHT
    case RIGHT ⇒ LEFT
  }

}
