package im.actor.server.office

import java.util.concurrent.TimeUnit

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

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    super.preRestart(reason, message)

    log.error(reason, "Failure while processing message {}", message)
  }

  def stashing: Receive = {
    case msg ⇒ stash()
  }

  def persistStashing[E, R](e: E)(onComplete: E ⇒ Any)(f: E ⇒ Future[R]): Unit = {
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

  def persistStashingReply[E, R](e: E)(onComplete: E ⇒ Any)(f: E ⇒ Future[R]): Unit = {
    val replyTo = sender()

    context become stashing

    persistAsync(e) { evt ⇒
      f(evt) pipeTo replyTo onComplete {
        case Success(_) ⇒
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

  if (passivationIntervalMs > 0) {
    log.warning("Passivating in {} ms", passivationIntervalMs)

    val interval = passivationIntervalMs.milliseconds
    context.system.scheduler.scheduleOnce(interval, context.parent, Passivate(stopMessage = StopOffice))
  }

}
