package im.actor.server.group

import java.time.ZoneOffset

import scala.concurrent.duration._
import scala.concurrent.{ Await, ExecutionContext, Future, Promise }

import akka.actor.{ ActorLogging, ActorSystem, Props }
import akka.pattern.pipe
import akka.persistence.{ PersistentActor, RecoveryCompleted }
import org.joda.time.DateTime
import slick.driver.PostgresDriver.api._

import im.actor.server.file.{ Avatar, AvatarImage, FileLocation }
import im.actor.server.{ models, persist ⇒ p }

private final case class Migrate(group: models.FullGroup, avatarData: Option[models.AvatarData], botUsers: Seq[models.GroupBot], groupUsers: Seq[models.GroupUser])

object GroupMigrator {
  def migrateAll()(implicit system: ActorSystem, db: Database, ec: ExecutionContext): Unit = {
    Await.result(
      db.run(p.User.allIds) flatMap (ids ⇒ Future.sequence(ids map (migrate))) map (_ ⇒ ()),
      1.hour
    )
  }

  private def migrate(groupId: Int)(implicit system: ActorSystem, db: Database): Future[Unit] = {
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
        }
      case None ⇒
        log.error("Group not found")
        promise.failure(new Exception(s"Cannot find group ${groupId}"))
    }
  }

  override def receiveCommand: Receive = {
    case Migrate(group, avatarDataOpt, botUsers, users) ⇒
      val created: GroupEvent = Created(group.createdAt, group.id, group.creatorUserId, group.accessHash, group.title)

      val botAdded: Vector[GroupEvent] = botUsers.toVector map { bu ⇒
        BotAdded(group.createdAt, bu.userId, bu.token)
      }

      val becamePublic: Vector[GroupEvent] =
        if (group.isPublic)
          Vector(BecamePublic(group.createdAt))
        else
          Vector.empty

      val (userAdded, userJoined): (Vector[GroupEvent], Vector[GroupEvent]) = (users.toVector map { gu ⇒
        (UserInvited(gu.invitedAt, gu.userId, gu.inviterUserId),
          gu.joinedAt map (ts ⇒ UserJoined(new DateTime(ts.toInstant(ZoneOffset.UTC).getEpochSecond()), gu.userId, gu.inviterUserId)))
      }).unzip match {
        case (i, j) ⇒ (i, j.flatten)
      }

      val avatarUpdated: Vector[GroupEvent] = avatarDataOpt match {
        case Some(models.AvatarData(_, _,
          Some(smallFileId), Some(smallFileHash), Some(smallFileSize),
          Some(largeFileId), Some(largeFileHash), Some(largeFileSize),
          Some(fullFileId), Some(fullFileHash), Some(fullFileSize),
          Some(fullWidth), Some(fullHeight))) ⇒
          Vector(AvatarUpdated(group.avatarChangedAt, Some(Avatar(
            Some(AvatarImage(FileLocation(smallFileId, smallFileHash), 100, 100, smallFileSize.toLong)),
            Some(AvatarImage(FileLocation(largeFileId, largeFileHash), 200, 200, largeFileSize.toLong)),
            Some(AvatarImage(FileLocation(fullFileId, fullFileHash), fullWidth, fullHeight, fullFileSize.toLong))
          ))))
        case _ ⇒ Vector.empty
      }

      val events: Vector[GroupEvent] = created +: (botAdded ++ becamePublic ++ userAdded ++ userJoined ++ avatarUpdated).toVector

      persistAsync(events)(identity)

      defer("migrated") { _ ⇒
        promise.success(())
        context stop self
      }
  }

  private[this] var migrationNeeded = true

  override def receiveRecover: Receive = {
    case e: Created ⇒
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
