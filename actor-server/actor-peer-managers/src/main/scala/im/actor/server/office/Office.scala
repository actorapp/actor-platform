package im.actor.server.office

import java.util.concurrent.TimeUnit

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{ Failure, Success }

import akka.actor.{ ActorLogging, ActorRef, Status }
import akka.contrib.pattern.ShardRegion.Passivate
import akka.persistence.PersistentActor

case object StopOffice

trait Office[E] extends PersistentActor with ActorLogging {
  private val passivationIntervalMs = context.system.settings.config.getDuration("office.passivation-interval", TimeUnit.MILLISECONDS)
  private implicit val ec = context.dispatcher

  def stashing: Receive = {
    case msg ⇒ stash()
  }

  def persistStashingReply(evt: E, replyTo: ActorRef)(onComplete: E ⇒ Unit)(f: E ⇒ Future[Any]): Unit = {
    context become stashing

    persistAsync(evt) { _ ⇒
      f(evt) onComplete {
        case Success(res) ⇒
          replyTo ! res
          onComplete(evt)
        case Failure(e) ⇒
          log.error(e, "Failed to process event {}", e)
          replyTo ! Status.Failure(e)
          onComplete(evt)
      }
    }
  }

  if (passivationIntervalMs > 0) {
    log.warning("Passivating in {} ms", passivationIntervalMs)

    val interval = passivationIntervalMs.milliseconds
    context.system.scheduler.scheduleOnce(interval, context.parent, Passivate(stopMessage = StopOffice))
  }
}
