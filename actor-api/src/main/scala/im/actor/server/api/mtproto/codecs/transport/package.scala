package im.actor.server.api.mtproto.codecs

import im.actor.server.api.mtproto.transport._
import scodec.bits._
import scodec.Err
import scodec.Codec
import scodec.codecs._
import scalaz._
import Scalaz._

package object transport {
  import im.actor.server.api.util.ByteConstants._

  val HandshakeCodec = (byte :: byte :: byte :: bytes).as[Handshake]

  object TransportPackageCodec extends Codec[TransportPackage] {
    private val codec = (int32 :: MTProtoCodec).as[TransportPackage]

    def encode(p: TransportPackage) = {
      for {
        body <- codec.encode(p)
        pkgBody = body ++ CodecUtils.crc32(body)
        length <- int32.encode((pkgBody.length / byteSize).toInt)
      }
      yield length ++ pkgBody
    }

    def decode(buf: BitVector): Err \/ (BitVector, TransportPackage) = {
      val pkgCrc = buf.takeRight(int32Bits)
      val bsCrc = CodecUtils.crc32(buf.dropRight(int32Bits))
      if (pkgCrc == bsCrc) codec.decode(buf)
      else Err("invalid crc32").left
    }
  }

  val MTPackageCodec = (int64 :: int64 :: bits).as[MTPackage]

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
