package im.actor.server.dialog

import java.time.Instant

import akka.actor.Status
import akka.pattern.pipe
import akka.persistence.SnapshotMetadata
import im.actor.server.cqrs.{ Event, Processor }
import im.actor.server.db.DbExtension
import im.actor.server.model.DialogObsolete
import im.actor.server.persist.dialog.DialogRepo

trait DialogRootMigration extends Processor[DialogRootState] {

  import DialogRootEvents._
  import context.dispatcher

  private case class CreateEvents(models: Seq[DialogObsolete])
  private case object EventsPersisted

  val userId: Int
  private var needMigrate = true

  override def afterCommit(e: Event): Unit = {
    super.afterCommit(e)
    e match {
      case Initialized(_) ⇒ needMigrate = false
      case _              ⇒
    }
  }

  override protected def afterSnapshotApply(metadata: SnapshotMetadata, snapshot: Any): Unit = {
    super.afterSnapshotApply(metadata, snapshot)
    needMigrate = false
  }

  override protected def onRecoveryCompleted(): Unit = {
    super.onRecoveryCompleted()
    if (needMigrate)
      migrate()
  }

  private def migrating: Receive = {
    case CreateEvents(models) ⇒
      createEvents(models)
    case EventsPersisted ⇒
      unstashAll()
      context become receiveCommand
    case Status.Failure(e) ⇒
      log.error(e, "Failed to migrate")
      throw e
    case msg ⇒
      log.debug("Stashing while migrating {}", msg.getClass.getName)
      stash()
  }

  private def migrate(): Unit = {
    context.become(migrating)

    (for {
      models ← DbExtension(context.system).db.run(DialogRepo.fetchDialogs(userId))
    } yield CreateEvents(models)) pipeTo self
  }

  private def createEvents(models: Seq[DialogObsolete]): Unit = {
    val created = models map { dialog ⇒
      Created(Instant.ofEpochMilli(dialog.createdAt.getMillis), Some(dialog.peer))
    }

    val archived = models.view.filter(_.archivedAt.isDefined) map { dialog ⇒
      Archived(Instant.ofEpochMilli(dialog.archivedAt.get.getMillis), Some(dialog.peer))
    }

    var ts = Instant.now()
    val favourited = models.view.filter(_.isFavourite) map { dialog ⇒
      ts = ts.plusMillis(1)
      Favourited(ts, Some(dialog.peer))
    }

    val events: List[Event] = Initialized(Instant.now()) +: (created ++ archived ++ favourited).toList
    persistAll(events)(e ⇒ commit(e))
    deferAsync(()) { _ ⇒
      self ! EventsPersisted
    }
  }
}
