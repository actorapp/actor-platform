package im.actor.server.api.mtproto

import scodec.bits._

package object transport {
  sealed trait MTProto
  case class MTPackage(authId: Long, sessionId: Long, message: BitVector) extends MTProto
  case class InternalError(errorCode: Byte, retryTimeout: Int, msg: String) extends MTProto
  case class Ping(randomBytes: BitVector) extends MTProto
  case class Pong(randomBytes: BitVector) extends MTProto
  case class Drop(errorCode: Byte, message: String) extends MTProto
  case class Redirect(hostname: String, port: Int, timeout: Int) extends MTProto

  sealed trait MTTransport
  case class ProtoPackage(m: MTProto) extends MTTransport
  case class Handshake(protoVersion: Byte, apiMajorVersion: Byte, apiMinorVersion: Byte,
                       randomBytes: BitVector) extends MTTransport
  case object SilentClose extends MTTransport

  case class TransportPackage(packageIndex: Int, pkg: MTProto)

  object MTPackage {
    val header: Byte = 0
  }

  object Ping {
    val header: Byte = 1
  }

  object Pong {
    val header: Byte = 2
  }

  object Drop {
    val header: Byte = 3
  }

  object Redirect {
    val header: Byte = 4
  }

  object InternalError {
    val header: Byte = 5
  }

}
