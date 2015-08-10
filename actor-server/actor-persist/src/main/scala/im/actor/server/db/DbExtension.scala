package im.actor.server.db

import java.util
import javax.naming.{ NamingException, Context, InitialContext }
import javax.naming.spi.{ InitialContextFactory, InitialContextFactoryBuilder, NamingManager }
import javax.sql.DataSource

import scala.util.Try

import akka.actor._
import com.typesafe.config.{ ConfigFactory, Config }
import com.github.kxbmap.configs._
import org.flywaydb.core.Flyway
import slick.driver.PostgresDriver.api.Database
import slick.jdbc.{ JdbcDataSource, HikariCPJdbcDataSource }

import im.actor.server.JNDI

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