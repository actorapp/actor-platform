package im.actor.server

import im.actor.server.api.frontend.{ Tcp, Ws }
import akka.actor._
import akka.stream.ActorFlowMaterializer
import akka.kernel.Bootable
import com.typesafe.config.ConfigFactory
import im.actor.server.db.{ DbInit, FlywayInit }

class ApiKernel extends Bootable with DbInit with FlywayInit {
  val config = ConfigFactory.load()
  val serverConfig = config.getConfig("actor-server")

  implicit val system = ActorSystem(serverConfig.getString("actor-system-name"), serverConfig)
  implicit val executor = system.dispatcher
  implicit val materializer = ActorFlowMaterializer()
  val sqlConfig = serverConfig.getConfig("sql")
  val ds = initDs(sqlConfig)
  implicit val db = initDb(ds)

  def startup() = {
    val flyway = initFlyway(config.getConfig("jdbc"))
    flyway.migrate()

    Tcp.start(serverConfig)
    Ws.start(serverConfig)
  }

  def shutdown() = {
    system.shutdown()
    ds.close()
  }
}
