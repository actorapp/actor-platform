package im.actor.server.activation.common

sealed trait Code {
  def code: String
}
final case class SmsCode(phone: Long, code: String = "") extends Code
final case class CallCode(phone: Long, language: String, code: String = "") extends Code
final case class EmailCode(email: String, code: String = "") extends Code