package im.actor.server.api.frontend

import java.util.concurrent.atomic.AtomicLong

trait Frontend {
  private val connCounter = new AtomicLong(0L)

  protected val connIdPrefix: String

  protected def nextConnId(): String = s"conn-${connIdPrefix}-${connCounter.incrementAndGet()}"
}