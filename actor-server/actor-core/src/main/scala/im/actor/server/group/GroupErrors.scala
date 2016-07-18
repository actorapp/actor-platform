package im.actor.server.group

import im.actor.server.office.EntityNotFound

import scala.util.control.NoStackTrace

object GroupErrors {
  final case class GroupNotFound(id: Int) extends EntityNotFound(s"Group $id not found")

  final case class GroupIdAlreadyExists(id: Int) extends Exception with NoStackTrace

  object NotAMember extends Exception("Not a group member") with NoStackTrace

  case object UserAlreadyJoined extends Exception with NoStackTrace

  case object UserAlreadyInvited extends Exception with NoStackTrace

  case object UserAlreadyAdmin extends Exception with NoStackTrace

  case object UserAlreadyNotAdmin extends Exception with NoStackTrace

  case object NotAdmin extends Exception with NoStackTrace

  case object NotOwner extends Exception with NoStackTrace

  case object InvalidTitle extends Exception with NoStackTrace

  case object AboutTooLong extends Exception with NoStackTrace

  case object InvalidShortName extends Exception with NoStackTrace

  case object ShortNameTaken extends Exception with NoStackTrace

  case object TopicTooLong extends Exception with NoStackTrace

  case object NoBotFound extends Exception with NoStackTrace

  case object BlockedByUser extends Exception with NoStackTrace
}
