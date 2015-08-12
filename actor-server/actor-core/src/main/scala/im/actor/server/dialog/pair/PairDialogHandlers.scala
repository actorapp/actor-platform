package im.actor.server.dialog.pair

import akka.actor.Status
import akka.pattern.pipe

import im.actor.server.dialog.Dialog._
import im.actor.server.dialog.PairDialogCommands
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

trait PairDialogHandlers {
  this: PairDialog ⇒

  import PairDialogCommands._

  protected def sendMessage(
    direction:    Direction,
    state:        PairDialogState,
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
    val date = new DateTime
    val dateMillis = date.getMillis

    val sendFuture: Future[SeqStateDate] = withCachedFuture[AuthIdRandomId, SeqStateDate](senderAuthId → randomId) { () ⇒
      for {
        _ ← Future.successful(UserOffice.deliverMessage(direction.to, privatePeerStruct(direction.from), direction.from, randomId, date, message, isFat))
        SeqState(seq, state) ← UserOffice.deliverOwnMessage(direction.from, privatePeerStruct(direction.to), senderAuthId, randomId, date, message, isFat)
        _ ← Future.successful(recordRelation(direction.from, direction.to))
        _ ← db.run(writeHistoryMessage(models.Peer.privat(direction.from), models.Peer.privat(direction.to), date, randomId, message.header, message.toByteArray))
      } yield SeqStateDate(seq, state, dateMillis)
    }

    sendFuture onComplete {
      case Success(seqstate) ⇒
        replyTo ! seqstate
        context.self ! MessageSentComplete(state.copy(lastMessageDate = Some(dateMillis)))
      case Failure(e) ⇒
        replyTo ! Status.Failure(e)
        log.error(e, "Failed to send message")
        context.self ! MessageSentComplete(state)
    }
  }

  //we mark messages FROM user delivered TO user
  protected def messageReceived(direction: Direction, state: PairDialogState, date: Long): Unit = {
    val userState = getUserState(direction.to, state)

    val receiveFuture = if (!userState.lastReceiveDate.exists(_ >= date) && (state.lastMessageDate.isEmpty || state.lastMessageDate.exists(_ >= date))) {
      context become working(updateLastReceiveDate(direction.to, Some(date), state))

      val now = System.currentTimeMillis
      val update = UpdateMessageReceived(privatePeerStruct(direction.to), date, now)
      for {
        _ ← UserOffice.broadcastUserUpdate(direction.from, update, None, isFat = false)
        _ ← db.run(markMessagesReceived(models.Peer.privat(direction.to), models.Peer.privat(direction.from), new DateTime(date)))
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

  //we mark messages FROM user read by TO user
  protected def messageRead(direction: Direction, state: PairDialogState, readerAuthId: Long, date: Long): Unit = {
    val userState = getUserState(direction.to, state)

    val readFuture = if (!userState.lastReadDate.exists(_ >= date) && (state.lastMessageDate.isEmpty || state.lastMessageDate.exists(_ >= date))) {
      context become working(updateLastReadDate(direction.to, Some(date), state))

      val now = System.currentTimeMillis
      val update = UpdateMessageRead(privatePeerStruct(direction.to), date, now)
      val readerUpdate = UpdateMessageReadByMe(privatePeerStruct(direction.from), date)
      for {
        _ ← UserOffice.broadcastUserUpdate(direction.from, update, None, isFat = false)
        _ ← db.run(markMessagesRead(models.Peer.privat(direction.to), models.Peer.privat(direction.from), new DateTime(date)))
        counterUpdate ← db.run(getUpdateCountersChanged(direction.to))
        _ ← UserOffice.broadcastUserUpdate(direction.to, counterUpdate, None, isFat = false)
        _ ← db.run(SeqUpdatesManager.notifyUserUpdate(direction.to, readerAuthId, readerUpdate, None, isFat = false))
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

  private def updateLastReceiveDate(userId: Int, lastReceiveDate: Option[Long], state: PairDialogState): PairDialogState =
    if (state.leftState.userId == userId) {
      state.copy(leftState = state.leftState.copy(lastReceiveDate = lastReceiveDate))
    } else {
      state.copy(rightState = state.rightState.copy(lastReceiveDate = lastReceiveDate))
    }

  private def updateLastReadDate(userId: Int, lastReadDate: Option[Long], state: PairDialogState): PairDialogState =
    if (state.leftState.userId == userId) {
      state.copy(leftState = state.leftState.copy(lastReadDate = lastReadDate))
    } else {
      state.copy(rightState = state.rightState.copy(lastReadDate = lastReadDate))
    }

  private def getUserState(userId: Int, state: PairDialogState): DialogState =
    if (state.leftState.userId == userId) state.leftState else state.rightState

}
