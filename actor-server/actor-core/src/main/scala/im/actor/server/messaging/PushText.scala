package im.actor.server.messaging

import akka.actor.ActorSystem
import im.actor.api.rpc.messaging._
import im.actor.server.group.GroupExtension
import im.actor.server.model.{ Peer, PeerType }

import scala.concurrent.Future

trait PushText {

  implicit val system: ActorSystem
  import system.dispatcher

  type PushText = String
  type CensoredPushText = String

  private val CensoredText = "New message"

  protected def getPushText(peer: Peer, outUser: Int, clientName: String, message: ApiMessage): Future[(PushText, CensoredPushText)] = {
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
      case unsupported ⇒ Future.successful(("", ""))
    }
  }

  private def formatAuthored(peer: Peer, userId: Int, authorName: String, message: String): Future[(PushText, CensoredPushText)] = {
    peer match {
      case Peer(PeerType.Group, groupId) ⇒
        for {
          group ← GroupExtension(system).getApiStruct(groupId, userId)
        } yield (s"$authorName@${group.title}: $message", s"$authorName@${group.title}: $CensoredText")
      case Peer(PeerType.Private, _) ⇒ Future.successful((s"$authorName: $message", s"$authorName: $CensoredText"))
    }
  }

}
