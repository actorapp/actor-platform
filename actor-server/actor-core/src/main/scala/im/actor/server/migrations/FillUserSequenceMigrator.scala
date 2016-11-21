package im.actor.server.migrations

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import sql.migration.V20151108011300__FillUserSequence

import scala.concurrent.duration._
import scala.concurrent.Future

object FillUserSequenceMigrator extends Migration {
  override protected def migrationName: String = "2015-11-11-FillUserSequence"

  override protected def migrationTimeout: Duration = 24.hours

  override protected def startMigration()(implicit system: ActorSystem): Future[Unit] = {
    import system.dispatcher
    implicit val mat = ActorMaterializer()
    val migration = new V20151108011300__FillUserSequence
    Future(migration.migrate())
  }
}
