package im.actor.server.push

import akka.actor.ActorSystem
import akka.stream.{ ActorMaterializer, ActorMaterializerSettings, Supervision }
import spray.http.HttpRequest

import scala.concurrent._

package object google {

  type NotificationDelivery = (HttpRequest, GooglePushDelivery.Delivery)

  def tolerantMaterializer(implicit system: ActorSystem): ActorMaterializer = {
    val streamDecider: Supervision.Decider = {
      case e: TimeoutException ⇒
        system.log.warning("Timeout in stream, RESUME {}", e)
        Supervision.Resume
      case e: RuntimeException ⇒
        system.log.warning("Got runtime exception in stream, RESUME {}", e)
        Supervision.Resume
      case e ⇒
        system.log.error(e, "Got exception in stream, STOP")
        Supervision.Stop
    }
    ActorMaterializer(ActorMaterializerSettings(system)
      .withSupervisionStrategy(streamDecider))
  }

}
