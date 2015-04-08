package im.actor.server.db

import javax.sql.DataSource

import com.typesafe.config._
import slick.driver.PostgresDriver.api.Database
import slick.jdbc.{ JdbcDataSource, HikariCPJdbcDataSource }

trait DbInit {
  def initDs(sqlConfig: Config): HikariCPJdbcDataSource = {
    HikariCPJdbcDataSource.forConfig(sqlConfig, null, "")
  }

  def initDb(source: JdbcDataSource) =
    Database.forSource(source)
}
