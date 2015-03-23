package im.actor.server.session

import im.actor.api.rpc.BaseClientData
import im.actor.server.mtproto.protocol.ProtoMessage

sealed trait SessionMessage

object SessionMessage {
  @SerialVersionUID(1L)
  private[session] case class Envelope(authId: Long, sessionId: Long, message: SessionMessage)

  @SerialVersionUID(1L)
  case class HandleMessageBox(messageBoxBytes: Array[Byte]) extends SessionMessage

  @SerialVersionUID(1L)
  case class SendProtoMessage(message: ProtoMessage) extends SessionMessage

  @SerialVersionUID(1L)
  case class UserAuthorized(userId: Int) extends SessionMessage

  def envelope(authId: Long, sessionId: Long, message: SessionMessage): Envelope =
    Envelope(authId, sessionId, message)

  def envelope(message: SessionMessage)(implicit clientData: BaseClientData): Envelope =
    envelope(clientData.authId, clientData.sessionId, message)
}