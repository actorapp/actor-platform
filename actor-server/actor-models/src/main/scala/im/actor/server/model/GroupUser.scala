package im.actor.server.model

import java.time.{ Instant, LocalDateTime }

case class GroupUser(groupId: Int, userId: Int, inviterUserId: Int, invitedAt: Instant, joinedAt: Option[LocalDateTime], isAdmin: Boolean)
