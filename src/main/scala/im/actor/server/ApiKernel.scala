package im.actor.server

import im.actor.server.api.frontend.{ Tcp, Ws }
import akka.actor._
import akka.stream.ActorFlowMaterializer
import akka.kernel.Bootable
import com.typesafe.config.ConfigFactory
import im.actor.server.api.rpc.RpcApiService
import im.actor.server.db.{ DbInit, FlywayInit }
import im.actor.server.push.SeqUpdatesManager
import im.actor.server.session.Session

class ApiKernel extends Bootable with DbInit with FlywayInit {
  val config = ConfigFactory.load()
  val serverConfig = config.getConfig("actor-server")
  val sqlConfig = serverConfig.getConfig("persist.sql")

  implicit val system = ActorSystem(serverConfig.getString("actor-system-name"), serverConfig)
  implicit val executor = system.dispatcher
  implicit val materializer = ActorFlowMaterializer()

  val ds = initDs(sqlConfig)
  implicit val db = initDb(ds)

  def startup() = {
    val flyway = initFlyway(ds.ds)
    flyway.migrate()

    val rpcApiService = system.actorOf(RpcApiService.props())
    val seqUpdManagerRegion = SeqUpdatesManager.startRegion()
    val sessionRegion = Session.startRegion(Some(Session.props(rpcApiService, seqUpdManagerRegion)))

    Tcp.start(serverConfig, sessionRegion)
    //Ws.start(serverConfig)
  }

  def shutdown() = {
    system.shutdown()
    ds.close()
  }
}
