package im.actor.server.activation

object Activation {
  sealed trait Code {
    def code: String
  }
  final case class SmsCode(phone: Long, code: String) extends Code
  final case class EmailCode(email: String, code: String) extends Code
}