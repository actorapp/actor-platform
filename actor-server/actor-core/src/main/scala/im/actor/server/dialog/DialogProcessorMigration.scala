package im.actor.server.dialog

import java.time.Instant

import akka.actor.Status
import akka.pattern.pipe
import akka.persistence.SnapshotMetadata
import im.actor.server.cqrs.{ Event, Processor }
import im.actor.server.db.DbExtension
import im.actor.server.model.{ DialogObsolete, Peer }
import im.actor.server.persist.HistoryMessageRepo
import im.actor.server.persist.dialog.DialogRepo

trait DialogProcessorMigration extends Processor[DialogState] {
  import DialogEvents._
  import context.{ dispatcher, system }

  private case class PersistEvents(events: List[Event])
  private case object EventsPersisted

  private var needMigrate = true

  val userId: Int
  val peer: Peer
  private val db = DbExtension(context.system).db

  override def afterCommit(e: Event) = {
    super.afterCommit(e)
    e match {
      case Initialized() ⇒ needMigrate = false
      case _             ⇒
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
    case d: DialogObsolete ⇒
      log.warning("Finding messages")
      (for {
        historyOwner ← HistoryUtils.getHistoryOwner(peer, userId)
        unreadCount ← db.run(HistoryMessageRepo.getUnreadCount(historyOwner, userId, peer, d.ownerLastReadAt))
        _ = log.warning("Found {} messages", unreadCount)
      } yield PersistEvents(
        List(
          Initialized(),
          SetCounter(unreadCount),
          MessagesRead(Instant.ofEpochMilli(d.ownerLastReadAt.getMillis), readerUserId = userId),
          MessagesRead(Instant.ofEpochMilli(d.lastReadAt.getMillis))
        )
      )) pipeTo self
    case PersistEvents(events) ⇒
      log.warning("Persisting events")
      persistAll(events)(e ⇒ commit(e))

      deferAsync(()) { _ ⇒
        log.warning("Migration completed")
        self ! EventsPersisted
      }
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
    log.warning("Starting migration")
    context become migrating
    (db.run(DialogRepo.findDialog(userId, peer)) map {
      case Some(model) ⇒ model
      case _           ⇒ PersistEvents(List(Initialized()))
    }) pipeTo self
  }
}
