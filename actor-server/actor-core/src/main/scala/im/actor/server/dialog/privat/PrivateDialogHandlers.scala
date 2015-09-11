package im.actor.server.dialog.privat

import akka.actor.Status
import akka.pattern.pipe
import im.actor.api.rpc.messaging.{ ApiMessage, UpdateMessageRead, UpdateMessageReadByMe, UpdateMessageReceived }
import im.actor.api.rpc.peers.{ ApiPeer, ApiPeerType }
import im.actor.server.dialog._
import im.actor.server.history.HistoryUtils
import im.actor.server.misc.UpdateCounters
import im.actor.server.models
import im.actor.server.sequence.{ SeqState, SeqStateDate }
import im.actor.server.social.SocialManager._
import HistoryUtils._
import im.actor.util.cache.CacheHelpers._
import org.joda.time.DateTime

import scala.concurrent.Future

trait PrivateDialogHandlers extends UpdateCounters {
  this: PrivateDialog ⇒

  import DialogCommands._
  import PrivateDialogEvents._

  protected def sendMessage(
    state:        PrivateDialogState,
    senderUserId: Int,
    senderAuthId: Long,
    randomId:     Long,
    message:      ApiMessage,
    isFat:        Boolean
  ): Unit = {
    val date = new DateTime
    val dateMillis = date.getMillis
    val userState = state(senderUserId)
    deferStashingReply(LastMessageDate(dateMillis, userState.peerId), state) { e ⇒
      withCachedFuture[AuthIdRandomId, SeqStateDate](senderAuthId → randomId) {
        for {
          SeqState(seq, state) ← delivery.senderDelivery(senderUserId, senderAuthId, privatePeerStruct(userState.peerId), randomId, dateMillis, message, isFat)
          _ = recordRelation(senderUserId, userState.peerId)
          _ ← db.run(writeHistoryMessage(models.Peer.privat(senderUserId), models.Peer.privat(userState.peerId), date, randomId, message.header, message.toByteArray))
          _ ← delivery.receiverDelivery(userState.peerId, senderUserId, privatePeerStruct(senderUserId), randomId, dateMillis, message, isFat)
        } yield SeqStateDate(seq, state, dateMillis)
      } recover {
        case e ⇒
          log.error(e, "Failed to send message")
          throw e
      }
    }
  }

  protected def messageReceived(state: PrivateDialogState, receiverUserId: Int, date: Long): Unit = {
    val replyTo = sender()

    val userState = state(receiverUserId)
    val now = System.currentTimeMillis
    (if (!userState.lastReceiveDate.exists(_ >= date) &&
      !(date > now) &&
      (userState.lastMessageDate.isEmpty || userState.lastMessageDate.exists(_ >= date))) {
      context become working(updatedState(LastReceiveDate(date, receiverUserId), state))

      val update = UpdateMessageReceived(privatePeerStruct(receiverUserId), date, now)
      for {
        _ ← userExt.broadcastUserUpdate(userState.peerId, update, None, isFat = false, deliveryId = None)
        _ ← db.run(markMessagesReceived(models.Peer.privat(receiverUserId), models.Peer.privat(userState.peerId), new DateTime(date)))
      } yield MessageReceivedAck()
    } else {
      Future.successful(MessageReceivedAck())
    }) pipeTo replyTo onFailure {
      case e ⇒
        replyTo ! Status.Failure(ReceiveFailed)
        log.error(e, "Failed to mark messages received")
    }
  }

  protected def messageRead(state: PrivateDialogState, readerUserId: Int, readerAuthId: Long, date: Long): Unit = {
    val replyTo = sender()

    val userState = state(readerUserId)
    val now = System.currentTimeMillis
    (if (!userState.lastReadDate.exists(_ >= date) &&
      !(date > now) &&
      (userState.lastMessageDate.isEmpty || userState.lastMessageDate.exists(_ >= date))) {
      context become working(updatedState(LastReadDate(date, readerUserId), state))

      val update = UpdateMessageRead(privatePeerStruct(readerUserId), date, now)
      val readerUpdate = UpdateMessageReadByMe(privatePeerStruct(userState.peerId), date)
      for {
        _ ← userExt.broadcastUserUpdate(userState.peerId, update, None, isFat = false, deliveryId = None)
        counterUpdate ← db.run(for {
          _ ← markMessagesRead(models.Peer.privat(readerUserId), models.Peer.privat(userState.peerId), new DateTime(date))
          u ← getUpdateCountersChanged(readerUserId)
        } yield u)
        _ ← userExt.broadcastUserUpdate(readerUserId, counterUpdate, None, isFat = false, deliveryId = None)
        _ ← userExt.notifyUserUpdate(readerUserId, readerAuthId, readerUpdate, None, isFat = false, deliveryId = None)
      } yield MessageReadAck()
    } else {
      Future.successful(MessageReadAck())
    }) pipeTo replyTo onFailure {
      case e ⇒
        replyTo ! Status.Failure(ReadFailed)
        log.error(e, "Failed to mark messages read")
    }
  }

  private def privatePeerStruct(userId: Int): ApiPeer = ApiPeer(ApiPeerType.Private, userId)

}
