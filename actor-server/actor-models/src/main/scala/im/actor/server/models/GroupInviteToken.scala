package im.actor.server.models

import org.joda.time.DateTime

case class GroupInviteToken(groupId: Int, creatorId: Int, token: String, revokedAt: Option[DateTime] = None)