package im.actor.server.user

import java.time.ZoneOffset

import akka.actor.{ ActorLogging, ActorSystem, Props }
import akka.pattern.pipe
import akka.persistence.{ PersistentActor, RecoveryCompleted, RecoveryFailure }
import im.actor.api.rpc.users.ApiSex
import im.actor.server.event.TSEvent
import im.actor.server.file.{ Avatar, AvatarImage, FileLocation }
import im.actor.server.migrations.Migration
import im.actor.server.{ models, persist ⇒ p }
import org.joda.time.DateTime
import slick.driver.PostgresDriver
import slick.driver.PostgresDriver.api._

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future, Promise }

private final case class Migrate(
  accessSalt:  String,
  name:        String,
  countryCode: String,
  sex:         models.Sex,
  isBot:       Boolean,
  createdAt:   DateTime,
  authIds:     Seq[Long],
  phones:      Seq[models.UserPhone],
  emails:      Seq[models.UserEmail],
  avatarOpt:   Option[models.AvatarData]
)

object UserMigrator extends Migration {

  protected override def migrationName: String = "2015-08-04-UsersMigration"

  protected override def migrationTimeout: Duration = 1.hour

  protected override def startMigration()(implicit system: ActorSystem, db: PostgresDriver.api.Database, ec: ExecutionContext): Future[Unit] = {
    db.run(p.User.allIds) flatMap (ids ⇒ Future.sequence(ids map migrateSingle)) map (_ ⇒ ())
  }

  private def migrateSingle(userId: Int)(implicit system: ActorSystem, db: Database): Future[Unit] = {
    val promise = Promise[Unit]()

    system.actorOf(props(promise, userId), name = s"migrate_user_${userId}")
    promise.future
  }

  private def props(promise: Promise[Unit], userId: Int)(implicit db: Database) = Props(classOf[UserMigrator], promise, userId, db)
}

private final class UserMigrator(promise: Promise[Unit], userId: Int, db: Database) extends PersistentActor with ActorLogging {

  import UserEvents._

  private implicit val ec: ExecutionContext = context.dispatcher

  override def persistenceId = UserOffice.persistenceIdFor(userId)

  def migrate(): Unit = {
    db.run(p.User.find(userId).headOption) foreach {
      case Some(user) ⇒
        db.run(for {
          avatarOpt ← p.AvatarData.findByUserId(userId).headOption
          authIds ← p.AuthId.findIdByUserId(userId)
          phones ← p.UserPhone.findByUserId(userId)
          emails ← p.UserEmail.findByUserId(userId)
        } yield Migrate(
          user.accessSalt,
          user.name,
          user.countryCode,
          user.sex,
          user.isBot,
          new DateTime(user.createdAt.toInstant(ZoneOffset.UTC).getEpochSecond() * 1000),
          authIds,
          phones,
          emails,
          avatarOpt
        )) pipeTo self onFailure {
          case e ⇒
            log.error(e, "Failed to migrate user")
            promise.failure(e)
            context stop self
        }
      case None ⇒
        log.error("User not found")
        promise.failure(new Exception(s"Cannot find user ${userId}"))
        context stop self
    }
  }

  private var migrationNeeded = true

  def receiveCommand: Receive = {
    case m @ Migrate(accessSalt, name, countryCode, sex, isBot, createdAt, authIds, phones, emails, avatarOpt) ⇒
      log.info("Migrate: {}", m)
      val created = TSEvent(createdAt, Created(userId, accessSalt, name, countryCode, ApiSex(sex.toInt), isBot))
      val authAdded = authIds map (a ⇒ TSEvent(createdAt, AuthAdded(a)))
      val phoneAdded = phones map (p ⇒ TSEvent(createdAt, PhoneAdded(p.number)))
      val emailAdded = emails map (e ⇒ TSEvent(createdAt, EmailAdded(e.email)))
      val avatarUpdated = avatarOpt match {
        case Some(models.AvatarData(_, _,
          Some(smallFileId), Some(smallFileHash), Some(smallFileSize),
          Some(largeFileId), Some(largeFileHash), Some(largeFileSize),
          Some(fullFileId), Some(fullFileHash), Some(fullFileSize),
          Some(fullWidth), Some(fullHeight))) ⇒
          Vector(TSEvent(createdAt, AvatarUpdated(Some(Avatar(
            Some(AvatarImage(FileLocation(smallFileId, smallFileHash), 100, 100, smallFileSize.toLong)),
            Some(AvatarImage(FileLocation(largeFileId, largeFileHash), 200, 200, largeFileSize.toLong)),
            Some(AvatarImage(FileLocation(fullFileId, fullFileHash), fullWidth, fullHeight, fullFileSize.toLong))
          )))))
        case _ ⇒ Vector.empty
      }

      val events: Vector[TSEvent] = (created +: (authAdded ++ phoneAdded ++ emailAdded ++ avatarUpdated)).toVector

      persistAsync(events)(identity)

      defer(TSEvent(new DateTime(), "migrated")) { _ ⇒
        log.info("Migrated")
        promise.success(())
        context stop self
      }
  }

  def receiveRecover: Receive = {
    case TSEvent(_, _: Created) ⇒
      migrationNeeded = false
    case RecoveryCompleted ⇒
      if (migrationNeeded) {
        migrate()
      } else {
        promise.success(())
        context stop self
      }
    case RecoveryFailure(e) ⇒
      log.error(e, "Failed to recover user")
      promise.failure(e)
      context stop self
  }
}
