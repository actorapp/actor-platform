package im.actor.server

import java.time.Instant

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{ Seconds, Span }
import org.scalatest.{ FlatSpecLike, Inside, Matchers }

import scala.concurrent.ExecutionContext
import im.actor.server.db.DbExtension
import im.actor.server.migrations.v2.{ MigrationNameList, MigrationTsActions }

abstract class BaseAppSuite(_system: ActorSystem = {
                              ActorSpecification.createSystem()
                            })
  extends ActorSuite(_system)
  with FlatSpecLike
  with ScalaFutures
  with MessagingSpecHelpers
  with Matchers
  with Inside
  with ServiceSpecMatchers
  with ServiceSpecHelpers
  with ActorSerializerPrepare {

  protected implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit lazy val ec: ExecutionContext = _system.dispatcher

  protected implicit lazy val (db, conn) = {
    DbExtension(_system).clean()
    DbExtension(_system).migrate()
    val ext = DbExtension(_system)
    (ext.db, ext.connector)
  }

  system.log.debug("Writing migration timestamps")
  MigrationTsActions.insertTimestamp(
    MigrationNameList.MultiSequence,
    Instant.now.toEpochMilli
  )(conn)
  MigrationTsActions.insertTimestamp(
    MigrationNameList.GroupsV2,
    Instant.now.toEpochMilli
  )(conn)

  override implicit def patienceConfig: PatienceConfig =
    new PatienceConfig(timeout = Span(15, Seconds))

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    db
  }
}
