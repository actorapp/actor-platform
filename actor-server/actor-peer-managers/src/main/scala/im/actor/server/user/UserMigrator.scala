package im.actor.server.user

import java.time.ZoneOffset

import scala.concurrent.{ ExecutionContext, Future, Promise }
import scala.util.Success

import akka.actor.Actor.emptyBehavior
import akka.actor.{ ActorSystem, Props, ActorLogging }
import akka.pattern.pipe
import akka.persistence.{ RecoveryCompleted, PersistentActor, RecoveryFailure }
import org.joda.time.DateTime
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc.users.Sex
import im.actor.server.{ models, persist ⇒ p }

case object MigrateAck extends Serializable

private final case class Migrate(
  accessSalt:  String,
  name:        String,
  countryCode: String,
  sex:         models.Sex,
  isBot:       Boolean,
  createdAt:   DateTime,
  authIds:     Seq[Long],
  phones:      Seq[models.UserPhone],
  emails:      Seq[models.UserEmail]
)

object UserMigrator {
  def migrateAll()(implicit system: ActorSystem, db: Database, ec: ExecutionContext): Future[Unit] = {
    db.run(p.User.allIds) flatMap (ids ⇒ Future.sequence(ids map (migrate))) map (_ ⇒ ())
  }

  private def migrate(userId: Int)(implicit system: ActorSystem, db: Database): Future[Unit] = {
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
          authIds ← p.AuthId.findIdByUserId(userId)
          phones ← p.UserPhone.findByUserId(userId)
          emails ← p.UserEmail.findByUserId(userId)
        } yield Migrate(
          user.accessSalt,
          user.name,
          user.countryCode,
          user.sex,
          user.isBot,
          new DateTime(user.createdAt.toInstant(ZoneOffset.UTC).getEpochSecond()),
          authIds,
          phones,
          emails
        )) pipeTo self onFailure {
          case e ⇒
            log.error(e, "Failed to find user")
            promise.failure(e)
        }
      case None ⇒
        log.error("User not found")
        promise.failure(new Exception(s"Cannot find user ${userId}"))
        context stop self
    }

    context become {
      case Migrate(accessSalt, name, countryCode, sex, isBot, createdAt, authIds, phones, emails) ⇒
        val created = Created(createdAt, userId, accessSalt, name, countryCode, Sex(sex.toInt), isBot)
        val authAdded = authIds map (AuthAdded(createdAt, _))
        val phoneAdded = phones map (p ⇒ PhoneAdded(createdAt, p.number))
        val emailAdded = emails map (e ⇒ EmailAdded(createdAt, e.email))

        val events: Vector[UserEvent] = (created +: (authAdded ++ phoneAdded ++ emailAdded)).toVector

        persistAsync(events)(identity)

        defer("migrated") { _ ⇒
          promise.success(())
          context stop self
        }
      case msg ⇒ stash()
    }
  }

  private var migrationNeeded = true

  def receiveCommand: Receive = emptyBehavior

  def receiveRecover: Receive = {
    case e: Created ⇒
      migrationNeeded = false
    case RecoveryCompleted ⇒
      promise.success(())
    case RecoveryFailure(e) ⇒
      log.error(e, "Failed to recover user")
      promise.failure(e)
  }
}
