package im.actor.server.util

import scala.concurrent.forkjoin.ThreadLocalRandom

object IdUtils {
  def nextIntId(rnd: ThreadLocalRandom): Int = rnd.nextInt(1000, Int.MaxValue) + 1
}