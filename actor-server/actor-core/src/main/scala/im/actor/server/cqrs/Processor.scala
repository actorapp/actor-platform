package im.actor.server.cqrs

import java.sql.SQLException

import akka.actor.ActorLogging
import akka.pattern.pipe
import akka.persistence._
import im.actor.concurrent.AlertingActor

import scala.concurrent.Future
import scala.reflect.ClassTag
import scala.util.control.NoStackTrace

trait ProcessorState[S, E] {
  def updated(e: E): S

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

trait IncrementalSnapshots[S <: ProcessorState[S, E], E] extends ProcessorStateControl[S, E] with PersistenceDebug {
  private var _commitsNum = 0

  val SnapshotCommitsThreshold = 100

  override protected def afterCommit(): Unit = {
    super.afterCommit()
    _commitsNum += 1
    if (_commitsNum == SnapshotCommitsThreshold) {
      log.debug("Saving snapshot due to threshold hit")
      saveSnapshot(state.snapshot)
    }
  }
}

trait ProcessorStateControl[S <: ProcessorState[S, E], E] {
  private[this] var _state: S = getInitialState

  protected def getInitialState: S

  protected final def state: S = _state

  protected def setState(state: S) = this._state = state

  protected def commit(e: E): S = {
    beforeCommit()
    setState(state.updated(e))
    afterCommit()
    state
  }

  protected def beforeCommit() = {}

  protected def afterCommit() = {}
}


final class ProcessorStateProbe[S <: ProcessorState[S, E], E](initial: S) extends ProcessorStateControl[S, E] {
  override protected def getInitialState: S = initial
}

abstract class Processor[S <: ProcessorState[S, E], E: ClassTag]
  extends ProcessorStateControl[S, E]
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
    case e: E ⇒
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
