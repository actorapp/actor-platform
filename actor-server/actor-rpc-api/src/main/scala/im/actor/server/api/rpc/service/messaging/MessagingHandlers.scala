package im.actor.server.api.rpc.service.messaging

import akka.util.Timeout
import cats.data.Xor
import im.actor.api.rpc._
import im.actor.api.rpc.messaging._
import im.actor.api.rpc.misc._
import im.actor.api.rpc.peers._
import im.actor.config.ActorConfig
import im.actor.server.model.{ PeerType, Peer }
import im.actor.server.persist.HistoryMessageRepo
import im.actor.server.sequence.SeqState

import scala.concurrent._
import scala.concurrent.duration._

object MessagingRpcErors {
  val NotLastMessage = RpcError(400, "NOT_LAST_MESSAGE", "You are trying to edit not last message.", false, None)
  val NotInTimeWindow = RpcError(400, "NOT_IN_TIME_WINDOW", "You can't edit message sent more than 5 minutes age.", false, None)
  val NotTextMessage = RpcError(400, "NOT_TEXT_MESSAGE", "You can edit only text messages.", false, None)
  val IntenalError = RpcError(500, "INTERNAL_ERROR", "", false, None)
}

private[messaging] trait MessagingHandlers extends PeersImplicits with MessageParsing {
  this: MessagingServiceImpl ⇒

  import FutureResultRpc._
  import MessagingRpcErors._
  import PeerHelpers._

  override implicit val ec: ExecutionContext = actorSystem.dispatcher
  private implicit val timeout: Timeout = ActorConfig.defaultTimeout

  // TODO: configurable
  private val editTimeWindow: Long = 5.minutes.toMillis

  override def doHandleSendMessage(outPeer: ApiOutPeer, randomId: Long, message: ApiMessage, clientData: ClientData): Future[HandlerResult[ResponseSeqDate]] =
    authorized(clientData) { implicit client ⇒
      (for (
        s ← fromFuture(dialogExt.sendMessage(
          peer = outPeer.asPeer,
          senderUserId = client.userId,
          senderAuthSid = client.authSid,
          senderAuthId = Some(client.authId),
          randomId = randomId,
          message = message,
          accessHash = Some(outPeer.accessHash)
        ))
      ) yield ResponseSeqDate(s.seq, s.state.toByteArray, s.date)).value
    }

  override def doHandleUpdateMessage(outPeer: ApiOutPeer, randomId: Long, updatedMessage: ApiMessage, clientData: ClientData): Future[HandlerResult[ResponseSeqDate]] = {
    authorized(clientData) { implicit client ⇒
      withOutPeer(outPeer) {
        val peer = outPeer.asModel
        (for {
          histMessage ← fromFutureOption(CommonRpcErrors.forbidden("Not allowed"))(db.run(HistoryMessageRepo.findNewestSentBy(client.userId, peer)))
          _ ← fromBoolean(NotLastMessage)(histMessage.randomId == randomId)
          _ = fromBoolean(NotInTimeWindow)(inTimeWindow(histMessage.date.getMillis))
          apiMessage ← fromXor((e: Any) ⇒ IntenalError)(Xor.fromEither(parseMessage(histMessage.messageContentData)))
          _ ← fromBoolean(NotTextMessage)(apiMessage match {
            case _: ApiTextMessage ⇒ true
            case _                 ⇒ false
          })
          result ← fromFuture(updateMessageContent(peer, randomId, updatedMessage))
          date = System.currentTimeMillis
        } yield ResponseSeqDate(result.seq, result.state.toByteArray, date)).value
      }
    }
  }

  private def inTimeWindow(messageDateMillis: Long): Boolean = {
    (messageDateMillis + editTimeWindow) > System.currentTimeMillis
  }

  // TODO: move to trait and reuse
  private def updateMessageContent(peer: Peer, randomId: Long, updatedMessage: ApiMessage)(implicit client: AuthorizedClientData): Future[SeqState] = peer match {
    case Peer(PeerType.Private, _) ⇒ updateContentPrivate(client.userId, peer, randomId, updatedMessage)
    case Peer(PeerType.Group, _)   ⇒ updateContentGroup(client.userId, peer, randomId, updatedMessage)
  }

  private def updateContentPrivate(userId: Int, peer: Peer, randomId: Long, updatedMessage: ApiMessage): Future[SeqState] = {
    for {
      // update for client himself
      seqState ← userExt.broadcastUserUpdate(
        userId = userId,
        update = UpdateMessageContentChanged(peer.asStruct, randomId, updatedMessage),
        pushText = None,
        isFat = false,
        reduceKey = None,
        deliveryId = Some(s"msgcontent_$randomId")
      )
      // update for peer user
      _ ← userExt.broadcastUserUpdate(
        userId = peer.id,
        update = UpdateMessageContentChanged(ApiPeer(ApiPeerType.Private, userId), randomId, updatedMessage),
        pushText = None,
        isFat = false,
        reduceKey = None,
        deliveryId = Some(s"msgcontent_$randomId")
      )
      _ ← db.run(HistoryMessageRepo.updateContentAll(
        userIds = Set(userId, peer.id),
        randomId = randomId,
        peerType = PeerType.Private,
        peerIds = Set(userId, peer.id),
        messageContentHeader = updatedMessage.header,
        messageContentData = updatedMessage.toByteArray
      ))
    } yield seqState
  }

  private def updateContentGroup(userId: Int, peer: Peer, randomId: Long, updatedMessage: ApiMessage): Future[SeqState] = {
    val upd = UpdateMessageContentChanged(peer.asStruct, randomId, updatedMessage)
    for {
      // update for client user
      seqState ← userExt.broadcastUserUpdate(
        userId = userId,
        update = upd,
        pushText = None,
        isFat = false,
        reduceKey = None,
        deliveryId = Some(s"msgcontent_$randomId")
      )
      (memberIds, _, _) ← groupExt.getMemberIds(peer.id)
      membersSet = memberIds.toSet
      // update for other group members
      _ ← userExt.broadcastUsersUpdate(
        userIds = membersSet - userId,
        update = upd,
        pushText = None,
        isFat = false,
        deliveryId = Some(s"msgcontent_$randomId")
      )
      _ ← db.run(HistoryMessageRepo.updateContentAll(
        userIds = membersSet,
        randomId = randomId,
        peerType = PeerType.Group,
        peerIds = Set(peer.id),
        messageContentHeader = updatedMessage.header,
        messageContentData = updatedMessage.toByteArray
      ))
    } yield seqState
  }

}
