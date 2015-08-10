package im.actor.server.group

import im.actor.api.rpc.groups.{ Member ⇒ ApiMember }

trait GroupsImplicits {
  implicit class ExtMember(m: Member) {
    def asStruct: ApiMember = ApiMember(m.userId, m.inviterUserId, m.invitedAt.getMillis, Some(m.isAdmin))
  }
}
