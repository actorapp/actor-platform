package im.actor.server.persist.dialog

import im.actor.server.model.{ PeerType, Peer }
import im.actor.server.db.ActorPostgresDriver.api._

trait DialogId {

  def getDialogId(optUserId: Option[Int], peer: Peer): String = (optUserId, peer) match {
    case (Some(userId), Peer(PeerType.Private, peerUserId)) ⇒
      val userIds = if (userId < peerUserId) s"${userId}_${peerUserId}" else s"${peerUserId}_${userId}"
      s"${peer.`type`.value}_$userIds"
    case (_, Peer(PeerType.Group, groupId)) ⇒
      s"${peer.`type`.value}_$groupId"
    case _ ⇒ throw new RuntimeException(s"invalid params for dialog id passed, optUserId: ${optUserId}, peer: ${peer}")
  }

  def repDialogId(userId: Rep[Int], peerId: Rep[Int], peerType: Rep[Int]) = {
    Case If (peerType === PeerType.Private.value) Then
      repPrivateDialogId(userId, peerId, peerType) Else
      peerType.asColumnOf[String] ++ "_" ++ peerId.asColumnOf[String]
  }

  private def repPrivateDialogId(userId: Rep[Int], peerId: Rep[Int], peerType: Rep[Int]): Rep[String] = {
    Case If (userId < peerId) Then
      peerType.asColumnOf[String] ++ "_" ++ userId.asColumnOf[String] ++ "_" ++ peerId.asColumnOf[String] Else
      peerType.asColumnOf[String] ++ "_" ++ peerId.asColumnOf[String] ++ "_" ++ userId.asColumnOf[String]
  }

}
