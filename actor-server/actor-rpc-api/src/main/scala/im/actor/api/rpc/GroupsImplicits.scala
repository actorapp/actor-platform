package im.actor.api.rpc

import im.actor.api.rpc.groups.Member
import im.actor.server.models

trait GroupsImplicits {
  implicit class ExtGroupUser(gu: models.GroupUser) {
    def toMember: Member = Member(gu.userId, gu.inviterUserId, gu.invitedAt.getMillis)
  }
}
