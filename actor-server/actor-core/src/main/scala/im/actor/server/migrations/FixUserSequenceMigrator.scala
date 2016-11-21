package im.actor.server.migrations

import akka.actor.ActorSystem
import com.google.protobuf.ByteString
import im.actor.api.rpc.sequence.UpdateRawUpdate
import im.actor.server.db.ActorPostgresDriver.api._
import im.actor.server.db.DbExtension
import im.actor.server.model.{ SerializedUpdate, UpdateMapping }
import slick.jdbc.{ PositionedParameters, SetParameter }

import scala.concurrent.duration._
import scala.concurrent.Future

object FixUserSequenceMigrator extends Migration {
  implicit object SetByteArray extends SetParameter[Array[Byte]] {
    def apply(v: Array[Byte], pp: PositionedParameters) = {
      pp.setBytes(v)
    }
  }

  override protected def migrationName: String = "2015-11-12-FixUserSequence"

  override protected def migrationTimeout: Duration = 15.minutes

  override protected def startMigration()(implicit system: ActorSystem): Future[Unit] = {
    import system.dispatcher

    val timestamp = System.currentTimeMillis()
    val upd = UpdateRawUpdate(None, Array.empty)
    val mapping = UpdateMapping().withDefault(SerializedUpdate(upd.header, ByteString.copyFrom(upd.toByteArray)))
    val mappingBytes = mapping.toByteArray

    def fix(userId: Int): DBIO[Unit] = {
      for {
        seq ← sql"SELECT seq FROM user_sequence WHERE user_id = $userId ORDER BY seq DESC LIMIT 1".as[Int].headOption map (_ getOrElse 0)
        newSeq = seq + 3000000
        _ ← sqlu"INSERT INTO user_sequence (user_id, seq, timestamp, mapping) VALUES ($userId, $newSeq, $timestamp, $mappingBytes)"
      } yield ()
    }

    DbExtension(system).db.run(for {
      userIds ← sql"SELECT id FROM users".as[Int]
      _ ← DBIO.sequence(userIds map fix)
    } yield ())
  }
}
