package im.actor.server.dialog

import akka.actor.ActorSystem
import akka.util.Timeout
import im.actor.api.rpc.peers.{ Peer, PeerType }
import im.actor.server.dialog.Origin.{ RIGHT, LEFT }
import im.actor.server.group.{ GroupExtension, GroupViewRegion, GroupOffice }

import scala.concurrent.Future

object DialogId {
  def privat(a: Int, b: Int): PrivateDialogId = {
    val (left, right) = if (a > b) (b, a) else (a, b)

    PrivateDialogId(left, right)
  }

  def group(groupId: Int): GroupDialogId = GroupDialogId(groupId)

  def fromStringId(id: String): DialogId = {
    id.split('_').toList match {
      case head :: tail if head == PeerType.Private.id.toString ⇒
        privat(tail(0).toInt, tail(1).toInt)
      case head :: tail if head == PeerType.Group.id.toString ⇒
        group(tail.head.toInt)
      case unknown ⇒ throw new Exception(s"Unknown dialogId string ${unknown}")
    }
  }

  def peer(dialogId: DialogId, clientUserId: Int): Peer = {
    dialogId match {
      case id: PrivateDialogId ⇒
        val userId = if (id.left == clientUserId) id.right else id.left
        Peer(PeerType.Private, userId)
      case id: GroupDialogId ⇒
        Peer(PeerType.Group, id.groupId)
    }
  }

  def getParticipants(dialogId: DialogId)(implicit system: ActorSystem, timeout: Timeout): Future[Seq[Int]] = {
    import system.dispatcher

    dialogId match {
      case PrivateDialogId(left, right) ⇒
        Future.successful(Seq(left, right))
      case GroupDialogId(groupId) ⇒
        implicit val groupViewRegion: GroupViewRegion = GroupExtension(system).viewRegion

        for {
          (userIds, _, _) ← GroupOffice.getMemberIds(groupId)
        } yield userIds
    }
  }
}

trait DialogId {
  def stringId: String
}

private[dialog] trait GroupDialogIdBase extends DialogId {
  def groupId: Int

  override def stringId: String = s"${PeerType.Group.id}_${groupId}"
}

private[dialog] trait PrivateDialogIdBase extends DialogId {
  require(right >= left, "Left should be >= right")
  def left: Int
  def right: Int

  def origin(senderUserId: Int): Origin =
    if (senderUserId == left) LEFT else RIGHT

  override def stringId: String = s"${PeerType.Private.id}_${left}_${right}"
}

