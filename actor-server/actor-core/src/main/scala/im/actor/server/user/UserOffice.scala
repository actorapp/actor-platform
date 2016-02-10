package im.actor.server.user

object UserOffice {
  def persistenceIdFor(userId: Int): String = s"User-${userId}"
}