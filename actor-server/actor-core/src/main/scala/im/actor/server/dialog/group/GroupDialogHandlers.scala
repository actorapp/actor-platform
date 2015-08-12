package im.actor.server.dialog.group

import akka.actor.{ ActorRef, Status }
import akka.pattern.pipe
import com.google.protobuf.ByteString
import im.actor.api.rpc.messaging.{ Message ⇒ ApiMessage, UpdateMessageRead, UpdateMessageReadByMe, UpdateMessageReceived }
import im.actor.server.dialog.Dialog.{ ReadFailed, ReceiveFailed }
import im.actor.server.dialog.GroupDialogCommands
import im.actor.server.group.GroupErrors.NotAMember
import im.actor.server.group.GroupOffice
import im.actor.server.models
import im.actor.server.push.SeqUpdatesManager._
import im.actor.server.sequence.{ SeqState, SeqStateDate }
import im.actor.server.user.UserOffice
import im.actor.server.util.GroupServiceMessages
import im.actor.server.util.HistoryUtils._
import im.actor.utils.cache.CacheHelpers._
import org.joda.time.DateTime

import scala.concurrent.Future
import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.util.{ Failure, Success }

trait GroupDialogHandlers {
  this: GroupDialog ⇒

  import GroupDialogCommands._

  protected def sendMessage(
    replyTo:      ActorRef,
    state:        GroupDialogState,
    memberIds:    Set[Int],
    botId:        Int,
    senderUserId: Int,
    senderAuthId: Long,
    randomId:     Long,
    message:      ApiMessage,
    isFat:        Boolean
  ): Unit = {
    if ((memberIds contains senderUserId) || senderUserId == botId) {
      context.become {
        case MessageSentComplete(newState) ⇒
          unstashAll()
          context become working(newState)
        case msg ⇒
          stash()
      }
      val date = new DateTime

      val sendMessageFuture = withCachedFuture[AuthIdRandomId, SeqStateDate](senderAuthId → randomId) { () ⇒

        memberIds.filterNot(_ == senderUserId) foreach { userId ⇒
          UserOffice.deliverMessage(userId, groupPeer, senderUserId, randomId, date, message, isFat)
        }

        for {
          SeqState(seq, state) ← if (senderUserId == botId) {
            Future.successful(SeqState(0, ByteString.EMPTY))
          } else {
            UserOffice.deliverOwnMessage(senderUserId, groupPeer, senderAuthId, randomId, date, message, isFat)
          }
          _ ← db.run(writeHistoryMessage(models.Peer.privat(senderUserId), models.Peer.group(groupPeer.id), date, randomId, message.header, message.toByteArray))
        } yield SeqStateDate(seq, state, date.getMillis)
      }
      sendMessageFuture onComplete {
        case Success(seqStateDate) ⇒
          replyTo ! seqStateDate
          self ! MessageSentComplete(state.copy(lastSenderId = Some(senderUserId)))
        case Failure(e) ⇒
          replyTo ! Status.Failure(e)
          log.error(e, "Failed to send message")
          self ! MessageSentComplete(state)
      }
    } else {
      replyTo ! Status.Failure(NotAMember)
    }
  }

  protected def messageReceived(replyTo: ActorRef, state: GroupDialogState, memberIds: Set[Int], receiverUserId: Int, date: Long): Unit = {
    val receiveFuture: Future[MessageReceivedAck] =
      if (!state.lastReceiveDate.exists(_ >= date) && !state.lastSenderId.contains(receiverUserId)) {
        context become working(state.copy(lastReceiveDate = Some(date)))

        val now = System.currentTimeMillis

        val update = UpdateMessageReceived(groupPeer, date, now)

        val authIdsF = Future.sequence(memberIds.filterNot(_ == receiverUserId) map UserOffice.getAuthIds) map (_.flatten.toSet)
        for {
          _ ← db.run(markMessagesReceived(models.Peer.privat(receiverUserId), models.Peer.group(groupId), new DateTime(date)))
          authIds ← authIdsF
          _ ← db.run(persistAndPushUpdates(authIds.toSet, update, None, isFat = false))
        } yield MessageReceivedAck()
      } else Future.successful(MessageReceivedAck())
    receiveFuture pipeTo replyTo onFailure {
      case e ⇒
        replyTo ! Status.Failure(ReceiveFailed)
        log.error(e, "Failed to mark messages received")
    }
  }

  protected def messageRead(replyTo: ActorRef, state: GroupDialogState, memberIds: Set[Int], invitedUserIds: Set[Int], readerUserId: Int, readerAuthId: Long, date: Long): Unit = {
    val readFuture: Future[MessageReadAck] =
      if (!state.lastSenderId.contains(readerUserId)) {
        db.run(markMessagesRead(models.Peer.privat(readerUserId), models.Peer.group(groupId), new DateTime(date))) foreach { _ ⇒
          UserOffice.getAuthIds(readerUserId) map { authIds ⇒
            val authIdsSet = authIds.toSet
            for {
              counterUpdate ← db.run(getUpdateCountersChanged(readerUserId))
              _ ← persistAndPushUpdatesF(authIdsSet, UpdateMessageReadByMe(groupPeer, date), None, isFat = false)
              _ ← persistAndPushUpdatesF(authIdsSet, counterUpdate, None, isFat = false)
            } yield ()
          }
        }

        if (invitedUserIds contains readerUserId) {
          GroupOffice.joinAfterFirstRead(groupId, readerUserId, readerAuthId)
        }

        if (!state.lastReadDate.exists(_ >= date) && !state.lastSenderId.contains(readerUserId)) {
          context become working(state.copy(lastReadDate = Some(date)))

          val now = new DateTime().getMillis
          val authIdsF = Future.sequence(memberIds.filterNot(_ == readerUserId) map UserOffice.getAuthIds) map (_.flatten.toSet)

          for {
            authIds ← authIdsF
            _ ← db.run(markMessagesRead(models.Peer.privat(readerUserId), models.Peer.group(groupId), new DateTime(date)))
            _ ← persistAndPushUpdatesF(authIds, UpdateMessageRead(groupPeer, date, now), None, isFat = false)
          } yield MessageReadAck()
        } else Future.successful(MessageReadAck())
      } else Future.successful(MessageReadAck())

    readFuture pipeTo replyTo onFailure {
      case e ⇒
        replyTo ! Status.Failure(ReadFailed)
        log.error(e, "Failed to mark messages read")
    }

  }

}
