package im.actor.server.group

import akka.actor.Status
import im.actor.server.group.GroupErrors.{ NotAMember, NotAdmin }

private[group] trait GroupCommandHelpers {
  this: GroupProcessor ⇒

  protected def withGroupAdmin(group: GroupState, userId: Int)(f: ⇒ Unit): Unit =
    group.members.get(userId) match {
      case Some(member) if member.isAdmin ⇒ f
      case _                              ⇒ sender() ! Status.Failure(NotAdmin)
    }

  protected def withGroupMember(group: GroupState, userId: Int)(f: Member ⇒ Unit): Unit =
    group.members.get(userId) match {
      case Some(member) ⇒ f(member)
      case None         ⇒ sender() ! Status.Failure(NotAMember)
    }

}
