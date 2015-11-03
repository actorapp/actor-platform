package im.actor.server.model.push

sealed trait PushCredentials

@SerialVersionUID(1L)
case class GooglePushCredentials(authId: Long, projectId: Long, regId: String) extends PushCredentials

@SerialVersionUID(1L)
case class ApplePushCredentials(authId: Long, apnsKey: Int, token: Array[Byte]) extends PushCredentials
