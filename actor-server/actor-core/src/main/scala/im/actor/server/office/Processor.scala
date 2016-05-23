package im.actor.server.office

import java.time.Instant
import java.util.concurrent.TimeUnit

import akka.actor.{ ActorRef, Cancellable, Status }
import akka.cluster.sharding.ShardRegion.Passivate
import akka.pattern.pipe
import akka.persistence.PersistentActor
import im.actor.concurrent.{ ActorFutures, AlertingActor }

import scala.collection.immutable
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.control.NoStackTrace
import scala.util.{ Failure, Success }

abstract class EntityNotFound(msg: String = "") extends RuntimeException(msg) with NoStackTrace
case object EntityNotFoundError extends EntityNotFound("Entity not found")
case object StopOffice

trait ProcessorState

trait Processor[State, Event <: AnyRef] extends PersistentActor with ActorFutures with AlertingActor {

  case class BreakStashing(ts: Instant, evts: Seq[Event], state: State)

  case class UnstashAndWork(evt: Event, state: State)

  case class UnstashAndWorkBatch(es: immutable.Seq[Event], state: State)

  private val passivationIntervalMs = context.system.settings.config.getDuration("office.passivation-interval", TimeUnit.MILLISECONDS)
  private implicit val ec = context.dispatcher

  protected def updatedState(evt: Event, state: State): State

  protected val notFoundError: EntityNotFound = EntityNotFoundError

  protected def workWith(es: immutable.Seq[Event], state: State): State = {
    val newState = es.foldLeft(state) {
      case (s, e) ⇒
        log.debug("Updating state: {} with event: {}", s, e)
        updatedState(e, s)
    }
    context become working(newState)
    newState
  }

  protected def workWith(e: Event, s: State): State = {
    val newState = updatedState(e, s)
    context become working(newState)
    newState
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    log.error(reason, "Failure while processing message {}", message)

    super.preRestart(reason, message)
  }

  protected def handleInitCommand: Receive

  protected def handleCommand(state: State): Receive

  protected def handleQuery(state: State): Receive

  final def receiveCommand = initializing

  protected final def initializing: Receive =
    handleInitCommand orElse unstashing orElse {
      case msg ⇒
        log.debug("Entity not found while processing {}", msg)
        sender() ! Status.Failure(notFoundError)
    }

  protected final def working(state: State): Receive = handleCommand(state) orElse handleQuery(state) orElse {
    case unmatched ⇒ log.warning("Unmatched message: {}, {}, sender: {}", unmatched.getClass.getName, unmatched, sender())
  }

  protected final def stashingBehavior: Receive =
    unstashing orElse {
      case msg ⇒
        log.warning("Stashing: {}", msg)
        stash()
    }

  private final def unstashing: Receive = {
    case BreakStashing(ts, evts, state) ⇒
      val strEvents = evts map { e ⇒
        s"{ type: ${e.getClass.getName}, event: $e }"
      }
      log.error("Break stashing. Was in stashing since: {}, with events: {}", ts, strEvents mkString "; ")
      context become working(state)
      unstashAll()
    case UnstashAndWork(evt, s) ⇒
      context become working(updatedState(evt, s))
      unstashAll()
    case UnstashAndWorkBatch(es, s) ⇒
      val newState = es.foldLeft(s) {
        case (acc, e) ⇒
          log.debug("Updating state: {} with event: {}", acc, e)
          updatedState(e, acc)
      }
      context become working(newState)
      unstashAll()
  }

  protected final def stashing(state: State): Receive = handleQuery(state) orElse stashingBehavior

  final def persistReply[R](e: Event, state: State)(f: Event ⇒ Future[R]): Unit =
    persistReply(e, state, sender())(f)

  final def persistReply[R](e: Event, state: State, replyTo: ActorRef)(f: Event ⇒ Future[R]): Unit = {
    log.debug("[persistReply] {}", e)

    persist(e) { evt ⇒
      val newState = updatedState(e, state)

      f(evt) pipeTo replyTo onComplete {
        case Success(_) ⇒

        case Failure(f) ⇒
          log.error(f, "Failure while processing event {}", evt)
      }

      context become working(newState)
    }
  }

  final def persistStashingReply[R](e: Event, state: State)(f: Event ⇒ Future[R]): Unit =
    persistStashingReply(e, state, sender())(f)

  final def persistStashingReply[R](e: Event, state: State, replyTo: ActorRef)(f: Event ⇒ Future[R]): Unit = {
    log.debug("[persistStashingReply], event {}", e)
    val breakStashing = breakStashingTimeout(e, state)
    context become stashing(state)

    persistAsync(e) { evt ⇒
      f(evt) pipeTo replyTo onComplete {
        case Success(r) ⇒
          breakStashing.cancel()
          unstashAndWork(e, state)
        case Failure(exc) ⇒
          log.error(exc, "Failure while processing event {}", e)
          breakStashing.cancel()
          unstashAndWork(e, state)
      }
    }
  }

  final def persistStashingReply[R](es: immutable.Seq[Event], state: State)(f: immutable.Seq[Event] ⇒ Future[R]): Unit = {
    val replyTo = sender()

    log.debug("[persistStashingReply], events {}", es)
    val breakStashing = breakStashingTimeout(es, state)
    context become stashing(state)

    persistAllAsync(es)(_ ⇒ ())

    deferAsync(()) { _ ⇒
      f(es) pipeTo replyTo onComplete {
        case Success(_) ⇒
          breakStashing.cancel()
          unstashAndWorkBatch(es, state)
        case Failure(exc) ⇒
          log.error(exc, "Failure while processing events {}", es)
          breakStashing.cancel()
          unstashAndWorkBatch(es, state)
      }
    }
  }

  final def deferStashingReply[R](e: Event, state: State)(f: Event ⇒ Future[R]): Unit = {
    val replyTo = sender()

    log.debug("[deferStashingReply], event {}", e)
    val breakStashing = breakStashingTimeout(e, state)
    context become stashing(state)

    f(e) pipeTo replyTo onComplete {
      case Success(result) ⇒
        breakStashing.cancel()
        unstashAndWork(e, state)
      case Failure(f) ⇒
        log.error(f, "Failure while processing event {}", e)
        breakStashing.cancel()
        unstashAndWork(e, state)
    }
  }

  private def breakStashingTimeout(e: Event, s: State): Cancellable = breakStashingTimeout(Seq(e), s)

  private def breakStashingTimeout(es: Seq[Event], s: State): Cancellable =
    context.system.scheduler.scheduleOnce(30.seconds, self, BreakStashing(now(), es, s))

  protected final def unstashAndWork(evt: Event, state: State): Unit = self ! UnstashAndWork(evt, state)

  private final def unstashAndWorkBatch(es: immutable.Seq[Event], state: State): Unit = self ! UnstashAndWorkBatch(es, state)

  def now(): Instant = Instant.now

  if (passivationIntervalMs > 0) {
    log.warning("Passivating in {} ms", passivationIntervalMs)

    val interval = passivationIntervalMs.milliseconds
    context.system.scheduler.scheduleOnce(interval, context.parent, Passivate(stopMessage = StopOffice))
  }
}
