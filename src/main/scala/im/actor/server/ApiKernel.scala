package im.actor.server

import im.actor.server.api.frontend.{ TCP, WS }
import akka.actor._
import akka.stream.ActorFlowMaterializer
import akka.kernel.Bootable
import com.typesafe.config.ConfigFactory

class ApiKernel extends Bootable {
  val config = ConfigFactory.load()
  val serverConfig = config.getConfig("actor-server")

  implicit val system = ActorSystem(serverConfig.getString("actor-system-name"), serverConfig)
  implicit val executor = system.dispatcher
  implicit val materializer = ActorFlowMaterializer()

  def startup() = {
    TCP.start(serverConfig)
    WS.start(serverConfig)
  }

  def shutdown() = {
    system.shutdown()
  }
}
