package im.actor.server

import akka.actor.ActorSystem
import im.actor.server.api.rpc.service.messaging.MessagingServiceImpl

trait ImplicitMessagingService {
  protected implicit val system: ActorSystem

  implicit val msgService = MessagingServiceImpl()
}