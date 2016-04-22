package im.actor.server.dialog

import java.time.Instant

import akka.actor.Status
import akka.pattern.pipe
import im.actor.server.cqrs.{ Event, Processor }
import im.actor.server.db.DbExtension
import im.actor.server.model.{ DialogObsolete, Peer }
import im.actor.server.persist.HistoryMessageRepo
import im.actor.server.persist.dialog.DialogRepo

trait DialogProcessorMigration extends Processor[DialogState] {
  import DialogEvents._
  import context.dispatcher

  private case class PersistEvents(events: List[Event])

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

  override protected def onRecoveryCompleted(): Unit = {
    super.onRecoveryCompleted()
    if (needMigrate)
      migrate()
  }

  private def migrating: Receive = {
    case d: DialogObsolete ⇒
      (for {
        models ← db.run(HistoryMessageRepo.findAfter(userId, peer, d.ownerLastReadAt, Long.MaxValue))
        newMessages = models map { m ⇒
          NewMessage(
            randomId = m.randomId,
            date = Instant.ofEpochMilli(m.date.getMillis),
            senderUserId = m.senderUserId,
            messageHeader = m.messageContentHeader
          )
        }
      } yield PersistEvents(
        Initialized() +:
          newMessages.toList :+
          MessagesRead(Instant.ofEpochMilli(d.lastReadAt.getMillis))
      )) pipeTo self
    case PersistEvents(events) ⇒
      persistAll(events) { _ ⇒
        events foreach (e => commit(e))
        unstashAll()
        context become receiveCommand
      }
    case Status.Failure(e) ⇒
      log.error(e, "Failed to migrate")
      throw e
    case msg ⇒
      log.debug("Stashing while migrating {}", msg.getClass.getName)
      stash()
  }

  private def migrate(): Unit = {
    context become migrating
    (db.run(DialogRepo.findDialog(userId, peer)) map {
      case Some(model) ⇒ model
      case _           ⇒ PersistEvents(List(Initialized()))
    }) pipeTo self
  }
}
