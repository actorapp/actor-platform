package im.actor.server

import im.actor.server.api.frontend.TCP
import akka.actor._
import akka.stream.FlowMaterializer
import akka.kernel.Bootable
import com.typesafe.config.ConfigFactory

class ApiKernel extends Bootable {
  val config = ConfigFactory.load()
  val serverConfig = config.getConfig("actor-server")

  implicit val system = ActorSystem(serverConfig.getString("actor-system-name"), serverConfig)
  implicit val executor = system.dispatcher
  implicit val materializer = FlowMaterializer()

  def startup() = {
    TCP.start(serverConfig)
  }

  def shutdown() = {
    system.shutdown()
  }
}
