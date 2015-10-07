package im.actor.server.activation

object Activation {
  sealed trait Code {
    def code: String
  }
  sealed trait PhoneCode extends Code {
    def phone: Long
  }
  final case class SmsCode(phone: Long, code: String) extends PhoneCode {
    override def equals(that: Any): Boolean =
      that match {
        case that: SmsCode ⇒ this.phone == that.phone
        case _             ⇒ false
      }
    override def hashCode(): Int = phone.hashCode()
  }
  final case class CallCode(phone: Long, code: String, language: String) extends PhoneCode {
    override def equals(that: Any): Boolean =
      that match {
        case that: CallCode ⇒ this.phone == that.phone
        case _              ⇒ false
      }
    override def hashCode(): Int = phone.hashCode()
  }
  final case class EmailCode(email: String, code: String) extends Code {
    override def equals(that: Any): Boolean =
      that match {
        case that: EmailCode ⇒ this.email == that.email
        case _               ⇒ false
      }
    override def hashCode(): Int = email.hashCode()
  }
}