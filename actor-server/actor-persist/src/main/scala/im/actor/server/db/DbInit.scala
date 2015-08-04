package im.actor.server.db

import scala.util.Try

import com.github.kxbmap.configs._
import com.typesafe.config._
import slick.driver.PostgresDriver.api.Database
import slick.jdbc.{ HikariCPJdbcDataSource, JdbcDataSource }

trait DbInit {
  def initDs(sqlConfig: Config): Try[HikariCPJdbcDataSource] = {
    for {
      host ← sqlConfig.get[Try[String]]("host")
      port ← sqlConfig.get[Try[Int]]("port")
      db ← sqlConfig.get[Try[String]]("db")
      u ← sqlConfig.get[Try[String]]("user")
      _ ← sqlConfig.get[Try[String]]("password")
    } yield {
      val conf = sqlConfig.withFallback(ConfigFactory.parseString(
        s"""
          |{
          |  url: "jdbc:postgresql://${host}:${port}/${db}"
          |}
        """.stripMargin
      ))

      HikariCPJdbcDataSource.forConfig(conf, null, "")
    }
  }

  def initDb(source: JdbcDataSource) =
    Database.forSource(source)
}
