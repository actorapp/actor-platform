package im.actor.server

import scala.util.{ Failure, Success }

import akka.actor.ActorSystem
import com.typesafe.config._
import slick.driver.PostgresDriver.api.Database
import slick.jdbc.JdbcDataSource

import im.actor.server.db.{ DbExtension, DbInit, FlywayInit }

trait SqlSpecHelpers extends FlywayInit with DbInit {
  val system: ActorSystem
  final val sqlConfig = ConfigFactory.load().getConfig("services.postgresql")

  def migrateAndInitDb(): (JdbcDataSource, Database) = {
    initDs(sqlConfig) match {
      case Success(ds) ⇒
        val flyway = initFlyway(ds.ds)
        flyway.clean()
        flyway.migrate()

        (DbExtension(system).ds, DbExtension(system).db)
      case Failure(e) ⇒ throw e
    }
  }
}
