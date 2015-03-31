package im.actor.server.mtproto.codecs.transport

import scodec.GenCodec

import im.actor.server.mtproto.transport._
/*
object SignedMTProtoCodec {
  def apply(header: Int): GenCodec[_, MTProto] =
    header match {
      case MTPackage.header => signedMTPackage.map(_.asInstanceOf[MTProto])
      case Ping.header => signedPing.map(_.asInstanceOf[MTProto])
      case Pong.header => signedPong.map(_.asInstanceOf[MTProto])
      case Drop.header => signedDrop.map(_.asInstanceOf[MTProto])
      case Redirect.header => signedRedirect.map(_.asInstanceOf[MTProto])
      case InternalError.header => signedInternalError.map(_.asInstanceOf[MTProto])
    }
}


*/