package im.actor.server.migrations

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import slick.driver.PostgresDriver
import sql.migration.V20151108011300__FillUserSequence

import scala.concurrent.duration._
import scala.concurrent.{ Future, ExecutionContext }

object FillUserSequenceMigrator extends Migration {
  override protected def migrationName: String = "2015-11-08-FillUserSequence"

  override protected def migrationTimeout: Duration = 1.hour

  override protected def startMigration()(implicit system: ActorSystem, db: PostgresDriver.api.Database, ec: ExecutionContext): Future[Unit] = {
    implicit val mat = ActorMaterializer()
    val migration = new V20151108011300__FillUserSequence
    Future(migration.migrate())
  }
}