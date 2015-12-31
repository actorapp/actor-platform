package im.actor.server.migrations

import akka.actor._
import akka.util.Timeout
import im.actor.server.persist
import im.actor.server.user.UserExtension
import slick.driver.PostgresDriver.api._

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future, Promise }
import scala.util.{ Failure, Success }

object LocalNamesMigrator extends Migration {

  private val Email = "email"
  private val Phone = "phone"

  protected override def migrationName = "2015-08-20-LocalNamesMigration"

  protected override def migrationTimeout = 1.hour

  protected override def startMigration()(implicit system: ActorSystem, db: Database, ec: ExecutionContext) = {
    system.log.warning("Migrating local names")

    val actions = DBIO.sequence(Seq(
      persist.contact.UserEmailContactRepo.econtacts.filter(!_.isDeleted).map(c ⇒ (c.ownerUserId, c.contactUserId, Email)).result,
      persist.contact.UserPhoneContactRepo.pcontacts.filter(!_.isDeleted).map(c ⇒ (c.ownerUserId, c.contactUserId, Phone)).result
    ))

    db.run(actions) flatMap (contacts ⇒ Future.sequence(contacts.flatten map migrateSingle)) map (_ ⇒ ())
  }

  private def migrateSingle(ownerAndContactAndType: (Int, Int, String))(implicit system: ActorSystem, db: Database): Future[Unit] = {
    val promise = Promise[Unit]()
    val (ownerUserId, contactUserId, typ) = ownerAndContactAndType
    system.actorOf(props(promise, ownerUserId, contactUserId), name = s"migrate_local_name_${ownerUserId}_${contactUserId}_${typ}")
    promise.future
  }

  private def props(promise: Promise[Unit], ownerUserId: Int, contactUserId: Int)(implicit db: Database) =
    Props(classOf[LocalNamesMigrator], promise, ownerUserId, contactUserId, db)
}

private final class LocalNamesMigrator(promise: Promise[Unit], ownerUserId: Int, contactUserId: Int, db: Database) extends Migrator(promise) {

  private implicit val system: ActorSystem = context.system
  private implicit val ec: ExecutionContext = context.dispatcher
  private implicit val timeout: Timeout = Timeout(40.seconds)

  private val userExt = UserExtension(context.system)

  override def receive: Receive = Actor.emptyBehavior

  db.run(for {
    contact ← persist.contact.UserContactRepo.find(ownerUserId, contactUserId)
    user ← persist.UserRepo.find(contactUserId)
  } yield (contact, user)) foreach {
    case (Some(contact), Some(user)) ⇒
      (if (contact.name.contains(user.name)) {
        db.run(persist.contact.UserContactRepo.updateName(ownerUserId, contactUserId, None))
      } else {
        contact.name map (_ ⇒ userExt.editLocalName(ownerUserId, contactUserId, contact.name, supressUpdate = true)) getOrElse Future.successful(())
      }) onComplete {
        case Success(_) ⇒
          log.debug(s"Migrated contact with ownerUserId: $ownerUserId, contactUserId: $contactUserId")
          promise.success(())
          self ! PoisonPill
        case Failure(e) ⇒
          log.error(e, s"Failed to migrate user contact with ownerUserId: $ownerUserId, contactUserId: $contactUserId")
          promise.failure(new Exception(s"Failed to create local name for contact with ownerUserId: $ownerUserId, contactUserId: $contactUserId"))
          self ! PoisonPill
      }
    case _ ⇒
      log.error("User contact not found")
      promise.failure(new Exception(s"Could not find contact with ownerUserId: $ownerUserId, contactUserId: $contactUserId"))
      self ! PoisonPill
  }
}
