package im.actor.server

import com.google.protobuf.CodedInputStream

import im.actor.api.rpc.messaging.Message

trait MessageParsing {
  def parseMessage(body: Array[Byte]): Either[Any, Message] = Message.parseFrom(CodedInputStream.newInstance(body))
}
