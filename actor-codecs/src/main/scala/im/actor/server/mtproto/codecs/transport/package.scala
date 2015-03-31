package im.actor.server.mtproto.codecs

import scodec._
import scodec.codecs._

import im.actor.server.mtproto.transport._

@SerialVersionUID(1L)
case class TransportPackageHeader(index: Int, header: Int, bodyLength: Long)

package object transport {
  val HandshakeCodec = (byte :: byte :: byte :: bytes).as[Handshake]

  val MTPackageCodec = (int64 :: int64 :: codecs.bits).as[MTPackage]

  val PingCodec = bytes.as[Ping]

  val PongCodec = bytes.as[Pong]

  val DropCodec = (int64 :: byte :: string).as[Drop]

  val RedirectCodec = (string :: int32 :: int32).as[Redirect]

  val InternalErrorCodec = (byte :: int32 :: string).as[InternalError]

  val PackageIndexCodec = int32

  val transportPackageHeader = (int32 :: uint8 :: varint).as[TransportPackageHeader]

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
