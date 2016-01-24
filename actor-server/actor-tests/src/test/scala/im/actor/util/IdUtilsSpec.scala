package im.actor.util

import im.actor.util.misc.IdUtils
import org.scalacheck.Prop._
import org.scalacheck.Properties

object IdUtilsSpec extends Properties("Id Utils") {
  property("In range") = forAll { a: Unit ⇒
    val id = IdUtils.nextIntId()
    id > 1000 && id <= Int.MaxValue
  }
}