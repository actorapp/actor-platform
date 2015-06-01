package im.actor.server.api.rpc.service.messaging

import akka.actor.{ Actor, ActorLogging }
import im.actor.api.rpc.messaging.{ Message ⇒ ApiMessage, _ }
import im.actor.api.rpc.peers.{ Peer, PeerType }
import im.actor.server.models
import im.actor.server.util.ContactsUtils
import org.joda.time.DateTime
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext

object PeerManager {
  private[messaging] sealed trait Message

  private[messaging] case class Envelope(peerId: Int, payload: Message)

  private[messaging] case class SendMessage(senderUserId: Int, senderAuthId: Long, randomId: Long, date: DateTime, message: ApiMessage, isFat: Boolean = false) extends Message

  private[messaging] case class MessageReceived(receiverUserId: Int, date: Long, receivedDate: Long) extends Message

  private[messaging] case class MessageRead(readerUserId: Int, date: Long, readDate: Long) extends Message
}

private[messaging] trait PeerManager extends Actor with ActorLogging {
  import ContactsUtils._

  implicit private val ec: ExecutionContext = context.dispatcher

  protected def getPushText(message: ApiMessage, clientUser: models.User, outUser: Int) = {
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