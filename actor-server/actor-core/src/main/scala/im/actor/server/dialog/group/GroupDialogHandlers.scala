package im.actor.server.dialog.group

import akka.actor.Status
import akka.pattern.pipe
import com.google.protobuf.ByteString
import im.actor.api.rpc.messaging.{ ApiMessage, UpdateMessageRead, UpdateMessageReadByMe, UpdateMessageReceived }
import im.actor.server.dialog._
import im.actor.server.group.GroupErrors.NotAMember
import HistoryUtils._
import im.actor.server.misc.UpdateCounters
import im.actor.server.model
import im.actor.server.model.{ Dialog, PeerType, Peer }
import im.actor.server.persist.DialogRepo
import im.actor.server.sequence.{ PushRules, SeqState, SeqStateDate }
import im.actor.util.cache.CacheHelpers._
import org.joda.time.DateTime

import scala.concurrent.Future

trait GroupDialogHandlers extends UpdateCounters {
  this: GroupDialog ⇒

  import DialogCommands._
  import GroupDialogEvents._

  protected def sendMessage(
    state:         GroupDialogState,
    senderUserId:  Int,
    senderAuthSid: Int,
    randomId:      Long,
    message:       ApiMessage,
    isFat:         Boolean
  ): Unit = {
    deferStashingReply(LastSenderIdChanged(senderUserId), state) { e ⇒
      withMemberIds(groupId) { (memberIds, _, optBot) ⇒
        if ((memberIds contains senderUserId) || optBot.contains(senderUserId)) {
          withCachedFuture[AuthSidRandomId, SeqStateDate](senderAuthSid → randomId) {
            val date = new DateTime
            val dateMillis = date.getMillis
            for {
              _ ← Future.sequence(memberIds.filterNot(_ == senderUserId) map { userId ⇒
                delivery.receiverDelivery(userId, senderUserId, groupPeer, randomId, dateMillis, message, isFat)
              })
              SeqState(seq, state) ← if (optBot.contains(senderUserId)) {
                Future.successful(SeqState(0, ByteString.EMPTY))
              } else {
                delivery.senderDelivery(senderUserId, senderAuthSid, groupPeer, randomId, dateMillis, message, isFat)
              }
              _ ← db.run(writeHistoryMessage(model.Peer(PeerType.Private, senderUserId), model.Peer(PeerType.Group, groupPeer.id), date, randomId, message.header, message.toByteArray))
            } yield SeqStateDate(seq, state, dateMillis)
          } recover {
            case e ⇒
              log.error(e, "Failed to send message")
              throw e
          }
        } else Future.successful(Status.Failure(NotAMember))
      }
    }
  }

  protected def writeMessage(
    state:        GroupDialogState,
    senderUserId: Int,
    dateMillis:   Long,
    randomId:     Long,
    message:      ApiMessage
  ): Unit = {
    val date = new DateTime(dateMillis)

    db.run(writeHistoryMessage(
      model.Peer(PeerType.Private, senderUserId),
      model.Peer(PeerType.Group, groupPeer.id),
      date,
      randomId,
      message.header,
      message.toByteArray
    )) map (_ ⇒ WriteMessageAck()) pipeTo sender()
  }

  protected def messageReceived(state: GroupDialogState, receiverUserId: Int, date: Long): Unit = {
    val replyTo = sender()

    (if (!state.lastReceiveDate.exists(_ >= date) && !state.lastSenderId.contains(receiverUserId)) {
      context become working(updatedState(LastReceiveDateChanged(date), state))

      withMemberIds(groupId) { (memberIds, _, _) ⇒

        val now = System.currentTimeMillis
        val update = UpdateMessageReceived(groupPeer, date, now)

        for {
          _ ← db.run(markMessagesReceived(model.Peer(PeerType.Private, receiverUserId), model.Peer(PeerType.Group, groupId), new DateTime(date)))
          _ ← seqUpdatesExt.broadcastSingleUpdate(memberIds.filterNot(_ == receiverUserId), update)
        } yield MessageReceivedAck()
      }
    } else Future.successful(MessageReceivedAck())) pipeTo replyTo onFailure {
      case e ⇒
        log.error(e, "Failed to mark messages received")
    }
  }

  protected def createForUser(state: GroupDialogState, userId: Int): Unit = {
    def doCreate(): Unit = {
      (for {
        created ← db.run(DialogRepo.createIfNotExists(Dialog(userId, Peer(PeerType.Group, groupId))))
        _ ← if (created) userExt.notifyDialogsChanged(userId) else Future.successful(())
      } yield ()) pipeTo self
    }

    val replyTo = sender()

    doCreate()

    context become {
      case () ⇒
        replyTo ! CreateForUserAck()
        unstashAll()
        context.unbecome()
      case Status.Failure(e) ⇒
        log.error(e, "Failed to create dialog for user {}", userId)
        doCreate()
      case msg ⇒ stash()
    }
  }

  protected def messageRead(state: GroupDialogState, readerUserId: Int, readerAuthSid: Int, date: Long): Unit = {
    val replyTo = sender()
    val withMembers = withMemberIds[Unit](groupId) _

    val readerUpdatesF: Future[Unit] =
      withMembers { (memberIds, _, _) ⇒
        if (memberIds contains readerUserId) {
          for {
            _ ← db.run(markMessagesRead(model.Peer(PeerType.Private, readerUserId), model.Peer(PeerType.Group, groupId), new DateTime(date)))
            _ ← seqUpdatesExt.deliverSingleUpdate(
              userId = readerUserId,
              update = UpdateMessageReadByMe(groupPeer, date),
              pushRules = PushRules(excludeAuthSids = Seq(readerAuthSid))
            )
            counterUpdate ← db.run(getUpdateCountersChanged(readerUserId))
            _ ← seqUpdatesExt.deliverSingleUpdate(readerUserId, counterUpdate)
          } yield ()
        } else Future.successful(())
      }

    val joinerF: Future[Unit] = withMembers { (_, invitedUserIds, _) ⇒
      if (invitedUserIds contains readerUserId) {
        groupExt.joinAfterFirstRead(groupId, readerUserId, readerAuthSid)
      } else Future.successful(())
    }

    val readerAckF: Future[Unit] = if (!state.lastSenderId.contains(readerUserId) && !state.lastReadDate.exists(_ >= date)) {
      //state changes before we assure that message read by group member
      //When kicked user tries to read messages, we change state, but don't mark message read.
      //When group member tries to read messages, after kicked user tried to read messages, but before new messages arrived we don't let him to read it.
      // **This behaviour is buggy and should be fixed after we implement subscription on group members**
      //It's not critical, as leave/kick is not frequent event in group.
      context become working(updatedState(LastReadDateChanged(date), state))

      withMembers { (memberIds, _, _) ⇒
        if (memberIds contains readerUserId) {
          val now = new DateTime().getMillis
          val restMembers = memberIds.filterNot(_ == readerUserId)

          for {
            _ ← db.run(markMessagesRead(model.Peer(PeerType.Private, readerUserId), model.Peer(PeerType.Group, groupId), new DateTime(date)))
            _ ← seqUpdatesExt.broadcastSingleUpdate(restMembers, UpdateMessageRead(groupPeer, date, now))
          } yield ()
        } else Future.successful(())
      }
    } else Future.successful(())

    (for {
      _ ← readerUpdatesF
      _ ← joinerF
      _ ← readerAckF
    } yield MessageReadAck()) pipeTo replyTo onFailure {
      case e ⇒
        log.error(e, "Failed to mark messages read")
    }
  }

  protected def withMemberIds[T](groupId: Int)(f: (Set[Int], Set[Int], Option[Int]) ⇒ Future[T]): Future[T] = {
    groupExt.getMemberIds(groupId) flatMap {
      case (memberIds, invitedUserIds, optBot) ⇒
        f(memberIds.toSet, invitedUserIds.toSet, optBot)
    }
  }

}
