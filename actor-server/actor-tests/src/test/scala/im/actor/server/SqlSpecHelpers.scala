package im.actor.server

import scala.util.{ Failure, Success }

import com.typesafe.config._
import slick.driver.PostgresDriver.api.Database
import slick.jdbc.JdbcDataSource

import im.actor.server.db.{ DbInit, FlywayInit }

trait SqlSpecHelpers extends FlywayInit with DbInit {
  final val sqlConfig = ConfigFactory.load().getConfig("services.postgresql")

  def migrateAndInitDb(): (JdbcDataSource, Database) = {
    initDs(sqlConfig) match {
      case Success(ds) ⇒
        val flyway = initFlyway(ds.ds)
        flyway.clean()
        flyway.migrate()

        (ds, initDb(ds))
      case Failure(e) ⇒ throw e
    }
  }
}
