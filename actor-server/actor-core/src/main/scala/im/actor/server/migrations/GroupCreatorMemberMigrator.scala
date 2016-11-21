package im.actor.server.migrations

import java.time.Instant

import akka.actor.{ ActorSystem, Props }
import akka.persistence.RecoveryCompleted
import im.actor.server.db.DbExtension
import im.actor.server.event.TSEvent
import im.actor.server.group.{ GroupEvents, GroupProcessor }
import im.actor.server.persist.GroupRepo

import scala.concurrent.{ Future, Promise }
import scala.concurrent.duration._

object GroupCreatorMemberMigrator extends Migration {
  private case object Migrate

  protected override def migrationName = "2015-08-29-GroupCreatorMemberMigration"

  protected override def migrationTimeout = 1.hour

  protected override def startMigration()(implicit system: ActorSystem): Future[Unit] = {
    import system.dispatcher

    DbExtension(system).db.run(GroupRepo.findAllIds) flatMap { groupIds ⇒
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

private final class GroupCreatorMemberMigrator(promise: Promise[Unit], groupId: Int) extends PersistentMigrator(promise) {
  import GroupCreatorMemberMigrator._
  import GroupEvents._

  override def persistenceId = GroupProcessor.persistenceIdFor(groupId)

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
      if (creatorUserIdOpt.contains(e.userId)) {
        creatorUserIdOpt = None
      }
    case TSEvent(_, e: UserKicked) ⇒
      if (creatorUserIdOpt.contains(e.userId)) {
        creatorUserIdOpt = None
      }
    case RecoveryCompleted ⇒
      self ! Migrate
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
        persist(UserInvited(Instant.now(), creatorUserId, creatorUserId))(identity)
        persist(UserJoined(Instant.now(), creatorUserId, creatorUserId)) { _ ⇒
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
