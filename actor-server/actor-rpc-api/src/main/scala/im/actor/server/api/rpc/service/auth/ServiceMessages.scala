package im.actor.server.api.rpc.service.auth

import im.actor.api.rpc.messaging.{ ServiceExContactRegistered, ServiceMessage }

object ServiceMessages {
  def contactRegistered(userId: Int) = ServiceMessage("Contact registered", Some(ServiceExContactRegistered(userId)))
}

