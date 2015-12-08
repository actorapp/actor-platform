package im.actor.server.sequence

import java.util.concurrent.TimeUnit

import akka.actor._
import im.actor.server.db.DbExtension
import im.actor.server.model.SeqUpdate
import im.actor.server.persist.sequence.UserSequenceRepo

import scala.collection.immutable
import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future, Promise }
import scala.util.{ Failure, Success }

private[sequence] object BatchUpdatesWriter {

  final case class Enqueue(update: SeqUpdate, promise: Promise[Unit])

  private case object Resume

  private case object ScheduledFlush

  def props = Props(classOf[BatchUpdatesWriter])
}

private[sequence] class BatchUpdatesWriter extends Actor with ActorLogging with Stash {

  import BatchUpdatesWriter._

  private val MaxUpdatesBatchSize = context.system.settings.config.getInt("sequence.max-updates-batch-size")
  private val MaxUpdatesBatchInterval = context.system.settings.config.getDuration("sequence.max-updates-batch-interval", TimeUnit.MILLISECONDS).millis

  private val db = DbExtension(context.system).db
  private implicit val ec: ExecutionContext = context.dispatcher

  private[this] var queue = immutable.Queue.empty[SeqUpdate]
  private[this] var senders = immutable.Queue.empty[Promise[Unit]]
  private[this] var scheduledFlush: Option[Cancellable] = None

  def receive: Receive = {
    case Enqueue(update, promise) ⇒ enqueue(update, promise)
    case ScheduledFlush ⇒
      this.scheduledFlush = None
      flush()
  }

  private def stashing: Receive = {
    case Resume ⇒
      unstashAll()
      context become receive
    case msg ⇒ stash()
  }

  private def enqueue(update: SeqUpdate, promise: Promise[Unit]): Unit = {
    this.queue = this.queue.enqueue(update)
    this.senders = this.senders.enqueue(promise)

    if (this.queue.size == MaxUpdatesBatchSize) {
      this.scheduledFlush foreach (_.cancel())
      this.scheduledFlush = None
      flush()
    } else if (this.scheduledFlush.isEmpty) {
      this.scheduledFlush = Some(context.system.scheduler.scheduleOnce(MaxUpdatesBatchInterval, self, ScheduledFlush))
    }
  }

  private def flush(): Unit = {
    context become stashing

    val updatesQueue = this.queue
    val replyQueue = this.senders

    this.queue = immutable.Queue.empty
    this.senders = immutable.Queue.empty

    batchWrite(updatesQueue) onComplete {
      case Success(_) ⇒
        replyQueue foreach (_.success(()))

        self ! Resume
      case Failure(e) ⇒
        replyQueue foreach (_.failure(e))
        log.error(e, "Failed to batch write updates")

        self ! Resume
    }
  }

  private def batchWrite(updates: Seq[SeqUpdate]): Future[Unit] =
    db.run(UserSequenceRepo.create(updates)) map (_ ⇒ ())

  override def postRestart(reason: Throwable): Unit = {
    log.error(reason, "Failed")
    super.postRestart(reason)
  }
}
