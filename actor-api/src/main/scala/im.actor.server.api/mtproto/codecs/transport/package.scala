package im.actor.server.api.mtproto.codecs

import akka.util.ByteString
import im.actor.server.api.mtproto.transport._
import scodec.bits._
import scodec.Codec
import scodec.codecs._
import scalaz._
import Scalaz._

package object transport {
  import im.actor.server.api.util.ByteConstants._

  object TransportPackageCodec {
    def decode(bs: ByteString): String \/ TransportPackage = {
      val pkgCrc = bs.takeRight(int32Bytes)
      val bsCrc = CodecUtils.crc32(bs.dropRight(int32Bytes))
      if (pkgCrc == bsCrc) {
        val packageIndex = CodecUtils.readInt32(bs.take(int32Bytes))
        val header: Byte = bs.drop(int32Bytes).head
        val pkgBody = BitVector(CodecUtils.readBytes(bs.drop(int32Bytes + 1)).get.toByteBuffer)
        val codec = header match {
          case MTPackage.header => ???
          case Ping.header => ???
          case Pong.header => ???
          case Drop.header => ???
          case Redirect.header => ???
          case InternalError.header => ???
        }
        ???
      } else "invalid crc32".left
    }

    def encode(p: TransportPackage) = ???
  }

  object MTPackageCodec extends Codec[MTPackage] {
    def encode(m: MTPackage) = ???

    def decode(buf: BitVector) = ???
  }

  val PingCodec = bytes.pxmap[Ping](Ping.apply, Ping.unapply)

  val PongCodec = bytes.pxmap[Pong](Pong.apply, Pong.unapply)

//  val DropCodec = bytes.pxmap[Pong](Pong.apply, Pong.unapply)

  object HandshakeCodec {
    def parse(bs: ByteString): String \/ Handshake = {
      if (bs.length < 4) "received data less than minimum handshake length".left
      else if (bs.length > 260) "received data more than maximum handshake length".left
      else {
        val (protoVersion, apiMajorVersion, apiMinorVersion) = (bs(0), bs(1), bs(2))
        val randomBytes = CodecUtils.readBytes(bs.drop(3)).get
        Handshake(protoVersion, apiMajorVersion, apiMinorVersion, randomBytes).right
      }
    }
  }
}
