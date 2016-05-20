package im.actor.server.group

import akka.actor.Status
import akka.pattern.pipe
import im.actor.api.rpc.PeersImplicits
import im.actor.server.dialog.DialogCommands._
import im.actor.server.group.GroupErrors.NotAMember
import im.actor.server.model.Peer

import scala.concurrent.Future

trait GroupPeerCommandHandlers extends PeersImplicits {
  this: GroupPeer ⇒

  import GroupPeerEvents._

  protected def incomingMessage(state: GroupPeerState, sm: SendMessage): Unit = {
    val senderUserId = sm.getOrigin.id
    (withMemberIds(groupId) { (memberIds, _, optBot) ⇒
      if (canSend(memberIds, optBot, senderUserId)) {
        val receiverIds = sm.forUserId match {
          case Some(id) if memberIds.contains(id.value) ⇒ Seq(id.value)
          case _                                        ⇒ memberIds - senderUserId
        }

        for {
          _ ← Future.traverse(receiverIds) { userId ⇒
            dialogExt.ackSendMessage(Peer.privat(userId), sm)
          }
        } yield {
          self ! LastSenderIdChanged(senderUserId)
          SendMessageAck()
        }
      } else Future.successful(Status.Failure(NotAMember))
    } recover {
      case e ⇒
        log.error(e, "Failed to send message")
        throw e
    }) pipeTo sender()
  }

  protected def messageReceived(state: GroupPeerState, mr: MessageReceived) = {
    val receiverUserId = mr.getOrigin.id
    val canReceive = canMakeReceive(state, mr)
    ((if (canReceive) {
      withMemberIds(groupId) { (memberIds, _, _) ⇒
        Future.traverse(memberIds - receiverUserId) { memberId ⇒
          dialogExt.ackMessageReceived(Peer.privat(memberId), mr)
        }
      } map (_ ⇒ MessageReceivedAck())
    } else Future.successful(MessageReceivedAck())) recover {
      case e ⇒
        log.error(e, "Failed to mark messages received")
        throw e
    }) pipeTo sender()
    if (canReceive) {
      updateReceiveDate(state, mr.date)
    }
  }

  protected def messageRead(state: GroupPeerState, mr: MessageRead) = {
    val withMembers = withMemberIds[Unit](groupId) _
    val readerUserId = mr.getOrigin.id

    withMembers { (_, invitedUserIds, _) ⇒
      if (invitedUserIds contains readerUserId) {
        groupExt.joinAfterFirstRead(groupId, readerUserId, mr.readerAuthSid)
      } else Future.successful(())
    }

    val canRead = canMakeRead(state, mr)
    (if (canRead) {
      withMembers { (memberIds, _, _) ⇒
        if (memberIds contains readerUserId) {
          Future.traverse(memberIds - readerUserId) { memberId ⇒
            dialogExt.ackMessageRead(Peer.privat(memberId), mr)
          } map (_ ⇒ ())
        } else Future.successful(())
      }
    } else Future.successful(())) map { _ ⇒ MessageReadAck() } pipeTo sender() recover {
      case e ⇒
        log.error(e, "Failed to mark messages read")
        throw e
    }

    //this assumption is not totally right.
    //When not member reads dialog - we will still update read date anyway.
    //it can be easily fixed after we store updated members list in GroupPeer.
    if (canRead) {
      updateReadDate(state, mr.date)
    }
  }

  protected def setReaction(state: GroupPeerState, sr: SetReaction): Unit = {
    withMemberIds(groupId) { (memberIds, _, _) ⇒
      Future.traverse(memberIds - sr.getOrigin.id) { memberId ⇒
        dialogExt.ackSetReaction(Peer.privat(memberId), sr)
      }
    } map (_ ⇒ SetReactionAck()) pipeTo sender()
  }

  protected def removeReaction(state: GroupPeerState, sr: RemoveReaction): Unit = {
    withMemberIds(groupId) { (memberIds, _, _) ⇒
      Future.traverse(memberIds - sr.getOrigin.id) { memberId ⇒
        dialogExt.ackRemoveReaction(Peer.privat(memberId), sr)
      }
    } map (_ ⇒ RemoveReactionAck()) pipeTo sender()
  }

  protected def withMemberIds[T](groupId: Int)(f: (Set[Int], Set[Int], Option[Int]) ⇒ Future[T]): Future[T] = {
    groupExt.getMemberIds(groupId) flatMap {
      case (memberIds, invitedUserIds, optBot) ⇒
        f(memberIds.toSet, invitedUserIds.toSet, optBot)
    }
  }

  private def updateReceiveDate(state: GroupPeerState, date: Long): Unit =
    context become initialized(state.updated(LastReceiveDateChanged(date)))

  private def updateReadDate(state: GroupPeerState, date: Long): Unit =
    context become initialized(state.updated(LastReadDateChanged(date)))

  private def canSend(memberIds: Set[Int], optBot: Option[Int], senderUserId: Int): Boolean =
    (memberIds contains senderUserId) || (optBot contains senderUserId)

  private def canMakeReceive(state: GroupPeerState, mr: MessageReceived): Boolean =
    (mr.date > state.lastReceiveDate) && (state.lastSenderId != mr.getOrigin.id)

  private def canMakeRead(state: GroupPeerState, mr: MessageRead): Boolean =
    (mr.date > state.lastReadDate) && (state.lastSenderId != mr.getOrigin.id)

}
