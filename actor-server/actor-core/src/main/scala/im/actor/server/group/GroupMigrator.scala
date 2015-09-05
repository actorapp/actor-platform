package im.actor.server.group

import java.time.ZoneOffset
import im.actor.server.migrations.Migration
import slick.driver.PostgresDriver

import scala.concurrent.duration._
import scala.concurrent.{ Await, ExecutionContext, Future, Promise }

import akka.actor.{ ActorLogging, ActorSystem, Props }
import akka.pattern.pipe
import akka.persistence.{ PersistentActor, RecoveryCompleted }
import org.joda.time.DateTime
import slick.driver.PostgresDriver.api._

import im.actor.server.event.TSEvent
import im.actor.server.file.{ Avatar, AvatarImage, FileLocation }
import im.actor.server.{ persist ⇒ p, models }

private final case class Migrate(group: models.FullGroup, avatarData: Option[models.AvatarData], botUsers: Seq[models.GroupBot], groupUsers: Seq[models.GroupUser])

object GroupMigrator extends Migration {

  protected override def migrationName: String = "2015-08-04-GroupsMigration"

  protected override def migrationTimeout: Duration = 1.hour

  protected override def startMigration()(implicit system: ActorSystem, db: PostgresDriver.api.Database, ec: ExecutionContext): Future[Unit] = {
    db.run(p.Group.allIds) flatMap (ids ⇒ Future.sequence(ids map migrateSingle)) map (_ ⇒ ())
  }

  private def migrateSingle(groupId: Int)(implicit system: ActorSystem, db: Database): Future[Unit] = {
    val promise = Promise[Unit]()

    system.actorOf(props(promise, groupId), name = s"migrate_group_${groupId}")
    promise.future
  }

  private def props(promise: Promise[Unit], groupId: Int)(implicit db: Database) = Props(classOf[GroupMigrator], promise, groupId, db)
}

private final class GroupMigrator(promise: Promise[Unit], groupId: Int, db: Database) extends PersistentActor with ActorLogging {

  import GroupEvents._

  private implicit val ec: ExecutionContext = context.dispatcher

  override def persistenceId = GroupOffice.persistenceIdFor(groupId)

  private def migrate(): Unit = {
    db.run(p.Group.findFull(groupId)) foreach {
      case Some(group) ⇒
        db.run(for {
          avatarOpt ← p.AvatarData.findByGroupId(groupId)
          bots ← p.GroupBot.findByGroup(groupId) map (_.map(Seq(_)).getOrElse(Seq.empty))
          users ← p.GroupUser.find(groupId)
        } yield Migrate(
          group = group,
          avatarData = avatarOpt,
          botUsers = bots,
          groupUsers = users
        )) pipeTo self onFailure {
          case e ⇒
            log.error(e, "Failed to migrate group")
            promise.failure(e)
            context stop self
        }
      case None ⇒
        log.error("Group not found")
        promise.failure(new Exception(s"Cannot find group ${groupId}"))
        context stop self
    }
  }

  override def receiveCommand: Receive = {
    case m @ Migrate(group, avatarDataOpt, botUsers, users) ⇒
      log.info("Migrate: {}", m)

      val created: TSEvent = TSEvent(group.createdAt, Created(group.id, Some(GroupType.General), group.creatorUserId, group.accessHash, group.title))

      val botAdded: Vector[TSEvent] = botUsers.toVector map { bu ⇒
        TSEvent(group.createdAt, BotAdded(bu.userId, bu.token))
      }

      val becamePublic: Vector[TSEvent] =
        if (group.isPublic)
          Vector(TSEvent(group.createdAt, BecamePublic()))
        else
          Vector.empty

      val (userAdded, userJoined): (Vector[TSEvent], Vector[TSEvent]) = (users.toVector map { gu ⇒
        (TSEvent(gu.invitedAt, UserInvited(gu.userId, gu.inviterUserId)),
          gu.joinedAt map (ts ⇒ TSEvent(new DateTime(ts.toInstant(ZoneOffset.UTC).getEpochSecond() * 1000), UserJoined(gu.userId, gu.inviterUserId))))
      }).unzip match {
        case (i, j) ⇒ (i, j.flatten)
      }

      val avatarUpdated: Vector[TSEvent] = avatarDataOpt match {
        case Some(models.AvatarData(_, _,
          Some(smallFileId), Some(smallFileHash), Some(smallFileSize),
          Some(largeFileId), Some(largeFileHash), Some(largeFileSize),
          Some(fullFileId), Some(fullFileHash), Some(fullFileSize),
          Some(fullWidth), Some(fullHeight))) ⇒
          Vector(TSEvent(group.avatarChangedAt, AvatarUpdated(Some(Avatar(
            Some(AvatarImage(FileLocation(smallFileId, smallFileHash), 100, 100, smallFileSize.toLong)),
            Some(AvatarImage(FileLocation(largeFileId, largeFileHash), 200, 200, largeFileSize.toLong)),
            Some(AvatarImage(FileLocation(fullFileId, fullFileHash), fullWidth, fullHeight, fullFileSize.toLong))
          )))))
        case _ ⇒ Vector.empty
      }

      val events: Vector[TSEvent] = created +: (botAdded ++ becamePublic ++ userAdded ++ userJoined ++ avatarUpdated).toVector

      persistAsync(events)(identity)

      defer(TSEvent(new DateTime, "migrated")) { _ ⇒
        log.info("Migrated")
        promise.success(())
        context stop self
      }
  }

  private[this] var migrationNeeded = true

  override def receiveRecover: Receive = {
    case TSEvent(_, _: Created) ⇒
      migrationNeeded = false
    case RecoveryCompleted ⇒
      if (migrationNeeded) {
        migrate()
      } else {
        promise.success(())
        context stop self
      }
  }
}
