package im.actor.server.group

import im.actor.api.rpc.groups.{ Member ⇒ ApiMember }
import im.actor.server.models

trait GroupsImplicits {
  implicit class ExtGroupUser(gu: models.GroupUser) {
    def toMember: ApiMember = ApiMember(gu.userId, gu.inviterUserId, gu.invitedAt.getMillis)
  }
}
