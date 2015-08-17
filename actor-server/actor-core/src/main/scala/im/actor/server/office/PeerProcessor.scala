package im.actor.server.office

import akka.util.Timeout
import im.actor.api.rpc.messaging._
import im.actor.api.rpc.peers.{ Peer, PeerType }
import im.actor.server.group.{ GroupExtension, GroupOffice }

import scala.concurrent.{ ExecutionContext, Future }

trait PeerProcessor[State <: ProcessorState, Event <: AnyRef] extends Processor[State, Event] {

  private implicit val ec: ExecutionContext = context.dispatcher

  protected def getPushText(peer: Peer, outUser: Int, clientName: String, message: Message)(implicit timeout: Timeout): Future[String] = {
    message match {
      case TextMessage(text, _, _) ⇒
        formatAuthored(peer, outUser, clientName, text)
      case dm: DocumentMessage ⇒
        dm.ext match {
          case Some(_: DocumentExPhoto) ⇒
            formatAuthored(peer, outUser, clientName, "Photo")
          case Some(_: DocumentExVideo) ⇒
            formatAuthored(peer, outUser, clientName, "Video")
          case _ ⇒
            formatAuthored(peer, outUser, clientName, dm.name)
        }
      case unsupported ⇒ Future.successful("")
    }
  }

  private def formatAuthored(peer: Peer, userId: Int, authorName: String, message: String)(implicit timeout: Timeout): Future[String] = {
    implicit val viewRegion = GroupExtension(context.system).viewRegion
    peer match {
      case Peer(PeerType.Group, groupId) ⇒ GroupOffice.getApiStruct(groupId, userId) map (g ⇒ s"$authorName@${g.title}: $message")
      case Peer(PeerType.Private, _)     ⇒ Future.successful(s"$authorName: $message")
    }
  }

}