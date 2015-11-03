package im.actor.server.model

case class SessionInfo(authId: Long, sessionId: Long, optUserId: Option[Int])
