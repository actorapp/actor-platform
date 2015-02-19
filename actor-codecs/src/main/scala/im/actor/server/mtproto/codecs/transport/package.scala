package im.actor.server.mtproto.codecs

import im.actor.server.mtproto.transport._
import scodec.bits._
import scodec._
import scodec.codecs._

package object transport {
  val HandshakeCodec = (byte :: byte :: byte :: bytes).as[Handshake]

  object TransportPackageCodec extends Codec[TransportPackage] {
    def sizeBound = SizeBound.unknown

    private val codec = (int32 :: MTProtoCodec).as[TransportPackage]

    def encode(p: TransportPackage) = {
      for {
        body <- codec.encode(p)
        pkgBody = body ++ CodecUtils.crc32(body)
        length <- int32.encode((pkgBody.length / byteSize).toInt)
      }
      yield length ++ pkgBody
    }

    def decode(buf: BitVector) = {
      val pkgCrc = buf.takeRight(int32Bits)
      val bsCrc = CodecUtils.crc32(buf.dropRight(int32Bits))
      if (pkgCrc == bsCrc) codec.decode(buf)
      else Attempt.failure(Err("invalid crc32"))
    }
  }

  val MTPackageCodec = (int64 :: int64 :: codecs.bits).as[MTPackage]

  val PingCodec = bytes.pxmap[Ping](Ping.apply, Ping.unapply)

  val PongCodec = bytes.pxmap[Pong](Pong.apply, Pong.unapply)

  val DropCodec = (int64 :: byte :: string).as[Drop]

  val RedirectCodec = (string :: int32 :: int32).as[Redirect]

  val InternalErrorCodec = (byte :: int32 :: string).as[InternalError]

  val MTProtoCodec = discriminated[MTProto].by(uint8)
    .\(MTPackage.header) { case c: MTPackage => c } (MTPackageCodec)
    .\(Ping.header) { case c: Ping => c } (PingCodec)
    .\(Pong.header) { case c: Pong => c } (PongCodec)
    .\(Drop.header) { case c: Drop => c } (DropCodec)
    .\(Redirect.header) { case c: Redirect => c } (RedirectCodec)
    .\(InternalError.header) { case c: InternalError => c } (InternalErrorCodec)
}
