package sql.migration

import java.sql.{ SQLException, BatchUpdateException, Connection }

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
import scala.util.{ Failure, Success }

object V20151108011300__FillUserSequence {
  final case class Obsolete(authId: Long, timestamp: Long, seq: Int, header: Int, data: Array[Byte], userIds: String, groupIds: String)
  final case class New(userId: Int, seq: Int, timestamp: Long, mapping: Array[Byte])

  final class UserSequenceTable(tag: Tag) extends Table[New](tag, "user_sequence") {
    def userId = column[Int]("user_id", O.PrimaryKey)
    def seq = column[Int]("seq", O.PrimaryKey)
    def timestamp = column[Long]("timestamp")
    def mapping = column[Array[Byte]]("mapping")

    def * = (userId, seq, timestamp, mapping) <> (New.tupled, New.unapply)
  }

  val newTable = TableQuery[UserSequenceTable]

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
    connection.setAutoCommit(false)
    val wrappedConn = new DelegateConnection(connection) {
      override def close(): Unit = ()
    }

    val db = Database.forSource(new JdbcDataSource {
      def createConnection(): Connection = wrappedConn

      def close(): Unit = ()
    })

    try {
      log.warn("Starting filling user sequence")
      Await.result(db.run({
        SimpleDBIO(_.connection.setAutoCommit(false))
        for {
          userIds ← UserRepo.allIds map (_.toIterator)
          _ = log.warn(s"Found users: ${userIds}")
          affected ← DBIO.sequence(userIds map migrateUser) map (_.sum)
        } yield {
          log.warn(s"${affected} updates moved")
        }
      }.transactionally), 1.hour)
    } catch {
      case e: Exception ⇒
        log.error("Failed to migrate", e)
        throw e
    }
    connection.setAutoCommit(false)
  }

  private def migrateUser(userId: Int): DBIO[Int] = {
    log.warn(s"Moving user: ${userId}")
    (for {
      authIds ← AuthIdRepo.findIdByUserId(userId)
      _ = log.warn(s"Found ${authIds.length} authIds")
      oldestOpt ← maxSeq(authIds)
      copied ← oldestOpt map (fill(userId) _) getOrElse DBIO.successful(0)
    } yield {
      log.warn(s"Copied $copied updates")
      copied
    }).asTry map {
      case Success(res) ⇒ res
      case Failure(err: SQLException) ⇒
        log.error("Failed to move user", err.getNextException)
        throw err
      case Failure(err) ⇒
        log.error("Failed to move user", err)
        throw err
    }
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
      val news = bulkObs map {
        case Obsolete(_, timestamp, seq, header, data, userIds, groupIds) ⇒
          New(
            userId = userId,
            seq = seq,
            timestamp = timestamp,
            mapping = UpdateMapping(
            default = Some(SerializedUpdate(
              header = header,
              body = ByteString.copyFrom(data),
              userIds = userIds.split(",").view.filter(_.nonEmpty).map(_.toInt).toSeq,
              groupIds = groupIds.split(",").view.filter(_.nonEmpty).map(_.toInt).toSeq
            ))
          ).toByteArray
          )
      }

      newTable ++= news
    }) map (_ ⇒ obsoletes.length)
  }

  @tailrec
  private def bulks(obsoletes: Vector[Obsolete], result: Vector[Vector[Obsolete]]): Vector[Vector[Obsolete]] = {
    obsoletes.splitAt(BulkSize) match {
      case (bulk, Vector()) ⇒ result :+ bulk
      case (bulk, tail)     ⇒ bulks(tail, result :+ bulk)
    }
  }

  private def getSeq(authId: Long) = sql"""SELECT seq FROM seq_updates_ngen WHERE auth_id = $authId ORDER BY seq DESC LIMIT 1""".as[Int].headOption
}