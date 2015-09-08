package im.actor.server.api.rpc.service.messaging

import im.actor.api.rpc._
import im.actor.server.dialog.group.GroupDialogOperations
import im.actor.server.dialog.privat.{ PrivateDialogErrors, PrivateDialogOperations }
import im.actor.server.sequence.SeqStateDate

import scala.concurrent._
import scala.concurrent.duration._

import akka.util.Timeout
import DBIOResult._

import im.actor.api.rpc._
import im.actor.api.rpc.messaging._
import im.actor.api.rpc.misc._
import im.actor.api.rpc.peers._
import im.actor.server.group.{ GroupErrors, GroupOffice }
import im.actor.server.user.UserOffice

private[messaging] trait MessagingHandlers {
  self: MessagingServiceImpl ⇒

  import im.actor.api.rpc.Implicits._

  override implicit val ec: ExecutionContext = actorSystem.dispatcher
  private implicit val timeout: Timeout = Timeout(5.seconds) // TODO: configurable

  override def jhandleSendMessage(outPeer: ApiOutPeer, randomId: Long, message: ApiMessage, clientData: ClientData): Future[HandlerResult[ResponseSeqDate]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client ⇒
      val seqstateAction = outPeer.`type` match {
        case ApiPeerType.Private ⇒
          for {
            isChecked ← fromFuture(UserOffice.checkAccessHash(outPeer.id, client.authId, outPeer.accessHash))
            _ ← fromBoolean(CommonErrors.InvalidAccessHash)(isChecked)
            result ← fromFuture(PrivateDialogOperations.sendMessage(outPeer.id, client.userId, client.authId, randomId, message))
          } yield result
        case ApiPeerType.Group ⇒
          for {
            isChecked ← fromFuture(GroupOffice.checkAccessHash(outPeer.id, outPeer.accessHash))
            _ ← fromBoolean(CommonErrors.InvalidAccessHash)(isChecked)
            result ← fromFuture(GroupDialogOperations.sendMessage(outPeer.id, client.userId, client.authId, randomId, message))
          } yield result
      }

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
