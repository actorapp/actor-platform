package im.actor.server.dialog.privat

import akka.actor.Status
import akka.pattern.pipe

import im.actor.server.dialog.Dialog._
import im.actor.server.dialog.PrivateDialogCommands
import im.actor.server.dialog.PrivateDialogCommands.Origin.{ RIGHT, LEFT }
import im.actor.server.push.SeqUpdatesManager
import im.actor.server.util.HistoryUtils._
import im.actor.server.social.SocialManager._
import im.actor.api.rpc.messaging.{ Message ⇒ ApiMessage, UpdateMessageReadByMe, UpdateMessageRead, UpdateMessageReceived }
import im.actor.api.rpc.peers.{ PeerType, Peer }
import im.actor.server.sequence.{ SeqState, SeqStateDate }
import im.actor.server.user.UserOffice
import im.actor.utils.cache.CacheHelpers._
import im.actor.server.models
import org.joda.time.DateTime

import scala.concurrent.Future
import scala.util.{ Failure, Success }

trait PrivateDialogHandlers {
  this: PrivateDialog ⇒

  import PrivateDialog._
  import PrivateDialogCommands._

  protected def sendMessage(
    state:        State,
    origin:       Origin,
    senderAuthId: Long,
    randomId:     Long,
    message:      ApiMessage,
    isFat:        Boolean
  ): Unit = {
    val replyTo = sender()
    context become {
      case MessageSentComplete(newState) ⇒
        unstashAll()
        context become working(newState)
      case msg ⇒ stash()
    }
    val userState = state(origin)
    val date = new DateTime
    val dateMillis = date.getMillis

    val sendFuture: Future[SeqStateDate] = withCachedFuture[AuthIdRandomId, SeqStateDate](senderAuthId → randomId) { () ⇒
      for {
        _ ← Future.successful(UserOffice.deliverMessage(userState.peerId, privatePeerStruct(userState.userId), userState.userId, randomId, date, message, isFat))
        SeqState(seq, state) ← UserOffice.deliverOwnMessage(userState.userId, privatePeerStruct(userState.peerId), senderAuthId, randomId, date, message, isFat)
        _ ← Future.successful(recordRelation(userState.userId, userState.peerId))
        _ ← db.run(writeHistoryMessage(models.Peer.privat(userState.userId), models.Peer.privat(userState.peerId), date, randomId, message.header, message.toByteArray))
      } yield SeqStateDate(seq, state, dateMillis)
    }

    sendFuture onComplete {
      case Success(seqstate) ⇒
        replyTo ! seqstate
        context.self ! MessageSentComplete(updateState(state, reverse(origin), LastMessageDate(dateMillis)))
      case Failure(e) ⇒
        replyTo ! Status.Failure(e)
        log.error(e, "Failed to send message")
        context.self ! MessageSentComplete(state)
    }
  }

  protected def messageReceived(state: State, origin: Origin, date: Long): Unit = {
    val userState = state(origin)
    val receiveFuture = if (!userState.lastReceiveDate.exists(_ >= date) &&
      !(date > System.currentTimeMillis()) &&
      (userState.lastMessageDate.isEmpty || userState.lastMessageDate.exists(_ >= date))) {
      context become working(updateState(state, origin, LastReceiveDate(date)))

      val now = System.currentTimeMillis
      val update = UpdateMessageReceived(privatePeerStruct(userState.userId), date, now)
      for {
        _ ← UserOffice.broadcastUserUpdate(userState.peerId, update, None, isFat = false)
        _ ← db.run(markMessagesReceived(models.Peer.privat(userState.userId), models.Peer.privat(userState.peerId), new DateTime(date)))
      } yield MessageReceivedAck()
    } else {
      Future.successful(MessageReceivedAck())
    }

    val replyTo = sender()
    receiveFuture pipeTo replyTo onFailure {
      case e ⇒
        replyTo ! Status.Failure(ReceiveFailed)
        log.error(e, "Failed to mark messages received")
    }
  }

  protected def messageRead(state: State, origin: Origin, readerAuthId: Long, date: Long): Unit = {
    val userState = state(origin)
    val readFuture = if (!userState.lastReadDate.exists(_ >= date) &&
      !(date > System.currentTimeMillis()) &&
      (userState.lastMessageDate.isEmpty || userState.lastMessageDate.exists(_ >= date))) {
      context become working(updateState(state, origin, LastReadDate(date)))

      val now = System.currentTimeMillis
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
    }

    val replyTo = sender()
    readFuture pipeTo replyTo onFailure {
      case e ⇒
        replyTo ! Status.Failure(ReadFailed)
        log.error(e, "Failed to mark messages read")
    }
  }

  private def privatePeerStruct(userId: Int): Peer = Peer(PeerType.Private, userId)

  private def updateState(state: State, origin: Origin, change: StateChange): State = {
    val userState = state(origin)
    change match {
      case LastMessageDate(date) ⇒ state.updated(origin, userState.copy(lastMessageDate = Some(date)))
      case LastReceiveDate(date) ⇒ state.updated(origin, userState.copy(lastReceiveDate = Some(date)))
      case LastReadDate(date)    ⇒ state.updated(origin, userState.copy(lastReadDate = Some(date)))
    }
  }

  private def reverse: PartialFunction[Origin, Origin] = {
    case LEFT  ⇒ RIGHT
    case RIGHT ⇒ LEFT
  }

}
