package im.actor.server.model.push

trait PushCredentials {
  val authId: Long
}

trait GooglePushCredentials extends PushCredentials {
  val projectId: Long
  val regId: String
}
