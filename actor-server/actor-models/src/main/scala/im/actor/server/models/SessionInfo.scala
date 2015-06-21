package im.actor.server.models

case class SessionInfo(authId: Long, sessionId: Long, optUserId: Option[Int])
