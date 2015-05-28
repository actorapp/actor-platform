package im.actor.server

import com.google.protobuf.CodedInputStream

import im.actor.api.rpc.messaging.Message

trait MessageParsing {

  private def parseMessage(body: Array[Byte]) = Message.parseFrom(CodedInputStream.newInstance(body))


}
