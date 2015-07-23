package im.actor.server.group

import scala.util.control.NoStackTrace

import im.actor.server.commons.serialization.ActorSerializer
import im.actor.server.office.group.GroupEvents

trait GroupError

object GroupErrors {

  final object InvalidAccessHash extends IllegalArgumentException("Invalid group access hash") with NoStackTrace

  final object NotAMember extends Exception("Not a group member") with NoStackTrace

  case object UserAlreadyJoined extends Exception with NoStackTrace

  case object UserAlreadyInvited extends Exception with NoStackTrace

}

object GroupOffice extends GroupOperations {

  ActorSerializer.register(6001, classOf[GroupEvents.MessageRead])
  ActorSerializer.register(6002, classOf[GroupEvents.MessageReceived])
  ActorSerializer.register(6003, classOf[GroupEvents.UserInvited])
  ActorSerializer.register(6004, classOf[GroupEvents.UserJoined])
  ActorSerializer.register(6005, classOf[GroupEvents.Created])

}

