package im.actor.server.cqrs

import java.sql.SQLException

import akka.actor.ActorLogging
import akka.pattern.pipe
import akka.persistence._
import im.actor.concurrent.AlertingActor

import scala.concurrent.Future
import scala.reflect.ClassTag
import scala.util.control.NoStackTrace

trait ProcessorState[S] {
  def updated(e: Event): S

  def withSnapshot(metadata: SnapshotMetadata, snapshot: Any): S

  def snapshot: Any = this
}

abstract class ProcessorError(msg: String) extends RuntimeException(msg) with NoStackTrace

trait PersistenceDebug extends PersistentActor with ActorLogging with AlertingActor {
  override protected def onPersistFailure(cause: Throwable, event: Any, seqNr: Long): Unit = {
    super.onPersistFailure(cause, event, seqNr)

    cause match {
      case e: SQLException ⇒ log.error(e.getNextException, "Next exception:")
    }
  }
}

trait IncrementalSnapshots[S <: ProcessorState[S]] extends ProcessorStateControl[S] with PersistenceDebug {
  private var _commitsNum = 0

  val SnapshotCommitsThreshold = 100

  override protected def afterCommit(e: Event): Unit = {
    super.afterCommit(e)
    _commitsNum += 1
    if (_commitsNum == SnapshotCommitsThreshold) {
      log.debug("Saving snapshot due to threshold hit")
      _commitsNum = 0
      saveSnapshot(state.snapshot)
    }
  }
}

trait ProcessorStateControl[S <: ProcessorState[S]] {
  private[this] var _state: S = getInitialState

  protected def getInitialState: S

  final def state: S = _state

  def setState(state: S) = this._state = state

  def commit(e: Event): S = {
    beforeCommit(e)
    setState(state.updated(e))
    afterCommit(e)
    state
  }

  protected def beforeCommit(e: Event) = {}

  protected def afterCommit(e: Event) = {}
}

object ProcessorStateProbe {
  def apply[S <: ProcessorState[S]](initial: S) = new ProcessorStateProbe[S](initial)
}

final class ProcessorStateProbe[S <: ProcessorState[S]](initial: S) extends ProcessorStateControl[S] {
  override protected def getInitialState: S = initial
}

abstract class Processor[S <: ProcessorState[S]]
  extends ProcessorStateControl[S]
  with PersistenceDebug {

  import context.dispatcher

  type CommandHandler = PartialFunction[Any, Unit]
  type QueryHandler = PartialFunction[Any, Future[Any]]

  override def receiveCommand = handleCommand orElse (handleQuery andThen (_ pipeTo sender()))

  override def unhandled(message: Any): Unit = {
    log.warning(s"Unhandled message of class ${message.getClass.getName}: $message")
    super.unhandled(message)
  }

  override final def receiveRecover = {
    case e: Event ⇒
      setState(state.updated(e))
    case SnapshotOffer(metadata, snapshot) ⇒
      setState(state.withSnapshot(metadata, snapshot))
    case RecoveryCompleted ⇒ onRecoveryCompleted()
    case SaveSnapshotFailure(metadata, cause) ⇒
      log.error(cause, "Failed to save snapshot, metadata: {}", metadata)
  }

  protected def handleCommand: Receive

  protected def handleQuery: PartialFunction[Any, Future[Any]]

  protected def onRecoveryCompleted() = {}

  protected def reply(msg: AnyRef): Unit = sender() ! msg

  protected def replyFuture(msgFuture: Future[Any]): Unit = msgFuture pipeTo sender()

  protected def saveSnapshotIfNeeded(): Unit = {}
}
