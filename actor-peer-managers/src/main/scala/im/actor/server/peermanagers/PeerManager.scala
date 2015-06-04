package im.actor.server.peermanagers

import scala.annotation.meta.field
import scala.concurrent.ExecutionContext

import akka.actor.{ Actor, ActorLogging }
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag
import org.joda.time.DateTime
import slick.dbio.DBIO

import im.actor.api.rpc.messaging.{ Message ⇒ ApiMessage, _ }
import im.actor.api.rpc.peers.{ Peer, PeerType }
import im.actor.server.commons.serialization.KryoSerializable
import im.actor.server.models
import im.actor.server.util.ContactsUtils

object PeerManager {
  sealed trait Message extends KryoSerializable

  case class Envelope(
    @(Tag @field)(0) peerId: Int,
    @(Tag @field)(1) payload:Message
  ) extends KryoSerializable

  case class SendMessage(
    @(Tag @field)(0) senderUserId:Int,
    @(Tag @field)(1) senderAuthId:Long,
    @(Tag @field)(2) randomId:    Long,
    @(Tag @field)(3) date:        DateTime,
    @(Tag @field)(4) message:     ApiMessage,
    @(Tag @field)(5) isFat:       Boolean    = false
  ) extends Message

  case class MessageReceived(
    @(Tag @field)(1) receiverUserId:Int,
    @(Tag @field)(2) date:          Long,
    @(Tag @field)(3) receivedDate:  Long
  ) extends Message

  case class MessageRead(
    @(Tag @field)(0) readerUserId:Int,
    @(Tag @field)(1) date:        Long,
    @(Tag @field)(2) readDate:    Long
  ) extends Message
}

private[peermanagers] trait PeerManager extends Actor with ActorLogging {
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