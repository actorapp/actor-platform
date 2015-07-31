package im.actor.server.models

import java.time.LocalDateTime

import org.joda.time.DateTime

case class GroupUser(groupId: Int, userId: Int, inviterUserId: Int, invitedAt: DateTime, joinedAt: Option[LocalDateTime], isAdmin: Boolean)
