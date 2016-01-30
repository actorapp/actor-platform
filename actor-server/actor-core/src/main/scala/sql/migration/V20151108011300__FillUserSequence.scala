package sql.migration

import java.sql.SQLException

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl._
import com.google.protobuf.ByteString
import com.typesafe.scalalogging.Logger
import im.actor.server.db.DbExtension
import im.actor.server.model.{ SerializedUpdate, UpdateMapping }
import im.actor.server.persist.{ AuthIdRepo, UserRepo }
import org.slf4j.LoggerFactory
import slick.driver.PostgresDriver.api._
import slick.jdbc.{ GetResult, ResultSetConcurrency, ResultSetType, SetParameter }

import scala.concurrent.duration._
import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.language.postfixOps

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
  val Parallelism = 3
}
//nohup bin/actor-cli migrate-user-sequence > log.log &
final class V20151108011300__FillUserSequence(implicit system: ActorSystem, materializer: Materializer) {
  import V20151108011300__FillUserSequence._

  private val executor = system.dispatcher
  private implicit val ec = ExecutionContext.fromExecutor(executor)
  private val log = Logger(LoggerFactory.getLogger(getClass))
  private val db = DbExtension(system).db

  def migrate(): Unit = {
    try {
      log.warn("Starting filling user sequence")
      val count =
        Await.result({
          Source.fromPublisher(db.stream(UserRepo.allIds))
            .mapAsync(Parallelism) { userId ⇒
              db.run(for {
                authIds ← AuthIdRepo.findIdByUserId(userId)
                _ = log.warn(s"Found ${authIds.length} authIds for ${userId}")
                oldestOpt ← maxSeq(authIds)
              } yield (userId, oldestOpt))
            }
            .map {
              case pair @ (userId, authIdOpt) ⇒
                if (authIdOpt.isEmpty)
                  log.warn(s"User ${userId} haven't any authIds, ignoring")

                pair
            }
            .collect {
              case (userId, Some(authId)) ⇒ (userId, authId)
            }
            .mapAsyncUnordered(Parallelism) {
              case (userId, authId) ⇒
                move(userId, authId)
            }
            .runFold(0)(_ + _)
        }, 48.hours)

      log.warn(s"Migration complete! Moved ${count} updates")
    } catch {
      case e: SQLException ⇒
        log.error("Failed to migrate", e.getNextException())
        throw e.getNextException()
    }
  }

  private def move(userId: Int, authId: Long): Future[Int] = {
    log.warn(s"Moving user $userId")

    db.run(sql"""SELECT seq FROM user_sequence WHERE user_id = $userId ORDER BY seq DESC LIMIT 1""".as[Int]).map(_.headOption.getOrElse(0)) flatMap { startFrom ⇒
      log.warn(s"Starting userId ${userId} from seq: ${startFrom}")
      Source.fromPublisher(
        db.stream(
          sql"""SELECT auth_id, timestamp, seq, header, serialized_data, user_ids_str, group_ids_str FROM seq_updates_ngen WHERE auth_id = $authId and seq > $startFrom ORDER BY timestamp ASC"""
          .as[Obsolete].withStatementParameters(
            rsType = ResultSetType.ForwardOnly,
            rsConcurrency = ResultSetConcurrency.ReadOnly,
            fetchSize = BulkSize
          ).transactionally
        )
      )
        .map {
          case Obsolete(_, timestamp, seq, header, data, userIds, groupIds) ⇒
            log.debug(s"Parsing userId: ${userId}, seq: ${seq}")
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
        .grouped(BulkSize)
        .mapAsync(1) { bulk ⇒
          val action = newTable ++= bulk
          db.run(action) map (_.getOrElse(0))
        }
        .runFold(0)(_ + _)
        .map { count ⇒
          log.warn(s"Moved ${count} updates for user ${userId}")
          count
        }
    }
  }

  private def maxSeq(authIds: Seq[Long]): DBIO[Option[Long]] = {
    if (authIds.isEmpty) DBIO.successful(None)
    else
      for {
        seqs ← DBIO.sequence(authIds map (a ⇒ getSeq(a) map (a → _)))
      } yield Some(seqs maxBy (_._2 getOrElse 0) _1)
  }

  private def getSeq(authId: Long) = sql"""SELECT seq FROM seq_updates_ngen WHERE auth_id = $authId ORDER BY timestamp DESC LIMIT 1""".as[Int].headOption
}
