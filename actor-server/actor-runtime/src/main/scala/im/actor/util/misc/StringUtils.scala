package im.actor.util.misc

import java.nio.charset.Charset
import java.util.regex.Pattern

import scalaz._, Scalaz._

object StringUtils {

  private val encoder = Charset.forName("US-ASCII").newEncoder()

  private val usernamePattern = Pattern.compile("""^[0-9a-zA-Z_]{5,32}""", Pattern.UNICODE_CHARACTER_CLASS)

  def utfToHexString(s: String): String = { s.map(ch ⇒ f"${ch.toInt}%04X").mkString }

  def isAsciiString(c: CharSequence): Boolean = encoder.canEncode(c)

  def nonEmptyString(s: String): \/[NonEmptyList[String], String] = {
    val trimmed = s.trim
    if (trimmed.isEmpty) "Should be nonempty".wrapNel.left else trimmed.right
  }

  def printableString(s: String): \/[NonEmptyList[String], String] = {
    val p = Pattern.compile("\\p{Print}+", Pattern.UNICODE_CHARACTER_CLASS)
    if (p.matcher(s).matches) s.right else "Should contain printable characters only".wrapNel.left
  }

  def validName(n: String): \/[NonEmptyList[String], String] =
    nonEmptyString(n).flatMap(printableString)

  def validUsername(username: String): Boolean = usernamePattern.matcher(username.trim).matches

  def normalizeUsername(username: String): Option[String] = {
    val trimmed = username.trim
    if (usernamePattern.matcher(trimmed).matches())
      Some(trimmed)
    else None
  }
}
