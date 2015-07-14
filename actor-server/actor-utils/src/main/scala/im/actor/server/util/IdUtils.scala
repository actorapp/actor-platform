package im.actor.server.util

import scala.concurrent.forkjoin.ThreadLocalRandom

object IdUtils {
  def nextIntId(rnd: ThreadLocalRandom): Int = {
    val possible = rnd.nextInt(Int.MaxValue) + 1
    if (possible == 0) {
      nextIntId(rnd)
    } else possible
  }
}