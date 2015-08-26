package im.actor.server.office

import java.util.concurrent.TimeUnit

import akka.actor.{ ActorLogging, Status }
import akka.contrib.pattern.ShardRegion.Passivate
import akka.pattern.pipe
import akka.persistence.PersistentActor
import org.joda.time.DateTime

import scala.collection.immutable
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{ Failure, Success }

case object StopOffice

trait ProcessorState

trait Processor[State, Event <: AnyRef] extends PersistentActor with ActorLogging {

  case class UnstashAndWork(evt: Event, state: State)

  case class UnstashAndWorkBatch(es: immutable.Seq[Event], state: State)

  case class Work(state: State)

  private val passivationIntervalMs = context.system.settings.config.getDuration("office.passivation-interval", TimeUnit.MILLISECONDS)
  private implicit val ec = context.dispatcher

  protected def updatedState(evt: Event, state: State): State

  protected def workWith(e: Event, s: State): State = {
    val updated = updatedState(e, s)
    self ! Work(updated)
    updated
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    log.error(reason, "Failure while processing message {}", message)

    super.preRestart(reason, message)
  }

  protected def handleInitCommand: Receive

  protected def handleCommand(state: State): Receive

  protected def handleQuery(state: State): Receive

  final def receiveCommand = initializing

  protected final def initializing: Receive = handleInitCommand orElse stashingBehavior

  protected final def working(state: State): Receive = handleCommand(state) orElse handleQuery(state) orElse {
    case Work(newState) => context become working(newState)
    case unmatched ⇒ log.warning("Unmatched message: {}, sender: {}", unmatched, sender())
  }

  private final def stashingBehavior: Receive = {
    case UnstashAndWork(evt, s) =>
      context become working(updatedState(evt, s))
      unstashAll()
    case UnstashAndWorkBatch(es, s) =>
      val newState = es.foldLeft(s) {
        case (acc, e) ⇒
          log.debug("Updating state: {} with event: {}", acc, e)
          updatedState(e, acc)
      }
      context become working(newState)
      unstashAll()
    case msg ⇒
      log.warning("Stashing: {}", msg)
      stash()
  }

  protected final def stashing(state: State): Receive = handleQuery(state) orElse stashingBehavior

  final def persistReply[R](e: Event, state: State)(f: Event ⇒ Future[R]): Unit = {
    log.debug("[persistReply] {}", e)

    persist(e) { evt ⇒
      f(evt) pipeTo sender() onComplete {
        case Success(_) ⇒

        case Failure(f) ⇒
          log.error(f, "Failure while processing event {}", evt)
      }

      workWith(e, state)
    }
  }

  final def persistStashing[R](e: Event, state: State)(f: Event ⇒ Future[R]): Unit = {
    log.debug("[persistStashing], event {}", e)
    context become stashing(state)

    persistAsync(e) { evt ⇒
      f(evt) andThen {
        case Success(_) ⇒
          unstashAndWork(e, state)
        case Failure(f) ⇒
          log.error(f, "Failure while processing event {}", e)
          unstashAndWork(e, state)
      }
    }
  }

  final def persistStashingReply[R](e: Event, state: State)(f: Event ⇒ Future[R]): Unit = {
    val replyTo = sender()

    log.debug("[persistStashingReply], event {}", e)
    context become stashing(state)

    persistAsync(e) { evt ⇒
      f(evt) pipeTo replyTo onComplete {
        case Success(r) ⇒
          unstashAndWork(e, state)
        case Failure(f) ⇒
          log.error(f, "Failure while processing event {}", e)
          replyTo ! Status.Failure(f)

          unstashAndWork(e, state)
      }
    }
  }

  final def persistStashingReply[R](es: immutable.Seq[Event], state: State)(f: immutable.Seq[Event] ⇒ Future[R]): Unit = {
    val replyTo = sender()

    log.debug("[persistStashingReply], events {}", es)
    context become stashing(state)

    persistAsync(es)(_ ⇒ ())

    defer(()) { _ ⇒
      f(es) pipeTo replyTo onComplete {
        case Success(_) ⇒
          unstashAndWorkBatch(es, state)
        case Failure(e) ⇒
          log.error(e, "Failure while processing event {}", e)
          replyTo ! Status.Failure(e)

          unstashAndWorkBatch(es, state)
      }
    }
  }

  final def deferStashingReply[R](e: Event, state: State)(f: Event ⇒ Future[R]): Unit = {
    val replyTo = sender()

    log.debug("[deferStashingReply], event {}", e)
    context become stashing(state)

    f(e) pipeTo replyTo onComplete {
      case Success(result) ⇒
        unstashAndWork(e, state)
      case Failure(f) ⇒
        log.error(f, "Failure while processing event {}", e)
        replyTo ! Status.Failure(f)

        unstashAndWork(e, state)
    }
  }

  private final def unstashAndWork(evt: Event, state: State): Unit = self ! UnstashAndWork(evt, state)

  private final def unstashAndWorkBatch(es: immutable.Seq[Event], state: State): Unit = self ! UnstashAndWorkBatch(es, state)

  def now(): DateTime = new DateTime()

  if (passivationIntervalMs > 0) {
    log.warning("Passivating in {} ms", passivationIntervalMs)

    val interval = passivationIntervalMs.milliseconds
    context.system.scheduler.scheduleOnce(interval, context.parent, Passivate(stopMessage = StopOffice))
  }
}
