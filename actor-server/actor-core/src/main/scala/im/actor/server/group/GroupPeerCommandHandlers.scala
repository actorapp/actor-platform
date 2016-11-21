package im.actor.server.group

import akka.http.scaladsl.util.FastFuture
import akka.pattern.pipe
import im.actor.api.rpc.PeersImplicits
import im.actor.server.dialog.DialogCommands._
import im.actor.server.group.GroupErrors.{ NotAMember, NotAdmin }
import im.actor.server.model.Peer

import scala.concurrent.Future

trait GroupPeerCommandHandlers extends PeersImplicits {
  this: GroupPeer ⇒

  import GroupPeerEvents._

  protected def incomingMessage(state: GroupPeerState, sm: SendMessage): Unit = {
    val senderUserId = sm.getOrigin.id

    val result: Future[SendMessageAck] = groupExt.canSendMessage(groupId, senderUserId) flatMap {
      case CanSendMessageInfo(true, isChannel, memberIds, optBotId) ⇒
        val (ack, smUpdated) = if (isChannel) {
          optBotId map { botId ⇒
            val botPeer = Peer.privat(botId)
            SendMessageAck().withUpdatedSender(botPeer) → sm.withOrigin(botPeer)
          } getOrElse (SendMessageAck() → sm)
        } else {
          SendMessageAck() → sm
        }

        val receiverIds = sm.forUserId match {
          case Some(id) if memberIds.contains(id) ⇒ Seq(id)
          case _                                  ⇒ memberIds - senderUserId
        }

        for {
          _ ← Future.traverse(receiverIds) { userId ⇒
            dialogExt.ackSendMessage(Peer.privat(userId), smUpdated)
          }
        } yield {
          self ! LastSenderIdChanged(senderUserId)
          ack
        }
      case CanSendMessageInfo(false, true, _, _) ⇒
        FastFuture.failed(NotAdmin)
      case CanSendMessageInfo(false, _, _, _) ⇒
        FastFuture.failed(NotAMember)
    }

    result onFailure {
      case e: Exception ⇒ log.error(e, "Failed to send message")
    }

    result pipeTo sender()
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
    } else FastFuture.successful(MessageReceivedAck())) recover {
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
      joinAfterFirstRead(invitedUserIds, readerUserId, mr.readerAuthId)
    }

    val canRead = canMakeRead(state, mr)
    (if (canRead) {
      withMembers { (memberIds, _, _) ⇒
        if (memberIds contains readerUserId) {
          Future.traverse(memberIds - readerUserId) { memberId ⇒
            dialogExt.ackMessageRead(Peer.privat(memberId), mr)
          } map (_ ⇒ ())
        } else FastFuture.successful(())
      }
    } else FastFuture.successful(())) map { _ ⇒ MessageReadAck() } pipeTo sender() recover {
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

  private def joinAfterFirstRead(invitedUserIds: Set[Int], readerUserId: Int, readerAuthId: Long): Future[Unit] = {
    if (invitedUserIds contains readerUserId) {
      groupExt.joinGroup(groupId, readerUserId, readerAuthId, None) map (_ ⇒ ())
    } else FastFuture.successful(())
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
