package im.actor.server.model.presences

import org.joda.time.DateTime

@SerialVersionUID(1L)
case class UserPresence(userId: Int, authId: Long, lastSeenAt: Option[DateTime])
