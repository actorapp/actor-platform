package im.actor.util

import im.actor.util.misc.StringUtils.validUsername
import org.scalatest.{ Matchers, FlatSpecLike }

class StringUtilsSpec extends FlatSpecLike with Matchers {

  "validNickName" should "validate nicknames" in e1

  def e1() = {
    validUsername("rockjam") shouldEqual true
    validUsername("abcde") shouldEqual true
    validUsername("rock_jam") shouldEqual true
    validUsername("r0ck_jaM___") shouldEqual true

    //too long
    val tooLong = 0 to 35 map (e ⇒ ".") mkString ""
    validUsername(tooLong) shouldEqual false
    //too short
    validUsername("roc") shouldEqual false
    //wrong symbols
    validUsername("rock-jam") shouldEqual false
    validUsername("rock&^^jam") shouldEqual false
  }

}
