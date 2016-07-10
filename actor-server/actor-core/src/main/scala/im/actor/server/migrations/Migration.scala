package im.actor.server.migrations

import java.time.Instant

import akka.actor.{ Actor, ActorLogging, ActorSystem }
import akka.http.scaladsl.util.FastFuture
import akka.persistence.PersistentActor
import akka.util.Timeout
import im.actor.config.ActorConfig
import im.actor.server.KeyValueMappings
import shardakka.{ InstantCodec, ShardakkaExtension }

import scala.concurrent.duration._
import scala.concurrent.{ Await, Future, Promise }

trait Migration {

  protected def migrationName: String

  protected def migrationTimeout: Duration

  protected def startMigration()(implicit system: ActorSystem): Future[Unit]

  def migrate()(implicit system: ActorSystem): Unit = {
    import system.dispatcher
    implicit val kvTimeout = Timeout(ActorConfig.defaultTimeout)
    val migrations = ShardakkaExtension(system).simpleKeyValue[Instant](KeyValueMappings.Migrations, InstantCodec)
    Await.result(migrations.get(migrationName) flatMap {
      case Some(date) ⇒
        system.log.debug(s"Migration $migrationName will not run. Already completed at $date")
        FastFuture.successful(())
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

abstract class Migrator(promise: Promise[Unit]) extends Actor with ActorLogging {
  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    log.error(reason, "Migrator failure")
    super.preRestart(reason, message)
    promise.failure(reason)
  }
}

abstract class PersistentMigrator(promise: Promise[Unit]) extends Migrator(promise) with PersistentActor {
  override protected def onRecoveryFailure(cause: Throwable, event: Option[Any]): Unit = {
    super.onRecoveryFailure(cause, event)
    promise.failure(cause)
  }
}
