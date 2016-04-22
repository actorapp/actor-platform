package im.actor.server.activation

import im.actor.server.activation.common.{ CallCode, Code, EmailCode, SmsCode }
import im.actor.util.ThreadLocalSecureRandom
import im.actor.util.misc.EmailUtils._
import im.actor.util.misc.PhoneNumberUtils._

import scala.util.Try

trait CodeGen {

  def generateCode(codeTemplate: Code): Code = codeTemplate match {
    case s: SmsCode   ⇒ s.copy(code = genPhoneCode(s.phone))
    case c: CallCode  ⇒ c.copy(code = genPhoneCode(c.phone))
    case e: EmailCode ⇒ e.copy(code = genEmailCode(e.email))
  }

  private def genEmailCode(email: String): String =
    if (isTestEmail(email))
      (email replaceAll (""".*acme""", "")) replaceAll (".com", "")
    else genCode()

  protected def genPhoneCode(phone: Long): String =
    if (isTestPhone(phone)) {
      val strPhone = phone.toString
      Try(strPhone(4).toString * 4) getOrElse strPhone
    } else genCode()

  private def genCode() = ThreadLocalSecureRandom.current.nextLong().toString.dropWhile(c ⇒ c == '0' || c == '-').take(5)

}