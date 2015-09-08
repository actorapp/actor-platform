package im.actor.util

import im.actor.util.misc.IdUtils
import org.scalacheck.Prop._
import org.scalacheck.Properties

import scala.concurrent.forkjoin.ThreadLocalRandom

object IdUtilsSpec extends Properties("Id Utils") {
  val rng = ThreadLocalRandom.current()

  property("In range") = forAll { a: Unit ⇒
    val id = IdUtils.nextIntId(rng)
    id > 1000 && id <= Int.MaxValue
  }

}