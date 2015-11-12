package im.actor.server.dialog

import im.actor.server.model.Peer

trait DialogCommand {
  val dest: Peer
}

trait DirectDialogCommand extends DialogCommand {
  val origin: Peer
}