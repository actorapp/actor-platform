package im.actor.server.session

import akka.actor.ActorRef

import im.actor.api.rpc.BaseClientData
import im.actor.server.mtproto.protocol.ProtoMessage

case class SessionRegion(ref: ActorRef)

sealed trait SessionMessage
sealed trait SubscribeCommand extends SessionMessage
sealed trait SessionResponse

object SessionMessage {
  @SerialVersionUID(1L)
  private[session] case class Envelope(authId: Long, sessionId: Long, message: SessionMessage)

  @SerialVersionUID(1L)
  case class HandleMessageBox(messageBoxBytes: Array[Byte]) extends SessionMessage

  @SerialVersionUID(1L)
  case class AuthorizeUser(userId: Int) extends SessionMessage

  @SerialVersionUID(1L)
  case class SubscribeToOnline(userIds: Set[Int]) extends SubscribeCommand

  @SerialVersionUID(1L)
  case class SubscribeFromOnline(userIds: Set[Int]) extends SubscribeCommand

  @SerialVersionUID(1L)
  case class SubscribeToGroupOnline(groupIds: Set[Int]) extends SubscribeCommand

  @SerialVersionUID(1L)
  case class SubscribeFromGroupOnline(groupIds: Set[Int]) extends SubscribeCommand

  @SerialVersionUID(1L)
  case class AuthorizeUserAck(userId: Int) extends SessionResponse

  def envelope(authId: Long, sessionId: Long, message: SessionMessage): Envelope =
    Envelope(authId, sessionId, message)

  def envelope(message: SessionMessage)(implicit clientData: BaseClientData): Envelope =
    envelope(clientData.authId, clientData.sessionId, message)
}