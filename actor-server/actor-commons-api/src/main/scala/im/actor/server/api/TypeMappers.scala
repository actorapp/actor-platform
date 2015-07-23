package im.actor.server.api

import com.google.protobuf.{ ByteString, CodedInputStream }
import com.trueaccord.scalapb.TypeMapper
import org.joda.time.DateTime

import im.actor.api.rpc.messaging.Message
import im.actor.api.rpc.peers.Peer

object TypeMappers extends MessageMapper

trait MessageMapper {
  private def applyMessage(buf: ByteString): Message =
    Message.parseFrom(CodedInputStream.newInstance(buf.asReadOnlyByteBuffer())).right.get

  private def unapplyMessage(message: Message): ByteString =
    ByteString.copyFrom(message.toByteArray)

  private def applyPeer(buf: ByteString): Peer =
    Peer.parseFrom(CodedInputStream.newInstance(buf.asReadOnlyByteBuffer())).right.get

  private def unapplyPeer(peer: Peer): ByteString =
    ByteString.copyFrom(peer.toByteArray)

  private def applyDateTime(millis: Long): DateTime = new DateTime(millis)

  private def unapplyDateTime(dt: DateTime): Long = dt.getMillis

  implicit val messageMapper: TypeMapper[ByteString, Message] = TypeMapper(applyMessage)(unapplyMessage)

  implicit val peerMapper: TypeMapper[ByteString, Peer] = TypeMapper(applyPeer)(unapplyPeer)

  implicit val dateTimeMapper: TypeMapper[Long, DateTime] = TypeMapper(applyDateTime)(unapplyDateTime)
}
