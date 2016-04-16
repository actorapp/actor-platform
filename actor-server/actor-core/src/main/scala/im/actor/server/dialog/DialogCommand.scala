package im.actor.server.dialog

import im.actor.server.model.Peer

trait DialogCommand {
  val dest: Option[Peer]

  def getDest: Peer
}

trait DirectDialogCommand extends DialogCommand {
  val origin: Option[Peer]

  def getOrigin: Peer
}