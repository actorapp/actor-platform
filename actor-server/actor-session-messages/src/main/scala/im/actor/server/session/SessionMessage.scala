package im.actor.server.session

import akka.actor.ActorRef

import im.actor.server.commons.serialization.ActorSerializer

case class SessionRegion(ref: ActorRef)

object SessionMessage {
  def register(): Unit = {
    ActorSerializer.register(100, classOf[SessionEnvelope])
    ActorSerializer.register(101, classOf[SubscribeToOnline])
    ActorSerializer.register(102, classOf[SubscribeFromOnline])
    ActorSerializer.register(103, classOf[SubscribeToGroupOnline])
    ActorSerializer.register(104, classOf[SubscribeFromGroupOnline])
    ActorSerializer.register(105, classOf[AuthorizeUserAck])
  }
}

trait SessionMessage

trait SubscribeCommand extends SessionMessage

trait SessionResponse
