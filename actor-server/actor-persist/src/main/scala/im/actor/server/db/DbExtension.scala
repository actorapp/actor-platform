package im.actor.server.db

import akka.actor._
import com.github.kxbmap.configs._
import com.typesafe.config.{ Config, ConfigFactory }
import im.actor.server.JNDI
import org.flywaydb.core.Flyway
import slick.driver.PostgresDriver.api.Database
import slick.jdbc.{ HikariCPJdbcDataSource, JdbcDataSource }

import scala.util.Try

trait DbExtension extends Extension {
  val ds: JdbcDataSource
  val db: Database
}

final class DbExtensionImpl(val ds: HikariCPJdbcDataSource, val db: Database) extends Extension with FlywayInit {
  private lazy val flyway: Flyway = initFlyway(ds.ds)

  def clean(): Unit = flyway.clean()

  def migrate(): Unit = flyway.migrate()
}

object DbExtension extends ExtensionId[DbExtensionImpl] with ExtensionIdProvider {
  private val JndiPath = "DefaultDataSource"

  override def lookup = DbExtension

  override def createExtension(system: ExtendedActorSystem): DbExtensionImpl = {
    val sqlConfig = system.settings.config.getConfig("services.postgresql")
    val ds = initDs(sqlConfig).get
    val db = initDb(ds)

    system.registerOnTermination {
      db.close()
    }

    new DbExtensionImpl(ds, db)
  }

  private def initDs(sqlConfig: Config): Try[HikariCPJdbcDataSource] = {
    for {
      host ← sqlConfig.get[Try[String]]("host")
      port ← sqlConfig.get[Try[Int]]("port")
      db ← sqlConfig.get[Try[String]]("db")
      _ ← sqlConfig.get[Try[String]]("user")
      _ ← sqlConfig.get[Try[String]]("password")
    } yield HikariCPJdbcDataSource.forConfig(sqlConfig.withFallback(ConfigFactory.parseString(
      s"""
        |url: "jdbc:postgresql://"${host}":"${port}"/"${db}
      """.stripMargin
    )).resolve(), null, "main")
  }

  private def initDb(ds: HikariCPJdbcDataSource): Database = {
    JNDI.initialContext.rebind(JndiPath, ds.ds)
    Database.forSource(ds)
  }
}