package im.actor.server.db

import com.typesafe.config._
import java.sql.Driver
import slick.driver.PostgresDriver.api.Database

trait DbInit {
  def initDb(dbConfig: Config) =
    Database.forConfig("", dbConfig, null)
}
