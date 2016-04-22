package im.actor.server.model

object PeerErrors {
  final case class UnknownPeerType(typ: PeerType) extends IllegalArgumentException(s"Unknown peer type: $typ")
}

trait PeerBase {
  val `type`: PeerType

  val typ = `type`
}
