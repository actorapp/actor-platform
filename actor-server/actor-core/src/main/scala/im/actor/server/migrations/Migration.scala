package im.actor.server.migrations

import java.time.Instant

import akka.actor.ActorSystem
import akka.util.Timeout
import im.actor.server.KeyValueMappings
import shardakka.{ InstantCodec, ShardakkaExtension }
import slick.driver.PostgresDriver.api._

import scala.concurrent.duration._
import scala.concurrent.{ Await, ExecutionContext, Future }

trait Migration {

  protected def migrationName: String

  protected def migrationTimeout: Duration

  protected def startMigration()(implicit system: ActorSystem, db: Database, ec: ExecutionContext): Future[Unit]

  def migrate()(implicit system: ActorSystem, db: Database, ec: ExecutionContext): Unit = {
    implicit val kvTimeout = Timeout(5.seconds)
    val migrations = ShardakkaExtension(system).simpleKeyValue[Instant](KeyValueMappings.Migrations, InstantCodec)
    Await.result(migrations.get(migrationName) flatMap {
      case Some(date) ⇒
        system.log.debug(s"Migration $migrationName will not run. Already completed at $date")
        Future.successful(())
      case _ ⇒
        system.log.warning(s"Migration $migrationName started")
        startMigration() flatMap { _ ⇒
          system.log.info(s"Migration $migrationName finished")
          migrations.upsert(migrationName, Instant.now())
        }
    } recover {
      case e ⇒
        system.log.error(e, s"Migration $migrationName failed!!!")
        throw e
    }, migrationTimeout)
  }

}
