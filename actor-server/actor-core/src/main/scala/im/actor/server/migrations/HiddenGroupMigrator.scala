package im.actor.server.migrations

import akka.actor.ActorSystem
import im.actor.server.db.DbExtension
import im.actor.server.group.GroupExtension
import im.actor.server.persist
import slick.driver.PostgresDriver

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

object HiddenGroupMigrator extends Migration {
  override protected def migrationName: String = "PutHiddenGroupsToSQL"

  override protected def migrationTimeout: Duration = 15.minutes

  override protected def startMigration()(implicit system: ActorSystem, db: PostgresDriver.api.Database, ec: ExecutionContext): Future[Unit] = {
    for {
      ids ← db.run(persist.Group.findAllIds)
      _ ← Future.sequence(ids map migrateGroup)
    } yield ()
  }

  private def migrateGroup(id: Int)(implicit system: ActorSystem, ec: ExecutionContext): Future[Unit] = {
    val db = DbExtension(system).db
    val groupExt = GroupExtension(system)
    groupExt.getApiStruct(id, 0) flatMap { group ⇒
      if (group.isHidden.contains(true))
        db.run(persist.Group.makeHidden(id)) map (_ ⇒ ())
      else
        Future.successful(())
    }
  }
}