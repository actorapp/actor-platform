package im.actor.server.dialog.group

import akka.actor.Status
import akka.pattern.pipe
import com.google.protobuf.ByteString
import im.actor.api.rpc.messaging.{ Message ⇒ ApiMessage, UpdateMessageRead, UpdateMessageReadByMe, UpdateMessageReceived }
import im.actor.server.dialog.{ AuthIdRandomId, GroupDialogCommands, ReadFailed, ReceiveFailed }
import im.actor.server.group.GroupErrors.NotAMember
import im.actor.server.group.GroupOffice
import im.actor.server.misc.UpdateCounters
import im.actor.server.models
import im.actor.server.sequence.SeqUpdatesManager._
import im.actor.server.sequence.{ SeqState, SeqStateDate }
import im.actor.server.user.UserOffice
import im.actor.server.util.HistoryUtils._
import im.actor.utils.cache.CacheHelpers._
import org.joda.time.DateTime

import scala.concurrent.Future

trait GroupDialogHandlers extends UpdateCounters {
  this: GroupDialog ⇒

  import GroupDialogCommands._
  import GroupDialogEvents._

  protected def sendMessage(
    state:        GroupDialogState,
    senderUserId: Int,
    senderAuthId: Long,
    randomId:     Long,
    message:      ApiMessage,
    isFat:        Boolean
  ): Unit = {
    deferStashingReply(LastSenderIdChanged(senderUserId), state) { e ⇒
      withMemberIds(groupId) { (memberIds, _, botId) ⇒
        if ((memberIds contains senderUserId) || senderUserId == botId) {
          withCachedFuture[AuthIdRandomId, SeqStateDate](senderAuthId → randomId) {
            val date = new DateTime
            for {
              _ ← Future.sequence(memberIds.filterNot(_ == senderUserId) map { userId ⇒
                UserOffice.deliverMessage(userId, groupPeer, senderUserId, randomId, date, message, isFat)
              })
              SeqState(seq, state) ← if (senderUserId == botId) {
                Future.successful(SeqState(0, ByteString.EMPTY))
              } else {
                UserOffice.deliverOwnMessage(senderUserId, groupPeer, senderAuthId, randomId, date, message, isFat)
              }
              _ ← db.run(writeHistoryMessage(models.Peer.privat(senderUserId), models.Peer.group(groupPeer.id), date, randomId, message.header, message.toByteArray))
            } yield SeqStateDate(seq, state, date.getMillis)
          } recover {
            case e ⇒
              log.error(e, "Failed to send message")
              throw e
          }
        } else Future.successful(Status.Failure(NotAMember))
      }
    }
  }

  protected def messageReceived(state: GroupDialogState, receiverUserId: Int, date: Long): Unit = {
    val replyTo = sender()

    (if (!state.lastReceiveDate.exists(_ >= date) && !state.lastSenderId.contains(receiverUserId)) {
      context become working(updatedState(LastReceiveDateChanged(date), state))

      withMemberIds(groupId) { (memberIds, _, _) ⇒

        val now = System.currentTimeMillis
        val update = UpdateMessageReceived(groupPeer, date, now)

        val authIdsF = Future.sequence(memberIds.filterNot(_ == receiverUserId) map UserOffice.getAuthIds) map (_.flatten.toSet)
        for {
          _ ← db.run(markMessagesReceived(models.Peer.privat(receiverUserId), models.Peer.group(groupId), new DateTime(date)))
          authIds ← authIdsF
          _ ← db.run(persistAndPushUpdates(authIds.toSet, update, None, isFat = false))
        } yield MessageReceivedAck()
      }
    } else Future.successful(MessageReceivedAck())) pipeTo replyTo onFailure {
      case e ⇒
        replyTo ! Status.Failure(ReceiveFailed)
        log.error(e, "Failed to mark messages received")
    }
  }

  protected def messageRead(state: GroupDialogState, readerUserId: Int, readerAuthId: Long, date: Long): Unit = {
    val replyTo = sender()
    (if (!state.lastSenderId.contains(readerUserId)) {
      context become working(updatedState(LastReadDateChanged(date), state))

      withMemberIds(groupId) { (memberIds, invitedUserIds, _) ⇒
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
        for {
          _ ← if (invitedUserIds contains readerUserId) {
            GroupOffice.joinAfterFirstRead(groupId, readerUserId, readerAuthId)
          } else Future.successful(())
          result ← if (!state.lastReadDate.exists(_ >= date) && !state.lastSenderId.contains(readerUserId)) {
            workWith(LastReadDateChanged(date), state)

            val now = new DateTime().getMillis
            val restMembers = memberIds.filterNot(_ == readerUserId)
            val authIdsF = Future.sequence(restMembers map UserOffice.getAuthIds) map (_.flatten.toSet)

            for {
              authIds ← authIdsF
              _ ← db.run(markMessagesRead(models.Peer.privat(readerUserId), models.Peer.group(groupId), new DateTime(date)))
              _ ← persistAndPushUpdatesF(authIds, UpdateMessageRead(groupPeer, date, now), None, isFat = false)
            } yield MessageReadAck()

          } else Future.successful(MessageReadAck())
        } yield result
      }
    } else Future.successful(MessageReadAck())) pipeTo replyTo onFailure {
      case e ⇒
        replyTo ! Status.Failure(ReadFailed)
        log.error(e, "Failed to mark messages read")
    }
  }

  protected def withMemberIds[T](groupId: Int)(f: (Set[Int], Set[Int], Int) ⇒ Future[T]): Future[T] = {
    GroupOffice.getMemberIds(groupId) flatMap {
      case (memberIds, invitedUserIds, botId) ⇒
        f(memberIds.toSet, invitedUserIds.toSet, botId)
    }
  }

}
