package im.actor.server.sequence

abstract class SequenceError(message: String) extends RuntimeException(message)

object SequenceErrors {
  final case class UpdateAlreadyApplied(field: String) extends SequenceError(s"Update of $field is already applied")
}