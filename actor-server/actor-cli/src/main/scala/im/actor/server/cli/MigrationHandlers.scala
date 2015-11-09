package im.actor.server.cli

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import im.actor.config.ActorConfig
import im.actor.server.db.DbExtension
import sql.migration.V20151108011300__FillUserSequence

import scala.concurrent._

final class MigrationHandlers {
  def userSequence(): Future[Unit] = {
    val config = ActorConfig.load(ConfigFactory.parseString(
      """
        |akka {
        |  cluster.seed-nodes = []
        |  remote {
        |    netty.tcp.hostname = "127.0.0.1"
        |    netty.tcp.port = 0
        |  }
        |}
      """.stripMargin
    ))

    implicit val system = ActorSystem("migrator", config)
    implicit val ec = system.dispatcher
    implicit val db = DbExtension(system)
    implicit val mat = ActorMaterializer()

    val migration = new V20151108011300__FillUserSequence

    Future {
      blocking {
        migration.migrate()
      }
    }
  }
}