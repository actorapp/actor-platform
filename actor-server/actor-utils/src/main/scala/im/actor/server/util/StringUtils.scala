package im.actor.server.util

import java.nio.charset.Charset

object StringUtils {

  private val encoder = Charset.forName("US-ASCII").newEncoder()

  def utfToHexString(s: String): String = { s.map(ch ⇒ f"${ch.toInt}%04X").mkString }

  def isAsciiString(c: CharSequence): Boolean = encoder.canEncode(c)

}
