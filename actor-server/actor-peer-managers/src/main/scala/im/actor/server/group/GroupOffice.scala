package im.actor.server.group

import scala.util.control.NoStackTrace

import im.actor.server.commons.serialization.ActorSerializer
import im.actor.server.office.group.{GroupEnvelope, GroupEvents}

trait GroupError

object GroupErrors {

  final object InvalidAccessHash extends IllegalArgumentException("Invalid group access hash") with NoStackTrace

  final object NotAMember extends Exception("Not a group member") with NoStackTrace

  case object UserAlreadyJoined extends Exception with NoStackTrace

  case object UserAlreadyInvited extends Exception with NoStackTrace

}

object GroupOffice extends GroupOperations {
  ActorSerializer.register(5000, classOf[GroupEnvelope])
  ActorSerializer.register(5001, classOf[GroupEnvelope.Create])
  ActorSerializer.register(5002, classOf[GroupEnvelope.CreateResponse])
  ActorSerializer.register(5003, classOf[GroupEnvelope.Invite])
  ActorSerializer.register(5004, classOf[GroupEnvelope.Join])
  ActorSerializer.register(5005, classOf[GroupEnvelope.Kick])
  ActorSerializer.register(5006, classOf[GroupEnvelope.Leave])
  ActorSerializer.register(5007, classOf[GroupEnvelope.SendMessage])
  ActorSerializer.register(5008, classOf[GroupEnvelope.MessageReceived])
  ActorSerializer.register(5009, classOf[GroupEnvelope.MessageRead])

  ActorSerializer.register(6001, classOf[GroupEvents.MessageRead])
  ActorSerializer.register(6002, classOf[GroupEvents.MessageReceived])
  ActorSerializer.register(6003, classOf[GroupEvents.UserInvited])
  ActorSerializer.register(6004, classOf[GroupEvents.UserJoined])
  ActorSerializer.register(6005, classOf[GroupEvents.Created])
}

