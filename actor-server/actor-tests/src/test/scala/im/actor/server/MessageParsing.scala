package im.actor.server

import com.google.protobuf.CodedInputStream

import im.actor.api.rpc.messaging.ApiMessage

trait MessageParsing {
  def parseMessage(body: Array[Byte]): Either[Any, ApiMessage] = ApiMessage.parseFrom(CodedInputStream.newInstance(body))
}
