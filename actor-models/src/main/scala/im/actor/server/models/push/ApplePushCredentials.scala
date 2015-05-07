package im.actor.server.models.push

@SerialVersionUID(1L)
case class ApplePushCredentials(authId: Long, apnsKey: Int, token: Array[Byte])
