package im.actor.server

object CommonErrors {
  case class Forbidden(message: String) extends RuntimeException(message)
  object Forbidden extends Forbidden("You are not allowed to do this.")
}
