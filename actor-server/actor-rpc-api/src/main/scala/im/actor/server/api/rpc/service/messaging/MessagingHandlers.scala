package im.actor.server.api.rpc.service.messaging

import akka.http.scaladsl.util.FastFuture
import akka.util.Timeout
import cats.data.Xor
import im.actor.api.rpc.CommonRpcErrors.IntenalError
import im.actor.api.rpc.{ CommonRpcErrors, _ }
import im.actor.api.rpc.messaging._
import im.actor.api.rpc.misc._
import im.actor.api.rpc.peers._
import im.actor.config.ActorConfig
import im.actor.server.messaging.{ MessageParsing, MessageUpdating }
import im.actor.server.persist.HistoryMessageRepo

import scala.concurrent._
import scala.concurrent.duration._

object MessagingRpcErors {
  val NotLastMessage = RpcError(400, "NOT_LAST_MESSAGE", "You are trying to edit not last message.", false, None)
  val NotInTimeWindow = RpcError(400, "NOT_IN_TIME_WINDOW", "You can't edit message sent more than 5 minutes age.", false, None)
  val NotTextMessage = RpcError(400, "NOT_TEXT_MESSAGE", "You can edit only text messages.", false, None)
  val NotUniqueRandomId = RpcError(400, "RANDOM_ID_NOT_UNIQUE", "", false, None)
}

private[messaging] trait MessagingHandlers extends PeersImplicits
  with MessageParsing
  with MessageUpdating {
  this: MessagingServiceImpl ⇒

  import FutureResultRpc._
  import MessagingRpcErors._
  import PeerHelpers._

  override implicit val ec: ExecutionContext = actorSystem.dispatcher
  private implicit val timeout: Timeout = ActorConfig.defaultTimeout

  // TODO: configurable
  private val editTimeWindow: Long = 5.minutes.toMillis

  override def doHandleSendMessage(
    outPeer:         ApiOutPeer,
    randomId:        Long,
    message:         ApiMessage,
    isOnlyForUser:   Option[Int],
    quotedReference: Option[ApiMessageOutReference],
    clientData:      ClientData
  ): Future[HandlerResult[ResponseSeqDate]] =
    authorized(clientData) { implicit client ⇒
      (for (
        s ← fromFuture(dialogExt.sendMessage(
          peer = outPeer.asPeer,
          senderUserId = client.userId,
          senderAuthSid = client.authSid,
          senderAuthId = Some(client.authId),
          randomId = randomId,
          message = message,
          accessHash = Some(outPeer.accessHash),
          forUserId = isOnlyForUser
        ))
      ) yield ResponseSeqDate(s.seq, s.state.toByteArray, s.date)).value
    }

  override def doHandleNotifyDialogOpened(peer: ApiOutPeer, clientData: ClientData): Future[HandlerResult[ResponseVoid]] =
    FastFuture.failed(new RuntimeException("Not implemented"))

  override def doHandleUpdateMessage(outPeer: ApiOutPeer, randomId: Long, updatedMessage: ApiMessage, clientData: ClientData): Future[HandlerResult[ResponseSeqDate]] = {
    authorized(clientData) { implicit client ⇒
      withOutPeer(outPeer) {
        val peer = outPeer.asModel
        (for {
          histMessage ← fromFutureOption(CommonRpcErrors.forbidden("Not allowed"))(db.run(HistoryMessageRepo.findNewestSentBy(client.userId, peer)))
          _ ← fromBoolean(NotLastMessage)(histMessage.randomId == randomId)
          _ ← fromBoolean(NotInTimeWindow)(inTimeWindow(histMessage.date.getMillis))
          apiMessage ← fromXor((e: Any) ⇒ IntenalError)(Xor.fromEither(parseMessage(histMessage.messageContentData)))
          _ ← fromBoolean(NotTextMessage)(apiMessage match {
            case _: ApiTextMessage ⇒ true
            case _                 ⇒ false
          })
          result ← fromFuture(updateMessageContent(client.userId, peer, randomId, updatedMessage))
          date = System.currentTimeMillis
        } yield ResponseSeqDate(result.seq, result.state.toByteArray, date)).value
      }
    }
  }

  private def inTimeWindow(messageDateMillis: Long): Boolean = {
    (messageDateMillis + editTimeWindow) > System.currentTimeMillis
  }

}
