package im.actor.server.office

import java.util.concurrent.TimeUnit

import scala.collection.immutable
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{ Failure, Success }

import akka.actor.{ ActorLogging, Status }
import akka.contrib.pattern.ShardRegion.Passivate
import akka.pattern.pipe
import akka.persistence.PersistentActor

case object StopOffice

trait Office extends PersistentActor with ActorLogging {
  private val passivationIntervalMs = context.system.settings.config.getDuration("office.passivation-interval", TimeUnit.MILLISECONDS)
  private implicit val ec = context.dispatcher

  type OfficeState
  type OfficeEvent

  protected def workWith(e: OfficeEvent, s: OfficeState): Unit

  protected def updateState(e: OfficeEvent, s: OfficeState): OfficeState

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    super.preRestart(reason, message)

    log.error(reason, "Failure while processing message {}", message)
  }

  protected def stashing: Receive = {
    case msg ⇒ stash()
  }

  def persistReply[R](e: OfficeEvent)(onComplete: OfficeEvent ⇒ Any)(f: OfficeEvent ⇒ R): Unit = {
    persist(e) { evt ⇒
      sender() ! f(e)
      onComplete(evt)
    }
  }

  def persistStashing[R](e: OfficeEvent)(onComplete: OfficeEvent ⇒ Any)(f: OfficeEvent ⇒ Future[R]): Unit = {
    context become stashing

    persistAsync(e) { evt ⇒
      f(evt) andThen {
        case Success(_) ⇒
          onComplete(evt)
          unstashAll()
        case Failure(e) ⇒
          log.error(e, "Failure while processing event {}", e)
          onComplete(evt)
          unstashAll()
      }
    }
  }

  def persistStashingReply[R](e: OfficeEvent)(onComplete: OfficeEvent ⇒ Any)(f: OfficeEvent ⇒ Future[R]): Unit = {
    val replyTo = sender()

    context become stashing

    persistAsync(e) { evt ⇒
      f(evt) pipeTo replyTo onComplete {
        case Success(r) ⇒
          onComplete(evt)
          unstashAll()
        case Failure(e) ⇒
          log.error(e, "Failure while processing event {}", e)
          replyTo ! Status.Failure(e)

          onComplete(evt)
          unstashAll()
      }
    }
  }

  def persistStashingReply[R](es: immutable.Seq[OfficeEvent])(onComplete: OfficeEvent ⇒ Any)(f: immutable.Seq[OfficeEvent] ⇒ Future[R]): Unit = {
    val replyTo = sender()

    context become stashing

    persistAsync(es)(_ ⇒ ())

    defer(()) { _ ⇒
      f(es) pipeTo replyTo onComplete {
        case Success(_) ⇒
          es foreach onComplete
          unstashAll()
        case Failure(e) ⇒
          log.error(e, "Failure while processing event {}", e)
          replyTo ! Status.Failure(e)

          es foreach onComplete
          unstashAll()
      }
    }
  }

  if (passivationIntervalMs > 0) {
    log.warning("Passivating in {} ms", passivationIntervalMs)

    val interval = passivationIntervalMs.milliseconds
    context.system.scheduler.scheduleOnce(interval, context.parent, Passivate(stopMessage = StopOffice))
  }

}
