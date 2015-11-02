package im.actor.server.cqrs

import java.time.Instant

import akka.actor.ActorLogging
import akka.pattern.pipe
import akka.persistence.{ PersistentActor, RecoveryCompleted }
import im.actor.server.event.TSEvent
import org.joda.time.DateTime

import scala.concurrent.Future
import scala.util.control.NoStackTrace

trait ProcessorState[S] {
  def updated(e: AnyRef, ts: Instant): S

  final def updated(e: TSEvent): S = updated(e.payload, Instant.ofEpochMilli(e.ts.getMillis))
}

abstract class ProcessorError(msg: String) extends RuntimeException(msg) with NoStackTrace

trait Processor[S <: ProcessorState[S]] extends PersistentActor with ActorLogging {

  import context.dispatcher

  type CommandHandler = PartialFunction[Any, Unit]
  type QueryHandler = PartialFunction[Any, Future[Any]]

  private[this] var _state: S = getInitialState

  protected def getInitialState: S

  protected final def state: S = _state

  protected def persistTS[E <: AnyRef](e: E)(handler: (E, Instant) ⇒ Unit): Unit = {
    val instant = Instant.now()
    val dt = new DateTime(instant.toEpochMilli)
    val tsEv = TSEvent(dt, e)

    persist(tsEv) { _ ⇒
      handler(e, instant)
    }
  }

  override def receiveCommand = handleCommand orElse (handleQuery andThen (_ pipeTo sender()))

  override def unhandled(message: Any): Unit = {
    log.warning(s"Unhandled message: $message")
    super.unhandled(message)
  }

  override final def receiveRecover = {
    case e: TSEvent ⇒
      _state = _state.updated(e)
    case RecoveryCompleted ⇒ onRecoveryCompleted()
  }

  protected def handleCommand: Receive

  protected def handleQuery: PartialFunction[Any, Future[Any]]

  protected def onRecoveryCompleted() = {}

  protected def commit(e: AnyRef, ts: Instant): S = {
    _state = _state.updated(e, ts)
    state
  }

  protected def commit(e: TSEvent): Unit = commit(e.payload, Instant.ofEpochMilli(e.ts.getMillis))

  protected def reply(msg: AnyRef): Unit = sender() ! msg

  protected def replyFuture(msgFuture: Future[Any]): Unit = msgFuture pipeTo sender()
}
