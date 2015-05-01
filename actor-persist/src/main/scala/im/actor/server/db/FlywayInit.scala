package im.actor.server.db

import java.sql.Connection
import javax.sql.DataSource

import org.flywaydb.core.Flyway
import org.flywaydb.core.api.MigrationInfo
import org.flywaydb.core.api.callback.FlywayCallback

trait FlywayInit {
  def initFlyway(ds: DataSource) = {
    val flyway = new Flyway()
    flyway.setDataSource(ds)
    flyway.setLocations("sql.migration")
    flyway.setCallbacks(new BeforeCleanCallback())
    flyway
  }
}

class BeforeCleanCallback extends FlywayCallback {
  def afterBaseline(connection: Connection) {}
  def afterClean(connection: Connection) {}
  def afterEachMigrate(connection: Connection) {}
  def afterInfo(connection: Connection) {}
  def afterEachMigrate(connection: Connection, migrationInfo: MigrationInfo) {}
  def afterMigrate(connection: Connection) {}
  def afterRepair(connection: Connection) {}
  def afterValidate(connection: Connection) {}
  def beforeBaseline(connection: Connection) {}
  def beforeClean(connection: Connection) {}
  def beforeEachMigrate(connection: Connection, migrationInfo: MigrationInfo) {}
  def beforeInfo(connection: Connection) {}
  def beforeInit(connection: Connection) {}
  def beforeMigrate(connection: Connection) {}
  def beforeRepair(connection: Connection) {}
  def beforeValidate(connection: Connection) {}

  def afterInit(connection: Connection) {
    if (connection.getMetaData().getDriverName().startsWith("PostgreSQL")) {
      val stmt = connection.prepareStatement("""DROP EXTENSION IF EXISTS "uuid-ossp";""")

      try {
        stmt.execute()
      } finally {
        stmt.close()
      }
    }
  }
}
