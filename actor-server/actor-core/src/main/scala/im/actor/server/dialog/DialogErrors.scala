package im.actor.server.dialog

import im.actor.server.model.Peer

import scala.util.control.NoStackTrace

abstract class DialogError(msg: String) extends RuntimeException(msg) with NoStackTrace

object DialogErrors {
  object MessageToSelf extends DialogError("Private dialog with self is not allowed")

  final case class DialogAlreadyShown(peer: Peer) extends DialogError(s"Dialog $peer is already shown")
  final case class DialogAlreadyHidden(peer: Peer) extends DialogError(s"Dialog $peer is already hidden")
  final case class DialogAlreadyFavourited(peer: Peer) extends DialogError(s"Dialog $peer is already favourited")
  final case class DialogAlreadyUnfavourited(peer: Peer) extends DialogError(s"Dialog $peer is already unfavourited")
}