package im.actor.server.api

import com.google.protobuf.{ ByteString, CodedInputStream }
import com.trueaccord.scalapb.TypeMapper

import im.actor.api.rpc.messaging.Message

object TypeMappers extends MessageMapper

trait MessageMapper {
  private def applyMessage(buf: ByteString): Message = {
    Message.parseFrom(CodedInputStream.newInstance(buf.asReadOnlyByteBuffer())).right.get
  }

  private def unapplyMessage(message: Message): ByteString = {
    ByteString.copyFrom(message.toByteArray)
  }

  implicit val messageMapper: TypeMapper[ByteString, Message] = TypeMapper(applyMessage)(unapplyMessage)
}
