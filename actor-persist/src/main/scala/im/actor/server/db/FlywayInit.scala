package im.actor.server.db

import com.typesafe.config._
import org.flywaydb.core.Flyway

trait FlywayInit {
  def initFlyway(sqlConfig: Config) = {
    val flyway = new Flyway()
    flyway.setDataSource(sqlConfig.getString("url"), sqlConfig.getString("username"), sqlConfig.getString("password"))
    flyway.setLocations("sql.migration")
    flyway
  }
}
