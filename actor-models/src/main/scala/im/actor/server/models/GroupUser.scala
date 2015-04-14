package im.actor.server.models

import org.joda.time.DateTime

case class GroupUser(groupId: Int, userId: Int, inviterUserId: Int, invitedAt: DateTime)
