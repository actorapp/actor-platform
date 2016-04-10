package im.actor.server.dialog

import im.actor.server.model.Peer

import scala.util.control.NoStackTrace

abstract class DialogError(msg: String) extends RuntimeException(msg) with NoStackTrace

case object InvalidAccessHash extends RuntimeException with NoStackTrace

object DialogErrors {
  object MessageToSelf extends DialogError("Private dialog with self is not allowed")

  final case class DialogAlreadyShown(peer: Peer) extends DialogError(s"Dialog $peer is already shown")
  final case class DialogAlreadyFavourited(peer: Peer) extends DialogError(s"Dialog $peer is already favourited")
  final case class DialogAlreadyUnfavourited(peer: Peer) extends DialogError(s"Dialog $peer is already unfavourited")
  final case class DialogAlreadyArchived(peer: Peer) extends DialogError(s"Dialog $peer is already archived")

  final case class UnknownDialogGroupType(typ: DialogGroupType)
    extends RuntimeException(s"Unknown dialog group type: $typ")
}