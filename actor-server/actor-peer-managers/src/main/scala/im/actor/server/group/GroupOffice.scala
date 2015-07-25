package im.actor.server.group

import scala.util.control.NoStackTrace

object GroupErrors {

  final object InvalidAccessHash extends IllegalArgumentException("Invalid group access hash") with NoStackTrace

  final object NotAMember extends Exception("Not a group member") with NoStackTrace

  case object UserAlreadyJoined extends Exception with NoStackTrace

  case object UserAlreadyInvited extends Exception with NoStackTrace

}

object GroupOffice extends GroupOperations

