package im.actor.server.migrations.v2

import java.time.Instant

import com.google.protobuf.wrappers.Int64Value
import im.actor.storage.{ Connector, SimpleStorage }
import org.slf4j.LoggerFactory

import scala.concurrent.duration._
import scala.language.postfixOps

object MigrationNameList {
  val MultiSequence = "multi-sequence-2016-07-01"
  val GroupsV2 = "groups-v2-2016-07-10"
}

/**
 * Stores mapping "Migration name" -> "Migration timestamp"
 * name: String
 * timestamp: Long
 */
private object MigrationTsStorage extends SimpleStorage("migration_ts")

//TODO: find better name
object MigrationTsActions {

  private val log = LoggerFactory.getLogger(this.getClass)

  implicit val OperationTimeout = 10 seconds

  def getTimestamp(migrationName: String)(implicit conn: Connector): Option[Long] = {
    conn.runSync(MigrationTsStorage.get(migrationName)) map { bytes ⇒
      Int64Value.parseFrom(bytes).value
    }
  }

  // insert timestamp in `MigrationTsStorage`. Make sure it happens only once.
  def insertTimestamp(migrationName: String, ts: Long)(implicit conn: Connector): Unit = {
    val alreadyRun = conn.runSync(MigrationTsStorage.get(migrationName)).isDefined
    if (alreadyRun) {
      log.info(s"Migration $migrationName already run at ${Instant.ofEpochMilli(ts)}, skipping.")
    } else {
      conn.runSync(
        MigrationTsStorage.upsert(
          migrationName,
          Int64Value(ts).toByteArray
        )
      )
      log.info(s"Wrote migration timestamp for: $migrationName, date: ${Instant.ofEpochMilli(ts)}")
    }
  }
}
