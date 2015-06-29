package im.actor.server.models

@SerialVersionUID(1L)
case class AuthSmsCodeObsolete(phoneNumber: Long, smsHash: String, smsCode: String)