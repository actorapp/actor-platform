package im.actor.server.db

import akka.actor._
import akka.event.Logging
import com.github.kxbmap.configs.syntax._
import com.typesafe.config.{ Config, ConfigFactory }
import im.actor.server.JNDI
import im.actor.storage.Connector
import im.actor.storage.slick.SlickConnector
import org.flywaydb.core.Flyway
import slick.driver.PostgresDriver.api.Database
import slick.jdbc.hikaricp.HikariCPJdbcDataSource
import slick.jdbc.{ DataSourceJdbcDataSource, JdbcDataSource }

import scala.util.{ Failure, Success, Try }

final class DbExtensionImpl(val db: Database, val connector: Connector) extends Extension with FlywayInit {
  private lazy val flyway: Flyway = {
    val ds = db.source match {
      case s: HikariCPJdbcDataSource   ⇒ s.ds
      case s: DataSourceJdbcDataSource ⇒ s.ds
      case s                           ⇒ throw new IllegalArgumentException(s"Unknown DataSource: ${s.getClass.getName}")
    }
    initFlyway(ds)
  }

  def clean(): Unit = flyway.clean()

  def migrate(): Unit = flyway.migrate()
}

object DbExtension extends ExtensionId[DbExtensionImpl] with ExtensionIdProvider {
  private val JndiPath = "DefaultDatabase"

  private var _system: ActorSystem = null
  def system = _system

  override def lookup = DbExtension

  override def createExtension(system: ExtendedActorSystem): DbExtensionImpl = {
    this._system = system
    val log = Logging(system, getClass)

    val db = initDb(system.settings.config)
    val connector = new SlickConnector(db)(system.dispatcher)

    system.registerOnTermination {
      db.close()
    }

    val ext = new DbExtensionImpl(db, connector)

    Try(ext.migrate()) match {
      case Success(_) ⇒
      case Failure(e) ⇒
        log.error(e, "Migration failed")
        throw e
    }
    ext
  }

  private def initDb(appConfig: Config): Database = {
    val sqlConfig = appConfig.getConfig("services.postgresql")

    val useConfig = for {
      host ← sqlConfig.get[Try[String]]("host")
      port ← sqlConfig.get[Try[Int]]("port")
      db ← sqlConfig.get[Try[String]]("db")
      _ ← sqlConfig.get[Try[String]]("user")
      _ ← sqlConfig.get[Try[String]]("password")
    } yield sqlConfig.withFallback(ConfigFactory.parseString(
      s"""
         |url: "jdbc:postgresql://"${host}":"${port}"/"${db}
      """.stripMargin
    ))

    val db = Database.forConfig("", useConfig.get)
    JNDI.initialContext.rebind(JndiPath, db)
    db
  }
}