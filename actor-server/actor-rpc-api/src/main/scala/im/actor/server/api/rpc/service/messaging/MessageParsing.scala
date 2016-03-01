package im.actor.server.api.rpc.service.messaging

import com.google.protobuf.CodedInputStream
import im.actor.api.rpc.messaging.ApiMessage

trait MessageParsing {
  def parseMessage(body: Array[Byte]): Either[Any, ApiMessage] = ApiMessage.parseFrom(CodedInputStream.newInstance(body))
}
