package im.actor.server.office

import akka.util.Timeout
import im.actor.api.rpc.messaging._
import im.actor.api.rpc.peers.{ ApiPeer, ApiPeerType }
import im.actor.server.group.{ GroupExtension, GroupOffice }

import scala.concurrent.{ ExecutionContext, Future }

trait PeerProcessor[State <: ProcessorState, Event <: AnyRef] extends Processor[State, Event] {

  private implicit val ec: ExecutionContext = context.dispatcher

  protected def getPushText(peer: ApiPeer, outUser: Int, clientName: String, message: ApiMessage)(implicit timeout: Timeout): Future[String] = {
    message match {
      case ApiTextMessage(text, _, _) ⇒
        formatAuthored(peer, outUser, clientName, text)
      case dm: ApiDocumentMessage ⇒
        dm.ext match {
          case Some(_: ApiDocumentExPhoto) ⇒
            formatAuthored(peer, outUser, clientName, "Photo")
          case Some(_: ApiDocumentExVideo) ⇒
            formatAuthored(peer, outUser, clientName, "Video")
          case _ ⇒
            formatAuthored(peer, outUser, clientName, dm.name)
        }
      case unsupported ⇒ Future.successful("")
    }
  }

  private def formatAuthored(peer: ApiPeer, userId: Int, authorName: String, message: String)(implicit timeout: Timeout): Future[String] = {
    implicit val viewRegion = GroupExtension(context.system).viewRegion
    peer match {
      case ApiPeer(ApiPeerType.Group, groupId) ⇒ GroupOffice.getApiStruct(groupId, userId) map (g ⇒ s"$authorName@${g.title}: $message")
      case ApiPeer(ApiPeerType.Private, _)     ⇒ Future.successful(s"$authorName: $message")
    }
  }

}