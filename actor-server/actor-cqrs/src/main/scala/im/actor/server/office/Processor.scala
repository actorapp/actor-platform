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

trait Processor[State <: ProcessorState, Event <: AnyRef] extends PersistentActor with ActorLogging {
  private val passivationIntervalMs = context.system.settings.config.getDuration("office.passivation-interval", TimeUnit.MILLISECONDS)
  private implicit val ec = context.dispatcher

  protected type ProcessorQuery

  protected def updatedState(evt: Event, state: State): State

  protected def workWith(e: Event, s: State): State = {
    val updated = updatedState(e, s)
    context become working(updated)
    updated
  }

  protected def workWith(es: immutable.Seq[Event], state: State): State = {
    val newState = es.foldLeft(state) {
      case (s, e) ⇒
        log.debug("Updating state: {} with event: {}", s, e)
        updatedState(e, s)
    }
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

  protected final def initializing: Receive = handleInitCommand orElse stashingBehavior()

  protected final def working(state: State): Receive = handleCommand(state) orElse handleQuery(state) orElse {
    case unmatched ⇒ log.warning("Unmatched message: {}", unmatched)
  }

  private final def stashingBehavior(): Receive = {
    case msg ⇒
      log.debug("Stashing while initializing. Message: {}", msg)
      stash()
  }

  private final def stashingBehavior(evt: Any): Receive = {
    case msg ⇒
      log.debug("Stashing while event processing. Message: {}, Event: {}", msg, evt)
      stash()
  }

  protected final def stashing(evt: Any, state: State): Receive =
    handleQuery(state) orElse stashingBehavior(evt)

  final def persistReply[R](e: Event, state: State)(f: Event ⇒ Future[R]): Unit = {
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
    context become stashing(e, state)

    persistAsync(e) { evt ⇒
      f(evt) andThen {
        case Success(_) ⇒
          workWith(e, state)
          unstashAll()
        case Failure(f) ⇒
          log.error(f, "Failure while processing event {}", e)
          workWith(e, state)
          unstashAll()
      }
    }
  }

  final def persistStashingReply[R](e: Event, state: State)(f: Event ⇒ Future[R]): Unit = {
    val replyTo = sender()

    context become stashing(e, state)

    persistAsync(e) { evt ⇒
      f(evt) pipeTo replyTo onComplete {
        case Success(r) ⇒
          workWith(e, state)
          unstashAll()
        case Failure(f) ⇒
          log.error(f, "Failure while processing event {}", e)
          replyTo ! Status.Failure(f)

          workWith(e, state)
          unstashAll()
      }
    }
  }

  final def persistStashingReply[R](es: immutable.Seq[Event], state: State)(f: immutable.Seq[Event] ⇒ Future[R]): Unit = {
    val replyTo = sender()

    context become stashing(es, state)

    persistAsync(es)(_ ⇒ ())

    def updateBatch(es: immutable.Seq[Event], s: State): State =
      es.foldLeft(state) {
        case (s, e) ⇒
          updatedState(e, s)
      }

    defer(()) { _ ⇒
      f(es) pipeTo replyTo onComplete {
        case Success(_) ⇒
          context become working(updateBatch(es, state))
          unstashAll()
        case Failure(e) ⇒
          log.error(e, "Failure while processing event {}", e)
          replyTo ! Status.Failure(e)

          context become working(updateBatch(es, state))
          unstashAll()
      }
    }
  }

  def now(): DateTime = new DateTime()

  if (passivationIntervalMs > 0) {
    log.warning("Passivating in {} ms", passivationIntervalMs)

    val interval = passivationIntervalMs.milliseconds
    context.system.scheduler.scheduleOnce(interval, context.parent, Passivate(stopMessage = StopOffice))
  }
}
