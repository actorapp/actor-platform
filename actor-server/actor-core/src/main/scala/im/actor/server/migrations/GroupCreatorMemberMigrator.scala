package im.actor.server.migrations

import akka.actor.{ ActorLogging, Actor, Props, ActorSystem }
import akka.persistence.{ RecoveryFailure, RecoveryCompleted, PersistentActor }
import im.actor.server.event.TSEvent
import im.actor.server.group.{ GroupEvents, GroupOffice }
import org.joda.time.DateTime
import slick.driver.PostgresDriver.api._
import im.actor.server.persist

import scala.concurrent.{ Promise, Future, ExecutionContext }
import scala.concurrent.duration._

object GroupCreatorMemberMigrator extends Migration {
  private case object Migrate

  protected override def migrationName = "2015-08-29-GroupCreatorMemberMigration"

  protected override def migrationTimeout = 1.hour

  protected override def startMigration()(implicit system: ActorSystem, db: Database, ec: ExecutionContext): Future[Unit] = {
    db.run(persist.Group.allIds) flatMap { groupIds ⇒
      Future.sequence(groupIds map { groupId ⇒
        val promise = Promise[Unit]()

        system.actorOf(Props(classOf[GroupCreatorMemberMigrator], promise, groupId), s"migrate_group_creator_member_${groupId}")
        promise.future onFailure {
          case e ⇒ system.log.error(e, s"Failed to migrate ${groupId}")
        }

        promise.future
      }) map (_ ⇒ ())
    }
  }
}

private final class GroupCreatorMemberMigrator(promise: Promise[Unit], groupId: Int) extends PersistentActor with ActorLogging {
  import GroupCreatorMemberMigrator._
  import GroupEvents._

  override def persistenceId = GroupOffice.persistenceIdFor(groupId)

  def receiveCommand = {
    case Migrate ⇒ migrate()
  }

  var originalCreatorUserId: Int = -1
  var creatorUserIdOpt: Option[Int] = None

  def receiveRecover = {
    case TSEvent(_, e: Created) ⇒
      creatorUserIdOpt = Some(e.creatorUserId)
      originalCreatorUserId = e.creatorUserId
    case TSEvent(_, e: UserLeft) ⇒
      if (creatorUserIdOpt.exists(_ == e.userId)) {
        creatorUserIdOpt = None
      }
    case TSEvent(_, e: UserKicked) ⇒
      if (creatorUserIdOpt.exists(_ == e.userId)) {
        creatorUserIdOpt = None
      }
    case RecoveryCompleted ⇒
      self ! Migrate
    case RecoveryFailure(e) ⇒
      log.error(e, "Failed to recover")
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    super.preRestart(reason, message)
    promise.failure(reason)
  }

  private def migrate(): Unit = {
    log.warning("Migrating {}", groupId)
    creatorUserIdOpt match {
      case Some(creatorUserId) ⇒
        log.warning("Adding member {}", creatorUserId)
        persist(TSEvent(new DateTime(), UserInvited(creatorUserId, creatorUserId)))(identity)
        persist(TSEvent(new DateTime(), UserJoined(creatorUserId, creatorUserId))) { _ ⇒
          log.warning("Migrated")
          promise.success(())
          context stop self
        }
      case None ⇒
        log.warning("No migration needed, creator left")
        promise.success(())
        context stop self
    }
  }
}