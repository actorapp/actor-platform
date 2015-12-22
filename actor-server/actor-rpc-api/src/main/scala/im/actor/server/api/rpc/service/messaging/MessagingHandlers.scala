package im.actor.server.api.rpc.service.messaging

import akka.util.Timeout
import im.actor.api.rpc.FutureResultRpc._
import im.actor.api.rpc._
import im.actor.api.rpc.messaging._
import im.actor.api.rpc.misc._
import im.actor.api.rpc.peers._
import im.actor.config.ActorConfig
import im.actor.server.dialog.DialogErrors
import im.actor.server.group.GroupErrors
import im.actor.server.sequence.SeqStateDate

import scala.concurrent._

private[messaging] trait MessagingHandlers {
  this: MessagingServiceImpl ⇒

  import im.actor.api.rpc.Implicits._

  override implicit val ec: ExecutionContext = actorSystem.dispatcher
  private implicit val timeout: Timeout = ActorConfig.defaultTimeout

  override def jhandleSendMessage(outPeer: ApiOutPeer, randomId: Long, message: ApiMessage, clientData: ClientData): Future[HandlerResult[ResponseSeqDate]] =
    authorized(clientData) { implicit client ⇒
      val accessHashCheck = outPeer.`type` match {
        case ApiPeerType.Private ⇒ userExt.checkAccessHash(outPeer.id, client.authId, outPeer.accessHash)
        case ApiPeerType.Group   ⇒ groupExt.checkAccessHash(outPeer.id, outPeer.accessHash)
      }
      val seqstateAction = for {
        isChecked ← fromFuture(accessHashCheck)
        _ ← fromBoolean(CommonErrors.InvalidAccessHash)(isChecked)
        result ← fromFuture(dialogExt.sendMessage(
          peer = outPeer.asPeer,
          senderUserId = client.userId,
          senderAuthSid = client.authSid,
          randomId = randomId,
          message = message
        ))
      } yield result

      (for (SeqStateDate(seq, state, date) ← seqstateAction)
        yield ResponseSeqDate(seq, state.toByteArray, date)).run recover {
        case GroupErrors.NotAMember     ⇒ Error(CommonErrors.forbidden("You are not a group member."))
        case DialogErrors.MessageToSelf ⇒ Error(CommonErrors.forbidden("Sending messages to self is not allowed."))
      }
    }
}
