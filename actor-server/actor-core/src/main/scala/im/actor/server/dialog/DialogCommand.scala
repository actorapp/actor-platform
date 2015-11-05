package im.actor.server.dialog

import im.actor.server.model.Peer

trait DialogCommand {
  val origin: Peer
  val dest: Peer
}