package sql.migration

import java.sql.Connection

import com.google.protobuf.ByteString
import com.typesafe.scalalogging.Logger
import com.typesafe.slick.testkit.util.DelegateConnection
import im.actor.server.model.{ SerializedUpdate, UpdateMapping }
import im.actor.server.persist.{ AuthIdRepo, UserRepo }
import org.flywaydb.core.api.migration.jdbc.JdbcMigration
import org.slf4j.LoggerFactory
import slick.driver.PostgresDriver.api._
import slick.jdbc.{ GetResult, JdbcDataSource, SetParameter }

import scala.annotation.tailrec
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps

object V20151108011300__FillUserSequence {
  final case class Obsolete(authId: Long, timestamp: Long, seq: Int, header: Int, data: Array[Byte], userIds: String, groupIds: String)

  implicit val getByteArray = GetResult(r ⇒ r.nextBytes())
  implicit val setByteArray = SetParameter[Array[Byte]] { (bs, pp) ⇒ pp.setBytes(bs) }
  implicit val getObsolete = GetResult(r ⇒
    Obsolete(
      authId = r.nextLong(),
      timestamp = r.nextLong(),
      seq = r.nextInt(),
      header = r.nextInt(),
      data = r.nextBytes(),
      userIds = r.nextString,
      groupIds = r.nextString()
    ))
  val BulkSize = 300
}

final class V20151108011300__FillUserSequence extends JdbcMigration {
  import V20151108011300__FillUserSequence._

  private val log = Logger(LoggerFactory.getLogger(getClass))

  //implicit val get = (GetResult.createGetTuple5[Long, Int, Blob, String, String] _)
  //implicit val getObsolete =
  //GetResult.createGetTuple5[Long, Int, Blob, String, String]

  override def migrate(connection: Connection): Unit = {
    val wrappedConn = new DelegateConnection(connection) {
      override def close(): Unit = ()
    }
    val db = Database.forSource(new JdbcDataSource {
      def createConnection(): Connection = wrappedConn

      def close(): Unit = ()
    })

    Await.ready(db.run {
      for {
        userIds ← UserRepo.allIds
        _ ← DBIO.sequence(userIds map { userId ⇒
          for {
            authIds ← AuthIdRepo.findIdByUserId(userId)
            oldestOpt ← maxSeq(authIds)
            copied ← oldestOpt map (fill(userId) _) getOrElse DBIO.successful(0)
          } yield log.warn(s"Copied $copied rows")
        })
      } yield ()
    }, 1.hour)
  }

  private def maxSeq(authIds: Seq[Long]): DBIO[Option[Long]] = {
    if (authIds.isEmpty) DBIO.successful(None)
    else
      for {
        seqs ← DBIO.sequence(authIds map (a ⇒ getSeq(a) map (a → _)))
      } yield Some(seqs maxBy (_._2 getOrElse 0) _1)
  }

  private def fill(userId: Int)(oldestAuthId: Long): DBIO[Int] = {
    for {
      obsoletes ← sql"""SELECT auth_id, timestamp, seq, header, serialized_data, user_ids_str, group_ids_str FROM seq_updates_ngen WHERE auth_id = $oldestAuthId"""
        .as[Obsolete]
      affected ← move(userId, obsoletes)
    } yield affected
  }

  private def move(userId: Int, obsoletes: Vector[Obsolete]): DBIO[Int] = {
    DBIO.sequence(bulks(obsoletes, Vector.empty) map { bulkObs ⇒
      val values = bulkObs.zipWithIndex.map {
        case (obs, i) ⇒
          val seq = obs.seq
          val timestamp = obs.timestamp
          val mapping = UpdateMapping(
            default = Some(SerializedUpdate(
              header = obs.header,
              body = ByteString.copyFrom(obs.data),
              userIds = obs.userIds.split(",").map(_.toInt).toSeq,
              groupIds = obs.groupIds.split(",").map(_.toInt).toSeq
            ))
          ).toByteArray

          if (i != BulkSize - 1)
            sql"""VALUES($userId, $seq, $timestamp, $mapping), """
          else
            sql"""VALUES($userId, $seq, $timestamp, $mapping)"""
      }.foldLeft(Seq.empty[Any])(_ ++ _.queryParts)

      val insertBase = sql"""INSERT INTO user_sequence (user_id, seq, timestamp, mapping) """

      insertBase.copy(queryParts = insertBase.queryParts ++ values).asUpdate
    }) map (_ ⇒ obsoletes.length)

  }

  @tailrec
  private def bulks(obsoletes: Vector[Obsolete], result: Vector[Vector[Obsolete]]): Vector[Vector[Obsolete]] = {
    obsoletes.splitAt(BulkSize) match {
      case (bulk, Vector()) ⇒ result :+ bulk
      case (bulk, tail)     ⇒ bulks(tail, result :+ bulk)
    }
  }

  private def getSeq(authId: Long) = sql"""SELECT seq FROM seq_updates_ngen ORDER BY WHERE auth_id = $authId LIMIT 1""".as[Int].headOption
}