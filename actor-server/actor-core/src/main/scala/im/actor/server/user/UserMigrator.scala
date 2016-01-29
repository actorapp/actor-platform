package im.actor.server.user

import java.time.{ Instant, ZoneOffset }

import akka.actor.{ ActorSystem, Props }
import akka.pattern.pipe
import akka.persistence.RecoveryCompleted
import im.actor.api.rpc.users.ApiSex
import im.actor.server.event.TSEvent
import im.actor.server.file.{ Avatar, AvatarImage, FileLocation }
import im.actor.server.migrations.{ PersistentMigrator, Migration }
import im.actor.server.{ model, persist ⇒ p }
import org.joda.time.DateTime
import slick.driver.PostgresDriver
import slick.driver.PostgresDriver.api._

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future, Promise }

private final case class Migrate(
  accessSalt:  String,
  name:        String,
  countryCode: String,
  sex:         model.Sex,
  isBot:       Boolean,
  createdAt:   Instant,
  authIds:     Seq[Long],
  phones:      Seq[model.UserPhone],
  emails:      Seq[model.UserEmail],
  avatarOpt:   Option[model.AvatarData]
)

object UserMigrator extends Migration {

  protected override def migrationName: String = "2015-08-04-UsersMigration"

  protected override def migrationTimeout: Duration = 1.hour

  protected override def startMigration()(implicit system: ActorSystem, db: PostgresDriver.api.Database, ec: ExecutionContext): Future[Unit] = {
    db.run(p.UserRepo.allIds) flatMap (ids ⇒ Future.sequence(ids map migrateSingle)) map (_ ⇒ ())
  }

  private def migrateSingle(userId: Int)(implicit system: ActorSystem, db: Database): Future[Unit] = {
    val promise = Promise[Unit]()

    system.actorOf(props(promise, userId), name = s"migrate_user_${userId}")
    promise.future
  }

  private def props(promise: Promise[Unit], userId: Int)(implicit db: Database) = Props(classOf[UserMigrator], promise, userId, db)
}

private final class UserMigrator(promise: Promise[Unit], userId: Int, db: Database) extends PersistentMigrator(promise) {

  import UserEvents._

  private implicit val ec: ExecutionContext = context.dispatcher

  override def persistenceId = UserOffice.persistenceIdFor(userId)

  def migrate(): Unit = {
    db.run(p.UserRepo.find(userId)) foreach {
      case Some(user) ⇒
        db.run(for {
          avatarOpt ← p.AvatarDataRepo.findByUserId(userId).headOption
          authIds ← p.AuthIdRepo.findIdByUserId(userId)
          phones ← p.UserPhoneRepo.findByUserId(userId)
          emails ← p.UserEmailRepo.findByUserId(userId)
        } yield Migrate(
          user.accessSalt,
          user.name,
          user.countryCode,
          user.sex,
          user.isBot,
          user.createdAt.toInstant(ZoneOffset.UTC),
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
      val created = Created(createdAt, userId, accessSalt, None, name, countryCode, ApiSex(sex.toInt), isBot)
      val authAdded = authIds map (a ⇒ AuthAdded(createdAt, a))
      val phoneAdded = phones map (p ⇒ PhoneAdded(createdAt, p.number))
      val emailAdded = emails map (e ⇒ EmailAdded(createdAt, e.email))
      val avatarUpdated = avatarOpt match {
        case Some(model.AvatarData(_, _,
          Some(smallFileId), Some(smallFileHash), Some(smallFileSize),
          Some(largeFileId), Some(largeFileHash), Some(largeFileSize),
          Some(fullFileId), Some(fullFileHash), Some(fullFileSize),
          Some(fullWidth), Some(fullHeight))) ⇒
          Vector(AvatarUpdated(createdAt, Some(Avatar(
            Some(AvatarImage(FileLocation(smallFileId, smallFileHash), 100, 100, smallFileSize.toLong)),
            Some(AvatarImage(FileLocation(largeFileId, largeFileHash), 200, 200, largeFileSize.toLong)),
            Some(AvatarImage(FileLocation(fullFileId, fullFileHash), fullWidth, fullHeight, fullFileSize.toLong))
          ))))
        case _ ⇒ Vector.empty
      }

      val events: Vector[UserEvent] = (created +: (authAdded ++ phoneAdded ++ emailAdded ++ avatarUpdated)).toVector

      persistAllAsync(events)(identity)

      deferAsync(TSEvent(new DateTime(), "migrated")) { _ ⇒
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
  }
}
