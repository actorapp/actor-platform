package im.actor.server.messaging

import akka.actor.ActorSystem
import im.actor.api.rpc.PeersImplicits
import im.actor.api.rpc.messaging.{ ApiMessage, UpdateMessageContentChanged }
import im.actor.api.rpc.peers.{ ApiPeer, ApiPeerType }
import im.actor.server.db.DbExtension
import im.actor.server.group.GroupExtension
import im.actor.server.model.{ Peer, PeerType }
import im.actor.server.persist.HistoryMessageRepo
import im.actor.server.sequence.{ SeqState, SeqUpdatesExtension }

import scala.concurrent.Future

trait MessageUpdating extends PeersImplicits {

  def updateMessageContent(clientUserId: Int, clientAuthId: Long, peer: Peer, randomId: Long, updatedMessage: ApiMessage, date: Long = System.currentTimeMillis)(implicit system: ActorSystem): Future[SeqState] = {
    peer match {
      case Peer(PeerType.Private, _) ⇒ updateContentPrivate(clientUserId, clientAuthId, peer, randomId, updatedMessage, date)
      case Peer(PeerType.Group, _)   ⇒ updateContentGroup(clientUserId, clientAuthId, peer, randomId, updatedMessage, date)
    }
  }

  private def updateContentPrivate(userId: Int, clientAuthId: Long, peer: Peer, randomId: Long, updatedMessage: ApiMessage, date: Long)(implicit system: ActorSystem): Future[SeqState] = {
    import system.dispatcher
    val seqUpdExt = SeqUpdatesExtension(system)
    for {
      // update for client himself
      seqState ← seqUpdExt.deliverClientUpdate(
        userId = userId,
        authId = clientAuthId,
        update = UpdateMessageContentChanged(peer.asStruct, randomId, updatedMessage),
        pushRules = seqUpdExt.pushRules(isFat = false, None),
        deliveryId = s"msgcontent_${randomId}_${date}"
      )
      // update for peer user
      _ ← seqUpdExt.deliverUserUpdate(
        userId = peer.id,
        update = UpdateMessageContentChanged(ApiPeer(ApiPeerType.Private, userId), randomId, updatedMessage),
        pushRules = seqUpdExt.pushRules(isFat = false, None),
        deliveryId = s"msgcontent_${randomId}_${date}"
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

  private def updateContentGroup(
    userId:         Int,
    clientAuthId:   Long,
    groupPeer:      Peer,
    randomId:       Long,
    updatedMessage: ApiMessage,
    date:           Long
  )(implicit system: ActorSystem): Future[SeqState] = {
    import system.dispatcher
    val seqUpdExt = SeqUpdatesExtension(system)
    val update = UpdateMessageContentChanged(groupPeer.asStruct, randomId, updatedMessage)
    for {
      // update for client user
      seqState ← seqUpdExt.deliverClientUpdate(
        userId,
        clientAuthId,
        update,
        pushRules = seqUpdExt.pushRules(isFat = false, None),
        deliveryId = s"msgcontent_${randomId}_${date}"
      )
      (memberIds, _, optBotId) ← GroupExtension(system).getMemberIds(groupPeer.id)
      membersSet = (memberIds ++ optBotId.toSeq).toSet
      // update for other group members
      _ ← seqUpdExt.broadcastPeopleUpdate(
        membersSet - userId,
        update,
        pushRules = seqUpdExt.pushRules(isFat = false, None),
        deliveryId = s"msgcontent_${randomId}_${date}"
      )
      _ ← DbExtension(system).db.run(HistoryMessageRepo.updateContentAll(
        userIds = membersSet + userId,
        randomId = randomId,
        peerType = PeerType.Group,
        peerIds = Set(groupPeer.id),
        messageContentHeader = updatedMessage.header,
        messageContentData = updatedMessage.toByteArray
      ))
    } yield seqState
  }

}
