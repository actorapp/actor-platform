package im.actor.server.api.rpc.service.messaging

import akka.util.Timeout
import im.actor.api.rpc.DBIOResult._
import im.actor.api.rpc._
import im.actor.api.rpc.messaging._
import im.actor.api.rpc.misc._
import im.actor.api.rpc.peers._
import im.actor.server.dialog.DialogExtension
import im.actor.server.dialog.privat.PrivateDialogErrors
import im.actor.server.group.{ GroupErrors, GroupOffice }
import im.actor.server.sequence.SeqStateDate
import im.actor.server.user.UserOffice

import scala.concurrent._
import scala.concurrent.duration._

private[messaging] trait MessagingHandlers {
  self: MessagingServiceImpl ⇒

  import im.actor.api.rpc.Implicits._

  override implicit val ec: ExecutionContext = actorSystem.dispatcher
  private implicit val timeout: Timeout = Timeout(5.seconds) // TODO: configurable

  override def jhandleSendMessage(outPeer: ApiOutPeer, randomId: Long, message: ApiMessage, clientData: ClientData): Future[HandlerResult[ResponseSeqDate]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client ⇒
      val accessHashCheck = outPeer.`type` match {
        case ApiPeerType.Private ⇒ UserOffice.checkAccessHash(outPeer.id, client.authId, outPeer.accessHash)
        case ApiPeerType.Group   ⇒ GroupOffice.checkAccessHash(outPeer.id, outPeer.accessHash)
      }
      val seqstateAction = for {
        isChecked ← fromFuture(accessHashCheck)
        _ ← fromBoolean(CommonErrors.InvalidAccessHash)(isChecked)
        result ← fromFuture(DialogExtension(actorSystem).sendMessage(
          peerType = outPeer.`type`,
          peerId = outPeer.id,
          senderUserId = client.userId,
          senderAuthId = client.authId,
          randomId = randomId,
          message = message
        ))
      } yield result

      (for (SeqStateDate(seq, state, date) ← seqstateAction) yield {
        val fromPeer = ApiPeer(ApiPeerType.Private, client.userId)
        val toPeer = outPeer.asPeer
        onMessage(Events.PeerMessage(fromPeer.asModel, toPeer.asModel, randomId, date, message))
        ResponseSeqDate(seq, state.toByteArray, date)
      }).run
    }

    db.run(toDBIOAction(authorizedAction)) recover {
      case GroupErrors.NotAMember            ⇒ Error(CommonErrors.forbidden("You are not a group member."))
      case PrivateDialogErrors.MessageToSelf ⇒ Error(CommonErrors.forbidden("Sending messages to self is not allowed."))
    }
  }
}
