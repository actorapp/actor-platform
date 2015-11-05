package im.actor.server.dialog

object DialogErrors {
  final object MessageToSelf extends Exception("Private dialog with self is not allowed")
}