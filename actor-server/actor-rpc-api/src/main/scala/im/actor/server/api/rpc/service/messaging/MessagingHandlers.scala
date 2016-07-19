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
import im.actor.server.group.CanSendMessageInfo
import im.actor.server.messaging.{ MessageParsing, MessageUpdating }
import im.actor.server.model.{ Peer, PeerType }
import im.actor.server.persist.HistoryMessageRepo

import scala.concurrent._
import scala.concurrent.duration._

object MessagingRpcErors {
  val NotInTimeWindow = RpcError(400, "NOT_IN_TIME_WINDOW", "You can't edit message sent more than 5 minutes age.", false, None)
  val NotTextMessage = RpcError(400, "NOT_TEXT_MESSAGE", "You can edit only text messages.", false, None)
  val NotUniqueRandomId = RpcError(400, "RANDOM_ID_NOT_UNIQUE", "", false, None)
  val NotAllowedToEdit = CommonRpcErrors.forbidden("You are not allowed to edit this message")
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
  private val editTimeWindow: Long = 1.hour.toMillis

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
          senderAuthId = client.authId,
          randomId = randomId,
          message = message,
          accessHash = outPeer.accessHash,
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
          histMessage ← fromFutureOption(NotAllowedToEdit)(getEditableHistoryMessage(peer, randomId))
          _ ← fromBoolean(NotInTimeWindow)(inTimeWindow(histMessage.date.getMillis))
          apiMessage ← fromXor((e: Any) ⇒ IntenalError)(Xor.fromEither(parseMessage(histMessage.messageContentData)))
          _ ← fromBoolean(NotTextMessage)(apiMessage match {
            case _: ApiTextMessage ⇒ true
            case _                 ⇒ false
          })
          s ← fromFuture(updateMessageContent(client.userId, client.authId, peer, randomId, updatedMessage))
          date = System.currentTimeMillis
        } yield ResponseSeqDate(s.seq, s.state.toByteArray, date)).value
      }
    }
  }

  private def getEditableHistoryMessage(peer: Peer, randomId: Long)(implicit client: AuthorizedClientData) = {
    def findBySender(senderId: Int) = db.run(HistoryMessageRepo.findBySender(senderId, peer, randomId).headOption)

    for {
      optMessage ← peer match {
        case Peer(PeerType.Private, _) ⇒
          findBySender(client.userId)
        case Peer(PeerType.Group, groupId) ⇒
          for {
            CanSendMessageInfo(canSend, isChannel, _, optBotId) ← groupExt.canSendMessage(groupId, client.userId)
            mess ← (isChannel, canSend) match {
              case (true, true) ⇒ // channel, client user is one of those who can send messages, thus he can also edit message.
                (optBotId map findBySender) getOrElse FastFuture.successful(None)
              case (true, false) ⇒ // channel, client user can't send messages, thus he can't edit message.
                FastFuture.successful(None)
              case (false, _) ⇒ // not a channel group. regular, as in case of private peer
                findBySender(client.userId)
            }
          } yield mess
      }
    } yield optMessage
  }

  private def inTimeWindow(messageDateMillis: Long): Boolean = {
    (messageDateMillis + editTimeWindow) > System.currentTimeMillis
  }

}
