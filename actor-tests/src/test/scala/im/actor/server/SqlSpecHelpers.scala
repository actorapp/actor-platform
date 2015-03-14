package im.actor.server

import com.typesafe.config._
import im.actor.server.db.{ FlywayInit, DbInit }
import org.specs2.specification._
import slick.driver.PostgresDriver.api.Database

trait SqlSpecHelpers extends FlywayInit with DbInit {
  final val sqlConfig = ConfigFactory.load().getConfig("actor-server.persist.sql")

  def migrateAndInitDb(): Database = {
    val flyway = initFlyway(sqlConfig)
    flyway.clean()
    flyway.migrate()
    initDb(sqlConfig)
  }

  trait sqlDb extends Scope {
    val flyway = initFlyway(sqlConfig)

    flyway.clean
    flyway.migrate
  }
}
