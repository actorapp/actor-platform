package im.actor.server.session

import akka.actor.ActorRef
import im.actor.serialization.ActorSerializer

case class SessionRegion(ref: ActorRef)

object SessionMessage {
  def register(): Unit = {
    ActorSerializer.register(1000, classOf[SessionEnvelope])
    ActorSerializer.register(1001, classOf[SubscribeToOnline])
    ActorSerializer.register(1002, classOf[SubscribeFromOnline])
    ActorSerializer.register(1003, classOf[SubscribeToGroupOnline])
    ActorSerializer.register(1004, classOf[SubscribeFromGroupOnline])
    ActorSerializer.register(1005, classOf[AuthorizeUserAck])
  }
}

trait SessionMessage

trait SubscribeCommand extends SessionMessage

trait SessionResponse
