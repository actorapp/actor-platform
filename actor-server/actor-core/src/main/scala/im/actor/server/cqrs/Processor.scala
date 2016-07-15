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
  val logReceive: Receive = new PartialFunction[Any, Unit] {
    def isDefinedAt(x: Any): Boolean = {
      log.debug(s"Got message of class ${x.getClass.getName}: $x")
      false
    }
    def apply(v1: Any): Unit = throw new RuntimeException("Should not be here!")
  }

  override protected def onPersistFailure(cause: Throwable, event: Any, seqNr: Long): Unit = {
    super.onPersistFailure(cause, event, seqNr)

    cause match {
      case e: SQLException ⇒ log.error(e.getNextException, "Next exception:")
      case _               ⇒
    }
  }
}

trait IncrementalSnapshots[S <: ProcessorState[S]] extends ProcessorStateControl[S] with PersistenceDebug {
  val SnapshotCommitsThreshold = 100

  private var _commitsNum = 0
  private var _savingSequenceNr = 0L

  override protected def afterCommit(e: Event): Unit = {
    super.afterCommit(e)
    if (recoveryFinished) {
      _commitsNum += 1
      if (_commitsNum >= SnapshotCommitsThreshold && _savingSequenceNr != snapshotSequenceNr) {
        log.debug("Saving snapshot due to threshold hit")
        _commitsNum = 0
        _savingSequenceNr = snapshotSequenceNr
        saveSnapshot(state.snapshot)
      }
    }
  }
}

trait ProcessorStateControl[S <: ProcessorState[S]] {
  private[this] var _state: S = getInitialState

  protected def getInitialState: S

  //TODO: rename to processorState
  final def state: S = _state

  def setState(state: S) = this._state = state

  def commit(e: Event): S = {
    beforeCommit(e)
    setState(state.updated(e))
    afterCommit(e)
    state
  }

  def applySnapshot(metadata: SnapshotMetadata, snapshot: Any): Unit = {
    beforeSnapshotApply(metadata, snapshot)
    setState(state.withSnapshot(metadata, snapshot))
    afterSnapshotApply(metadata, snapshot)
  }

  protected def beforeCommit(e: Event) = {}

  protected def afterCommit(e: Event) = {}

  protected def beforeSnapshotApply(metadata: SnapshotMetadata, snapshot: Any): Unit = {}

  protected def afterSnapshotApply(metadata: SnapshotMetadata, snapshot: Any): Unit = {}
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
      commit(e)
    case SnapshotOffer(metadata, snapshot) ⇒
      applySnapshot(metadata, snapshot)
    case RecoveryCompleted ⇒ onRecoveryCompleted()
    case SaveSnapshotFailure(metadata, cause) ⇒
      log.error(cause, "Failed to save snapshot, metadata: {}", metadata)
    case SaveSnapshotSuccess(metadata) ⇒
      log.error("Got save SaveSnapshotSuccess during recovery! Should never happen. Metadata: {}", metadata)
  }

  protected def handleCommand: Receive

  protected def handleQuery: PartialFunction[Any, Future[Any]]

  protected def onRecoveryCompleted() = {}

  protected def reply(msg: AnyRef): Unit = sender() ! msg

  protected def replyFuture(msgFuture: Future[Any]): Unit = msgFuture pipeTo sender()

  protected def saveSnapshotIfNeeded(): Unit = {}
}
