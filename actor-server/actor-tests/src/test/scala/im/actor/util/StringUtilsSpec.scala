package im.actor.util

import im.actor.util.misc.StringUtils.validNickName
import org.scalatest.{ Matchers, FlatSpecLike }

class StringUtilsSpec extends FlatSpecLike with Matchers {

  "validNickName" should "validate nicknames" in e1

  def e1() = {
    validNickName("rockjam") shouldEqual true
    validNickName("abcde") shouldEqual true
    validNickName("rock_jam") shouldEqual true
    validNickName("r0ck_jaM___") shouldEqual true

    //too long
    val tooLong = 0 to 35 map (e ⇒ ".") mkString ""
    validNickName(tooLong) shouldEqual false
    //too short
    validNickName("roc") shouldEqual false
    //wrong symbols
    validNickName("rock-jam") shouldEqual false
    validNickName("rock&^^jam") shouldEqual false
  }

}
