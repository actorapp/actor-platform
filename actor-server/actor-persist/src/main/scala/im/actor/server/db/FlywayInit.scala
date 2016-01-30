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
    flyway.setBaselineOnMigrate(true)
    flyway
  }
}

class BeforeCleanCallback extends FlywayCallback {
  def afterBaseline(connection: Connection): Unit = {}
  def afterClean(connection: Connection): Unit = {}
  def afterEachMigrate(connection: Connection): Unit = {}
  def afterInfo(connection: Connection): Unit = {}
  def afterEachMigrate(connection: Connection, migrationInfo: MigrationInfo): Unit = {}
  def afterMigrate(connection: Connection): Unit = {}
  def afterRepair(connection: Connection): Unit = {}
  def afterValidate(connection: Connection): Unit = {}
  def beforeBaseline(connection: Connection): Unit = {}
  def beforeClean(connection: Connection): Unit = executeStmt(connection, """DROP EXTENSION IF EXISTS "ltree" CASCADE;""")
  def beforeEachMigrate(connection: Connection, migrationInfo: MigrationInfo): Unit = {}
  def beforeInfo(connection: Connection): Unit = {}
  def beforeInit(connection: Connection): Unit = {}
  def beforeMigrate(connection: Connection): Unit = {}
  def beforeRepair(connection: Connection): Unit = {}
  def beforeValidate(connection: Connection): Unit = {}
  def afterInit(connection: Connection): Unit = executeStmt(connection, """DROP EXTENSION IF EXISTS "ltree" CASCADE;""")

  def executeStmt(connection: Connection, statement: String): Unit = {
    if (connection.getMetaData().getDriverName().startsWith("PostgreSQL")) {
      val stmt = connection.prepareStatement(statement)
      try {
        stmt.execute()
      } finally {
        stmt.close()
      }
    }
  }

}