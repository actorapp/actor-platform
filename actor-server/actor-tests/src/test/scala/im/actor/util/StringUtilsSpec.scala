package im.actor.util

import im.actor.util.misc.StringUtils.{ transliterate, validGlobalName }
import org.scalatest.{ Matchers, FlatSpecLike }

class StringUtilsSpec extends FlatSpecLike with Matchers {

  "validNickName" should "validate nicknames" in nicknames

  "transliterate" should "transform string to lower-cased string with only latin chars" in translit

  def nicknames() = {
    validGlobalName("rockjam") shouldEqual true
    validGlobalName("abcde") shouldEqual true
    validGlobalName("rock_jam") shouldEqual true
    validGlobalName("r0ck_jaM___") shouldEqual true

    //too long
    val tooLong = 0 to 35 map (e ⇒ ".") mkString ""
    validGlobalName(tooLong) shouldEqual false
    //too short
    validGlobalName("roc") shouldEqual false
    //wrong symbols
    validGlobalName("rock-jam") shouldEqual false
    validGlobalName("rock&^^jam") shouldEqual false
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
