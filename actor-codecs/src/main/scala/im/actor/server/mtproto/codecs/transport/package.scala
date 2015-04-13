package im.actor.server.mtproto.codecs

import scodec.{ codecs => C, _ }

import im.actor.server.mtproto.transport._

@SerialVersionUID(1L)
case class TransportPackageHeader(index: Int, header: Int, bodyLength: Int)

package object transport {
  val intLengthBits = IntLengthBitsCodec

  val handshakeHeader = (C.byte :: C.byte :: C.byte :: C.int32).as[HandshakeHeader]
  val handshakeHeaderSize = byteSize + byteSize + byteSize + 32
  def handshakeData(bytesSize: Int) = C.bits(bytesSize.toLong * byteSize)

  val handshake = (C.byte :: C.byte :: C.byte :: intLengthBits).as[Handshake]

  val MTPackageCodec = (C.int64 :: C.int64 :: BytesCodec).as[MTPackage]

  val PingCodec = bytes.as[Ping]

  val PongCodec = bytes.as[Pong]

  val DropCodec = (C.int64 :: C.byte :: string).as[Drop]

  val RedirectCodec = (string :: C.int32 :: C.int32).as[Redirect]

  val InternalErrorCodec = (C.byte :: C.int32 :: string).as[InternalError]

  val PackageIndexCodec = C.int32

  val transportPackageHeader = (C.int32 :: C.uint8 :: C.int32).as[TransportPackageHeader]

  def mtprotoCodec(header: Int): GenCodec[_, MTProto] =
    header match {
      case MTPackage.header => MTPackageCodec.map(_.asInstanceOf[MTProto])
      case Ping.header => PingCodec.map(_.asInstanceOf[MTProto])
      case Pong.header => PongCodec.map(_.asInstanceOf[MTProto])
      case Drop.header => DropCodec.map(_.asInstanceOf[MTProto])
      case Redirect.header => RedirectCodec.map(_.asInstanceOf[MTProto])
      case InternalError.header => InternalErrorCodec.map(_.asInstanceOf[MTProto])
    }
}
