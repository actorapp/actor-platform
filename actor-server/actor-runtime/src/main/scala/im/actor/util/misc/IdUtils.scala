package im.actor.util.misc

import scala.concurrent.forkjoin.ThreadLocalRandom

object IdUtils {
  def nextIntId(): Int = nextIntId(ThreadLocalRandom.current())

  def nextIntId(rnd: ThreadLocalRandom): Int = rnd.nextInt(1000, Int.MaxValue) + 1

  def nextAuthId(rng: ThreadLocalRandom): Long = {
    val candidate = rng.nextLong()
    if (candidate == 0L) nextAuthId(rng) else candidate
  }
}
