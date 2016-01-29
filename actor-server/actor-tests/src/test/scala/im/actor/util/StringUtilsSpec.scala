package im.actor.util

import im.actor.util.misc.StringUtils.{ transliterate, validUsername }
import org.scalatest.{ Matchers, FlatSpecLike }

class StringUtilsSpec extends FlatSpecLike with Matchers {

  "validNickName" should "validate nicknames" in nicknames

  "transliterate" should "transform string to lower-cased string with only latin chars" in translit

  def nicknames() = {
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

  def translit() = {
    transliterate("actor") shouldEqual "actor"

    transliterate("актёр") shouldEqual "akter"
    transliterate("актер") shouldEqual "akter"

    transliterate("俳優") shouldEqual "pai you"
    transliterate("näyttelijä") shouldEqual "nayttelija"
    transliterate("演員") shouldEqual "yan yuan"

    transliterate("الممثل") shouldEqual "almmthl"

    transliterate("actor актёр 俳優 näyttelijä 演員 الممثل") shouldEqual "actor akter pai you nayttelija yan yuan almmthl"
  }

}
