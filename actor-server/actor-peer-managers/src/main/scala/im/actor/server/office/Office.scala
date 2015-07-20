package im.actor.server.office

import akka.actor.ActorLogging
import akka.persistence.PersistentActor
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag
import im.actor.api.rpc.AuthorizedClientData
import im.actor.api.rpc.messaging.{ Message ⇒ ApiMessage, _ }
import im.actor.api.rpc.peers.{ Peer, PeerType }
import im.actor.server.commons.serialization.KryoSerializable
import im.actor.server.models
import im.actor.server.util.ContactsUtils
import org.joda.time.DateTime
import slick.dbio.DBIO

import scala.annotation.meta.field
import scala.concurrent.ExecutionContext

object Office {

  /*
  sealed trait Message extends KryoSerializable

  private[office] case class Envelope(
    @(Tag @field)(0) peerId: Int,
    @(Tag @field)(1) payload:Message
  ) extends KryoSerializable

  private[office] case class SendMessage(
    @(Tag @field)(0) senderUserId:Int,
    @(Tag @field)(1) senderAuthId:Long,
    @(Tag @field)(2) randomId:    Long,
    @(Tag @field)(3) date:        DateTime,
    @(Tag @field)(4) message:     ApiMessage,
    @(Tag @field)(5) isFat:       Boolean    = false
  ) extends Message

  private[office] case class MessageReceived(
    @(Tag @field)(0) receiverUserId:Int,
    @(Tag @field)(1) receiverAuthId:Long,
    @(Tag @field)(2) date:          Long,
    @(Tag @field)(3) receivedDate:  Long
  ) extends Message

  private[office] case class MessageRead(
    @(Tag @field)(0) readerUserId:Int,
    @(Tag @field)(1) readerAuthId:Long,
    @(Tag @field)(2) date:        Long,
    @(Tag @field)(3) readDate:    Long
  ) extends Message

  private[office] case class JoinGroup(
    @(Tag @field)(0) joiningUserId:    Int,
    @(Tag @field)(1) joiningUserAuthId:Long,
    @(Tag @field)(2) invitingUserId:   Int
  ) extends Message

  private[office] case class InviteToGroup(
    @(Tag @field)(0) group:        models.FullGroup,
    @(Tag @field)(1) inviteeUserId:Int,
    @(Tag @field)(2) client:       AuthorizedClientData,
    @(Tag @field)(3) randomId:     Long
  ) extends Message

  private[office] case class KickUser(
    @(Tag @field)(0) kickedUserId:Int,
    @(Tag @field)(1) client:      AuthorizedClientData,
    @(Tag @field)(2) randomId:    Long
  ) extends Message

  private[office] case class LeaveGroup(
    @(Tag @field)(0) client:  AuthorizedClientData,
    @(Tag @field)(1) randomId:Long
  ) extends Message
*/
}

trait Office extends PersistentActor