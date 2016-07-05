package im.actor.server.migrations

import im.actor.server.BaseAppSuite
import im.actor.server.db.DbExtension
import im.actor.server.migrations.v2.MigrationTsActions

class MigrationTsSpec extends BaseAppSuite {

  "Migration timestamp" should "be written only once, and never changed" in writeOnce()

  def writeOnce(): Unit = {
    val conn = DbExtension(system).connector

    val initialTs = System.currentTimeMillis - 1000
    MigrationTsActions.insertTimestamp("test-migration-2016-07-03", initialTs)(conn)

    val secondTs = System.currentTimeMillis + 2000
    MigrationTsActions.insertTimestamp("test-migration-2016-07-03", secondTs)(conn)

    val optTs = MigrationTsActions.getTimestamp("test-migration-2016-07-03")(conn)
    optTs should not be empty
    optTs.get shouldEqual initialTs
  }
}
