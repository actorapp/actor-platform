package im.actor.server.util

import scala.annotation.tailrec
import scala.util.Try

import com.google.i18n.phonenumbers.PhoneNumberUtil

object PhoneNumber {
  def normalizeStr(number: String, defaultCountry: String = ""): Option[Long] = {
    val phoneUtil = PhoneNumberUtil.getInstance()
    Try(phoneUtil.parse(s"+$number", defaultCountry))
      .orElse(Try(phoneUtil.parse(number, defaultCountry)))
      .toOption map { p ⇒
        val phoneNumber = p.getCountryCode * Math.pow(10L, (sizeOf(p.getNationalNumber) + 1).toDouble).longValue + p.getNationalNumber
        phoneNumber
      }
  }

  def isValid(number: String, defaultCountry: String = ""): Boolean = normalizeStr(number, defaultCountry).isDefined

  def normalizeLong(number: Long, defaultCountry: String = ""): Option[Long] = normalizeStr(s"$number", defaultCountry)

  def normalizeWithCountry(number: Long, defaultCountry: String = ""): Option[(Long, String)] = {
    val phoneUtil = PhoneNumberUtil.getInstance()
    Try(phoneUtil.parse(s"+$number", defaultCountry))
      .orElse(Try(phoneUtil.parse(s"$number", defaultCountry)))
      .toOption map { p ⇒
        val phoneNumber = p.getCountryCode * Math.pow(10L, (sizeOf(p.getNationalNumber) + 1).toDouble).longValue + p.getNationalNumber
        (phoneNumber, phoneUtil.getRegionCodeForCountryCode(p.getCountryCode))
      }
  }

  def tryNormalize(number: Long, defaultCountry: String = "") = normalizeLong(number, defaultCountry).getOrElse(number)

  private def sizeOf(number: Long): Long = {
    @tailrec
    def f(n: Long, res: Long): Long = {
      if (n >= 10) f(n / 10, res + 1)
      else res
    }
    f(number, 0)
  }
}
