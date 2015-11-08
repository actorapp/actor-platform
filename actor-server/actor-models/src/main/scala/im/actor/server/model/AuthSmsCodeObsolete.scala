package im.actor.server.model

@SerialVersionUID(1L)
case class AuthSmsCodeObsolete(id: Long, phoneNumber: Long, smsHash: String, smsCode: String, isDeleted: Boolean = false)