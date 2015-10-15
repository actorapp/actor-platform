package im.actor.server.migrations

import akka.actor.{ ActorLogging, ActorSystem, PoisonPill, Props }
import akka.persistence.{ PersistentActor, RecoveryCompleted }
import im.actor.concurrent.FutureExt._
import im.actor.server.db.DbExtension
import im.actor.server.event.TSEvent
import im.actor.server.group.GroupOffice
import im.actor.server.{ persist ⇒ p }
import slick.driver.PostgresDriver

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future, Promise }
import scala.util.{ Failure, Success }

object HiddenGroupMigrator extends Migration {

  private case object Migrate

  override protected def migrationName: String = "PutHiddenGroupsToSQL"

  override protected def migrationTimeout: Duration = 15.minutes

  override protected def startMigration()(implicit system: ActorSystem, db: PostgresDriver.api.Database, ec: ExecutionContext): Future[Unit] = {
    for {
      ids ← db.run(p.Group.findAllIds)
      _ ← ftraverse(ids)(migrateGroup)
    } yield ()
  }

  private def migrateGroup(id: Int)(implicit system: ActorSystem, ec: ExecutionContext): Future[Unit] = {
    val promise = Promise[Unit]()
    system.actorOf(Props(new HiddenGroupMigrator(promise, id)), s"hidden_group_migrator_$id")
    promise.future onFailure {
      case e ⇒ system.log.error(e, s"Failed to migrate $id")
    }
    promise.future
  }
}

private final class HiddenGroupMigrator(promise: Promise[Unit], groupId: Int) extends PersistentActor with ActorLogging {

  import HiddenGroupMigrator._
  import im.actor.server.group.GroupEvents._
  import context.dispatcher

  val db = DbExtension(context.system).db

  override def persistenceId = GroupOffice.persistenceIdFor(groupId)

  var isHidden = false

  private def migrate(): Unit = {
    if (isHidden) {
      db.run(p.Group.makeHidden(groupId)) onComplete {
        case Failure(e) ⇒
          promise.failure(e)
          self ! PoisonPill
        case Success(_) ⇒
          promise.success(())
          self ! PoisonPill
      }
    } else {
      promise.success(())
      context stop self
    }
  }

  def receiveCommand = {
    case Migrate ⇒
      migrate()
  }

  def receiveRecover = {
    case TSEvent(_, e: Created) ⇒
      isHidden = e.isHidden.getOrElse(false)
    case RecoveryCompleted ⇒
      self ! Migrate
  }
}