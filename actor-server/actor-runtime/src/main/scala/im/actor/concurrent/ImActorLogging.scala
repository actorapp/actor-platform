package im.actor.concurrent

import akka.actor.DiagnosticActorLogging

trait ImActorLogging extends DiagnosticActorLogging {
  def markFailure[T](f: ⇒ T): T = {
    log.mdc(log.mdc + ("failure" → true))
    f
  }
}