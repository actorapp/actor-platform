package im.actor.server.api.mtproto

package object protocol {
  trait ProtoMessageWithHeader {
    val header: Int
  }
  trait ProtoMessage extends ProtoMessageWithHeader
  trait RpcRequestMessage extends ProtoMessageWithHeader
  trait RpcResponseMessage extends ProtoMessageWithHeader
  trait UpdateMessage extends ProtoMessageWithHeader
}
