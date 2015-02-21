package im.actor.server.models

@SerialVersionUID(1L)
case class ApplePushCredentials(authId: Long, apnsKey: Int, token: String)
