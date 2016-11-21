package im.actor.server.group

import java.time.ZoneOffset

import im.actor.server.migrations.{ Migration, PersistentMigrator }

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future, Promise }
import akka.actor.{ ActorSystem, Props }
import akka.pattern.pipe
import akka.persistence.RecoveryCompleted
import com.github.ghik.silencer.silent
import im.actor.server.db.DbExtension
import org.joda.time.DateTime
import im.actor.server.event.TSEvent
import im.actor.server.file.{ Avatar, AvatarImage, FileLocation }
import im.actor.server.model
import im.actor.server.persist.{ AvatarDataRepo, GroupBotRepo, GroupRepo, GroupUserRepo }

private final case class Migrate(group: model.FullGroup, avatarData: Option[model.AvatarData], botUsers: Seq[model.GroupBot], groupUsers: Seq[model.GroupUser])

object GroupMigrator extends Migration {

  protected override def migrationName: String = "2015-08-04-GroupsMigration"

  protected override def migrationTimeout: Duration = 1.hour

  protected override def startMigration()(implicit system: ActorSystem): Future[Unit] = {
    import system.dispatcher
    DbExtension(system).db.run(GroupRepo.findAllIds) flatMap (ids ⇒ Future.sequence(ids map migrateSingle)) map (_ ⇒ ())
  }

  private def migrateSingle(groupId: Int)(implicit system: ActorSystem): Future[Unit] = {
    val promise = Promise[Unit]()

    system.actorOf(props(promise, groupId), name = s"migrate_group_${groupId}")
    promise.future
  }

  private def props(promise: Promise[Unit], groupId: Int) = Props(classOf[GroupMigrator], promise, groupId)
}

private final class GroupMigrator(promise: Promise[Unit], groupId: Int) extends PersistentMigrator(promise) {

  import GroupEvents._

  private implicit val ec: ExecutionContext = context.dispatcher
  private val db = DbExtension(context.system).db

  override def persistenceId = GroupProcessor.persistenceIdFor(groupId)

  private def migrate(): Unit = {
    db.run(GroupRepo.findFull(groupId): @silent) foreach {
      case Some(group) ⇒
        db.run(for {
          avatarOpt ← AvatarDataRepo.findByGroupId(groupId)
          bots ← (GroupBotRepo.findByGroup(groupId): @silent) map (_.map(Seq(_)).getOrElse(Seq.empty))
          users ← GroupUserRepo.find(groupId): @silent
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

      val created = Created(group.createdAt, group.id, Some(GroupType.General), group.creatorUserId, group.accessHash, group.title)

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
          gu.joinedAt map (ts ⇒ UserJoined(ts.toInstant(ZoneOffset.UTC), gu.userId, gu.inviterUserId)))
      }).unzip match {
        case (i, j) ⇒ (i, j.flatten)
      }

      val avatarUpdated: Vector[GroupEvent] = avatarDataOpt match {
        case Some(model.AvatarData(_, _,
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

      val events: Vector[GroupEvent] = created +: (botAdded ++ becamePublic ++ userAdded ++ userJoined ++ avatarUpdated)

      persistAllAsync(events)(identity)

      deferAsync(TSEvent(new DateTime, "migrated")) { _ ⇒
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
