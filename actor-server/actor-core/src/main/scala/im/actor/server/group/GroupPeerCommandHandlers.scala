package im.actor.server.group

import akka.actor.Status
import akka.pattern.pipe
import im.actor.api.rpc.PeersImplicits
import im.actor.server.dialog.DialogCommands._
import im.actor.server.group.GroupErrors.NotAMember
import im.actor.server.model.Peer

import scala.concurrent.Future

trait GroupPeerCommandHandlers extends PeersImplicits {
  self: GroupPeer ⇒

  import GroupPeerEvents._

  protected def incomingMessage(state: GroupPeerState, sm: SendMessage): Unit = {
    val senderUserId = sm.origin.id
    val futureSend = withMemberIds(groupId) { (memberIds, _, optBot) ⇒
      if ((memberIds contains senderUserId) || (optBot contains senderUserId)) {
        for {
          _ ← Future.traverse(memberIds - senderUserId) { userId ⇒
            dialogExt.ackSendMessage(Peer.privat(userId), sm)
          }
        } yield SendMessageAck()
      } else Future.successful(Status.Failure(NotAMember))
    } recover {
      case e ⇒
        log.error(e, "Failed to send message")
        throw e
    }
    futureSend pipeTo sender()
    onSuccess(futureSend) { _ ⇒
      context become initialized(state.updated(LastSenderIdChanged(senderUserId)))
    }
  }

  protected def messageReceived(state: GroupPeerState, mr: MessageReceived) = {
    val receiverUserId = mr.origin.id
    val futureReceive =
      (if (!state.lastReceiveDate.exists(_ >= mr.date) && !state.lastSenderId.contains(receiverUserId)) {
        withMemberIds(groupId) { (memberIds, _, _) ⇒
          Future.traverse(memberIds.toSeq) { memberId ⇒
            dialogExt.ackMessageReceived(Peer.privat(memberId), mr)
          }
        } map (_ ⇒ MessageReceivedAck())
      } else Future.successful(MessageReceivedAck())) recover {
        case e ⇒
          log.error(e, "Failed to mark messages received")
          throw e
      }
    futureReceive pipeTo sender()
    onSuccess(futureReceive) { _ ⇒
      context become initialized(state.updated(LastReceiveDateChanged(mr.date)))
    }
  }

  protected def messageRead(state: GroupPeerState, mr: MessageRead) = {
    val withMembers = withMemberIds[Unit](groupId) _
    val readerUserId = mr.origin.id

    val joinerF: Future[Unit] = withMembers { (_, invitedUserIds, _) ⇒
      if (invitedUserIds contains readerUserId) {
        groupExt.joinAfterFirstRead(groupId, readerUserId, mr.readerAuthSid)
      } else Future.successful(())
    }

    val readerAckF: Future[Unit] = if (!state.lastSenderId.contains(readerUserId) && !state.lastReadDate.exists(_ >= mr.date)) {
      withMembers { (memberIds, _, _) ⇒
        if (memberIds contains readerUserId) {
          Future.traverse(memberIds - readerUserId) { memberId ⇒
            dialogExt.ackMessageRead(Peer.privat(memberId), mr)
          } map (_ ⇒ ())
        } else Future.successful(())
      }
    } else Future.successful(())

    val readFuture = (for {
      _ ← joinerF
      _ ← readerAckF
    } yield MessageReadAck()) recover {
      case e ⇒
        log.error(e, "Failed to mark messages read")
        throw e
    }
    readFuture pipeTo sender()
    onSuccess(readFuture) { _ ⇒
      context become initialized(state.updated(LastReadDateChanged(mr.date)))
    }
  }

  protected def withMemberIds[T](groupId: Int)(f: (Set[Int], Set[Int], Option[Int]) ⇒ Future[T]): Future[T] = {
    groupExt.getMemberIds(groupId) flatMap {
      case (memberIds, invitedUserIds, optBot) ⇒
        f(memberIds.toSet, invitedUserIds.toSet, optBot)
    }
  }

}
