package im.actor.server.cqrs

import java.sql.SQLException

import akka.actor.ActorLogging
import akka.pattern.pipe
import akka.persistence.{ PersistentActor, RecoveryCompleted, SnapshotMetadata, SnapshotOffer }
import im.actor.concurrent.AlertingActor

import scala.concurrent.Future
import scala.reflect.ClassTag
import scala.util.control.NoStackTrace

trait ProcessorState[S, E] {
  def updated(e: E): S

  def withSnapshot(metadata: SnapshotMetadata, snapshot: Any): S
}

abstract class ProcessorError(msg: String) extends RuntimeException(msg) with NoStackTrace

trait PersistenceDebug extends PersistentActor with ActorLogging {
  override protected def onPersistFailure(cause: Throwable, event: Any, seqNr: Long): Unit = {
    super.onPersistFailure(cause, event, seqNr)

    cause match {
      case e: SQLException ⇒ log.error(e.getNextException, "Next exception:")
    }
  }
}

abstract class Processor[S <: ProcessorState[S, E], E: ClassTag] extends PersistentActor with PersistenceDebug with AlertingActor {

  import context.dispatcher

  type CommandHandler = PartialFunction[Any, Unit]
  type QueryHandler = PartialFunction[Any, Future[Any]]

  private[this] var _state: S = getInitialState

  protected def getInitialState: S

  protected final def state: S = _state

  override def receiveCommand = handleCommand orElse (handleQuery andThen (_ pipeTo sender()))

  override def unhandled(message: Any): Unit = {
    log.warning(s"Unhandled message of class ${message.getClass.getName}: $message")
    super.unhandled(message)
  }

  override final def receiveRecover = {
    case e: E ⇒
      _state = _state.updated(e)
    case SnapshotOffer(metadata, snapshot) ⇒
      _state = _state.withSnapshot(metadata, snapshot)
    case RecoveryCompleted ⇒ onRecoveryCompleted()
  }

  protected def handleCommand: Receive

  protected def handleQuery: PartialFunction[Any, Future[Any]]

  protected def onRecoveryCompleted() = {}

  protected def commit(e: E): S = {
    _state = _state.updated(e)
    state
  }

  protected def reply(msg: AnyRef): Unit = sender() ! msg

  protected def replyFuture(msgFuture: Future[Any]): Unit = msgFuture pipeTo sender()

  protected def setState(state: S) = this._state = state
}
