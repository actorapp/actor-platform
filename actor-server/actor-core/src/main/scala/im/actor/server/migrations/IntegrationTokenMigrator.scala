package im.actor.server.migrations

import akka.actor.ActorSystem
import akka.util.Timeout
import im.actor.server.group.GroupErrors.NoBotFound
import im.actor.server.group.{ GroupExtension, GroupOffice, GroupViewRegion }
import im.actor.server.{ KeyValueMappings, persist }
import shardakka.keyvalue.SimpleKeyValue
import shardakka.{ IntCodec, ShardakkaExtension }
import slick.driver.PostgresDriver

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

object IntegrationTokenMigrator extends Migration {

  override protected def migrationName: String = "2015-08-21-IntegrationTokenMigration"

  override protected def migrationTimeout: Duration = 1.hour

  protected override def startMigration()(
    implicit
    system: ActorSystem,
    db:     PostgresDriver.api.Database,
    ec:     ExecutionContext
  ): Future[Unit] = {
    implicit val kv = ShardakkaExtension(system).simpleKeyValue[Int](KeyValueMappings.IntegrationTokens, IntCodec)
    implicit val viewRegion = GroupExtension(system).viewRegion
    db.run(persist.Group.allIds) flatMap { ids ⇒
      system.log.debug("Going to migrate integration tokens for groups: {}", ids)
      Future.sequence(ids map (groupId ⇒ migrateSingle(groupId) recover {
        case NoBotFound ⇒
          system.log.warning("No bot found for groupId: {}", groupId)
        case e ⇒
          system.log.error(e, "Failed to migrate token for groupId: {}", groupId)
          throw e
      }))
    } map (_ ⇒ ())
  }

  private def migrateSingle(groupId: Int)(
    implicit
    system:     ActorSystem,
    ec:         ExecutionContext,
    viewRegion: GroupViewRegion,
    kv:         SimpleKeyValue[Int]
  ): Future[Unit] = {
    implicit val timeout = Timeout(40.seconds)
    for {
      optToken ← GroupOffice.getIntegrationToken(groupId)
      _ ← optToken map { token ⇒ kv.upsert(token, groupId) } getOrElse {
        system.log.warning("Could not find integration token in group {}", groupId)
        Future.successful(())
      }
    } yield {
      system.log.info("Integration token migrated for group {}", groupId)
      ()
    }
  }
}
