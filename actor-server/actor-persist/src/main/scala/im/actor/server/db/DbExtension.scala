package im.actor.server.db

import akka.actor._
import akka.event.Logging
import com.github.kxbmap.configs._
import com.typesafe.config.{ Config, ConfigFactory }
import im.actor.server.JNDI
import org.flywaydb.core.Flyway
import slick.driver.PostgresDriver.api.Database
import slick.jdbc.hikaricp.HikariCPJdbcDataSource
import slick.jdbc.JdbcDataSource
import slick.util.AsyncExecutor

import scala.util.{ Failure, Success, Try }

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
    val log = Logging(system, getClass)

    val sqlConfig = system.settings.config.getConfig("services.postgresql")
    val queueSize = sqlConfig.getInt("queueSize")
    val ds = initDs(sqlConfig).get
    val db = initDb(ds, queueSize)

    system.registerOnTermination {
      db.close()
    }

    val ext = new DbExtensionImpl(ds, db)

    Try(ext.migrate()) match {
      case Success(_) ⇒
      case Failure(e) ⇒
        log.error(e, "Migration failed")
        throw e
    }
    ext
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
    )).resolve(), null, "main", getClass.getClassLoader)
  }

  private def initDb(ds: HikariCPJdbcDataSource, queueSize: Int): Database = {
    JNDI.initialContext.rebind(JndiPath, ds.ds)
    Database.forSource(ds, executor = AsyncExecutor("AsyncExecutor.actor", 20, queueSize))
  }
}