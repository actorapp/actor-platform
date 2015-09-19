package im.actor.server.user

import scala.util.control.NoStackTrace

object UserOffice {

  case object InvalidAccessHash extends Exception with NoStackTrace

  case object FailedToFetchInfo

  def persistenceIdFor(userId: Int): String = s"User-${userId}"
}