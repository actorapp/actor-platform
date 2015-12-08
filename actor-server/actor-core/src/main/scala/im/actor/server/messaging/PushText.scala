package im.actor.server.messaging

import akka.actor.ActorSystem
import im.actor.api.rpc.messaging._
import im.actor.server.group.GroupExtension
import im.actor.server.model.{ Peer, PeerType }

import scala.concurrent.Future

trait PushText {

  implicit val system: ActorSystem
  import system.dispatcher

  protected def getPushText(peer: Peer, outUser: Int, clientName: String, message: ApiMessage): Future[String] = {
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

  private def formatAuthored(peer: Peer, userId: Int, authorName: String, message: String): Future[String] = {
    peer match {
      case Peer(PeerType.Group, groupId) ⇒ GroupExtension(system).getApiStruct(groupId, userId) map (g ⇒ s"$authorName@${g.title}: $message")
      case Peer(PeerType.Private, _)     ⇒ Future.successful(s"$authorName: $message")
    }
  }

}
