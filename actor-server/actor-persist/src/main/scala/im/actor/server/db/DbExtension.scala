package im.actor.server.db

import akka.actor._
import slick.driver.PostgresDriver.api.Database
import slick.jdbc.{ JdbcDataSource, HikariCPJdbcDataSource }

trait DbExtension extends Extension {
  val ds: JdbcDataSource
  val db: Database
}

final class DbExtensionImpl(val ds: HikariCPJdbcDataSource, val db: Database) extends Extension

object DbExtension extends ExtensionId[DbExtensionImpl] with ExtensionIdProvider with DbInit {
  override def lookup = DbExtension

  override def createExtension(system: ExtendedActorSystem) = {
    val sqlConfig = system.settings.config.getConfig("services.postgresql")
    val ds = initDs(sqlConfig).toOption.get
    val db = initDb(ds)
    new DbExtensionImpl(ds, db)
  }
}