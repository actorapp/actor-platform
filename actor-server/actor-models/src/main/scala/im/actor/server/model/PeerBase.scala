package im.actor.server.model

trait PeerBase {
  val `type`: PeerType

  val typ = `type`
}
