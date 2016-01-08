package im.actor.server.mtproto.codecs

import scodec.{ codecs ⇒ C, _ }

import im.actor.server.mtproto.transport._

@SerialVersionUID(1L)
case class TransportPackageHeader(index: Int, header: Int, bodyLength: Int)

package object transport {
  val intLengthBits = IntLengthBitsCodec
  val intLengthString = IntLengthStringCodec

  val HandshakeCodec = (C.byte :: C.byte :: C.byte :: intLengthBits).as[Handshake]

  val HandshakeResponseCodec = (C.byte :: C.byte :: C.byte :: C.bits).as[HandshakeResponse]

  val PackageCodec = (C.int64 :: C.int64 :: C.bits).as[Package]

  val PingCodec = intLengthBits.as[Ping]

  val PongCodec = intLengthBits.as[Pong]

  val DropCodec = (C.int64 :: C.byte :: intLengthString).as[Drop]

  val RedirectCodec = (string :: C.int32 :: C.int32).as[Redirect]

  val InternalErrorCodec = (C.byte :: C.int32 :: string).as[InternalError]

  val AckCodec = C.int32.as[Ack]

  val PackageIndexCodec = C.int32

  val transportPackageHeader = (C.int32 :: C.uint8 :: C.int32).as[TransportPackageHeader]

  def mtprotoCodec(header: Int): GenCodec[_, MTProto] =
    header match {
      case Handshake.header         ⇒ HandshakeCodec.map(_.asInstanceOf[MTProto])
      case HandshakeResponse.header ⇒ HandshakeResponseCodec.map(_.asInstanceOf[MTProto])
      case Package.header           ⇒ PackageCodec.map(_.asInstanceOf[MTProto])
      case Ping.header              ⇒ PingCodec.map(_.asInstanceOf[MTProto])
      case Pong.header              ⇒ PongCodec.map(_.asInstanceOf[MTProto])
      case Drop.header              ⇒ DropCodec.map(_.asInstanceOf[MTProto])
      case Redirect.header          ⇒ RedirectCodec.map(_.asInstanceOf[MTProto])
      case InternalError.header     ⇒ InternalErrorCodec.map(_.asInstanceOf[MTProto])
      case Ack.header               ⇒ AckCodec.map(_.asInstanceOf[MTProto])
    }
}
