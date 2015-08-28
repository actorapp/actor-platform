package im.actor.server.util

import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.util.Random

object IdUtils {
  def nextIntId(): Int = nextIntId(ThreadLocalRandom.current())

  def nextIntId(rnd: ThreadLocalRandom): Int = rnd.nextInt(1000, Int.MaxValue) + 1

  def nextAuthId(rng: Random): Long = {
    val candidate = rng.nextLong()
    if (candidate == 0L) nextAuthId(rng) else candidate
  }
}