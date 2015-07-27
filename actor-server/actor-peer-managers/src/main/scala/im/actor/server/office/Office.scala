package im.actor.server.office

import java.util.concurrent.TimeUnit

import scala.concurrent.duration._
import scala.language.postfixOps

import akka.actor.{ PoisonPill, ActorLogging }
import akka.contrib.pattern.ShardRegion.Passivate
import akka.persistence.PersistentActor

case object StopOffice

trait Office extends PersistentActor with ActorLogging {
  private val passivationIntervalMs = context.system.settings.config.getDuration("office.passivation-interval", TimeUnit.MILLISECONDS)
  private implicit val ec = context.dispatcher

  if (passivationIntervalMs > 0) {
    log.warning("Passivating in {} ms", passivationIntervalMs)

    val interval = passivationIntervalMs.milliseconds
    context.system.scheduler.scheduleOnce(interval, context.parent, Passivate(stopMessage = StopOffice))
  }
}
