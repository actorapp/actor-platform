package im.actor.server

import com.typesafe.config._
import slick.jdbc.JdbcDataSource
import im.actor.server.db.{ FlywayInit, DbInit }
import slick.driver.PostgresDriver.api.Database

trait SqlSpecHelpers extends FlywayInit with DbInit {
  final val sqlConfig = ConfigFactory.load().getConfig("actor-server.persist.sql")

  def migrateAndInitDb(): (JdbcDataSource, Database) = {
    val ds = initDs(sqlConfig)

    val flyway = initFlyway(ds.ds)
    flyway.clean()
    flyway.migrate()

    (ds, initDb(ds))
  }
}
