package im.actor.server.group

import scala.util.control.NoStackTrace

object GroupErrors {

  final object NotAMember extends Exception("Not a group member") with NoStackTrace

  case object UserAlreadyJoined extends Exception with NoStackTrace

  case object UserAlreadyInvited extends Exception with NoStackTrace

  case object UserAlreadyAdmin extends Exception with NoStackTrace

  case object NotAdmin extends Exception with NoStackTrace

  case object AboutTooLong extends Exception with NoStackTrace

  case object TopicTooLong extends Exception with NoStackTrace

  case object NoBotFound extends Exception with NoStackTrace
}

object GroupOffice extends GroupOperations {
  def persistenceIdFor(groupId: Int): String = s"Group-${groupId}"
}

