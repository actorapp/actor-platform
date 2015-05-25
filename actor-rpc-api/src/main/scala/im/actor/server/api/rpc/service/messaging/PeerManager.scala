package im.actor.server.api.rpc.service.messaging

import scala.concurrent.ExecutionContext

import akka.actor.{ Actor, ActorLogging }
import slick.dbio.DBIO

import im.actor.api.rpc.messaging._
import im.actor.api.rpc.peers.{ Peer, PeerType }
import im.actor.server.models
import im.actor.server.util.ContactsUtils

private[messaging] trait PeerManager extends Actor with ActorLogging {
  import ContactsUtils._

  implicit private val ec: ExecutionContext = context.dispatcher

  protected def getPushText(message: Message, clientUser: models.User, outUser: Int) = {
    message match {
      case TextMessage(text, _, _) ⇒
        for (localName ← getLocalNameOrDefault(outUser, clientUser))
          yield formatAuthored(localName, text)
      case dm: DocumentMessage ⇒
        getLocalNameOrDefault(outUser, clientUser) map { localName ⇒
          dm.ext match {
            case Some(_: DocumentExPhoto) ⇒
              formatAuthored(localName, "Photo")
            case Some(_: DocumentExVideo) ⇒
              formatAuthored(localName, "Video")
            case _ ⇒
              formatAuthored(localName, dm.name)
          }
        }
      case unsupported ⇒
        log.error("Unsupported message content {}", unsupported)
        DBIO.successful("")
    }
  }

  protected def formatAuthored(authorName: String, message: String): String = s"${authorName}: ${message}"

  protected def privatePeerStruct(userId: Int): Peer = Peer(PeerType.Private, userId)

  protected def groupPeerStruct(groupId: Int): Peer = Peer(PeerType.Group, groupId)
}