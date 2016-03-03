package im.actor.server.messaging

import akka.actor.ActorSystem
import im.actor.api.rpc.PeersImplicits
import im.actor.api.rpc.messaging.{ ApiMessage, UpdateMessageContentChanged }
import im.actor.api.rpc.peers.{ ApiPeer, ApiPeerType }
import im.actor.server.db.DbExtension
import im.actor.server.group.GroupExtension
import im.actor.server.model.{ Peer, PeerType }
import im.actor.server.persist.HistoryMessageRepo
import im.actor.server.sequence.SeqState
import im.actor.server.user.UserExtension

import scala.concurrent.Future

trait MessageUpdating extends PeersImplicits {

  def updateMessageContent(clientUserId: Int, peer: Peer, randomId: Long, updatedMessage: ApiMessage, date: Long = System.currentTimeMillis)(implicit system: ActorSystem): Future[SeqState] = {
    peer match {
      case Peer(PeerType.Private, _) ⇒ updateContentPrivate(clientUserId, peer, randomId, updatedMessage, date)
      case Peer(PeerType.Group, _)   ⇒ updateContentGroup(clientUserId, peer, randomId, updatedMessage, date)
    }
  }

  private def updateContentPrivate(userId: Int, peer: Peer, randomId: Long, updatedMessage: ApiMessage, date: Long)(implicit system: ActorSystem): Future[SeqState] = {
    import system.dispatcher
    for {
      // update for client himself
      seqState ← UserExtension(system).broadcastUserUpdate(
        userId = userId,
        update = UpdateMessageContentChanged(peer.asStruct, randomId, updatedMessage),
        pushText = None,
        isFat = false,
        reduceKey = None,
        deliveryId = Some(s"msgcontent_${randomId}_${date}")
      )
      // update for peer user
      _ ← UserExtension(system).broadcastUserUpdate(
        userId = peer.id,
        update = UpdateMessageContentChanged(ApiPeer(ApiPeerType.Private, userId), randomId, updatedMessage),
        pushText = None,
        isFat = false,
        reduceKey = None,
        deliveryId = Some(s"msgcontent_${randomId}_${date}")
      )
      _ ← DbExtension(system).db.run(HistoryMessageRepo.updateContentAll(
        userIds = Set(userId, peer.id),
        randomId = randomId,
        peerType = PeerType.Private,
        peerIds = Set(userId, peer.id),
        messageContentHeader = updatedMessage.header,
        messageContentData = updatedMessage.toByteArray
      ))
    } yield seqState
  }

  private def updateContentGroup(userId: Int, peer: Peer, randomId: Long, updatedMessage: ApiMessage, date: Long)(implicit system: ActorSystem): Future[SeqState] = {
    import system.dispatcher
    val upd = UpdateMessageContentChanged(peer.asStruct, randomId, updatedMessage)
    for {
      // update for client user
      seqState ← UserExtension(system).broadcastUserUpdate(
        userId = userId,
        update = upd,
        pushText = None,
        isFat = false,
        reduceKey = None,
        deliveryId = Some(s"msgcontent_${randomId}_${date}")
      )
      (memberIds, _, _) ← GroupExtension(system).getMemberIds(peer.id)
      membersSet = memberIds.toSet
      // update for other group members
      _ ← UserExtension(system).broadcastUsersUpdate(
        userIds = membersSet - userId,
        update = upd,
        pushText = None,
        isFat = false,
        deliveryId = Some(s"msgcontent_${randomId}_${date}")
      )
      _ ← DbExtension(system).db.run(HistoryMessageRepo.updateContentAll(
        userIds = membersSet + userId,
        randomId = randomId,
        peerType = PeerType.Group,
        peerIds = Set(peer.id),
        messageContentHeader = updatedMessage.header,
        messageContentData = updatedMessage.toByteArray
      ))
    } yield seqState
  }

}
