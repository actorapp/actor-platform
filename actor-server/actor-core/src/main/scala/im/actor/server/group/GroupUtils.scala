package im.actor.server.group

import akka.actor.ActorSystem
import im.actor.api.rpc.groups.ApiGroup
import im.actor.api.rpc.users.ApiUser
import im.actor.server.user.UserUtils

import scala.concurrent.Future

object GroupUtils {

  def getUserIds(group: ApiGroup): Set[Int] =
    group.members.flatMap(m ⇒ Seq(m.userId, m.inviterUserId)).toSet + group.creatorUserId

  private def getUserIds(groups: Seq[ApiGroup]): Set[Int] =
    groups.foldLeft(Set.empty[Int])(_ ++ getUserIds(_))

  def getGroupsUsers(groupIds: Seq[Int], userIds: Seq[Int], clientUserId: Int, clientAuthId: Long)(implicit system: ActorSystem): Future[(Seq[ApiGroup], Seq[ApiUser])] = {
    import system.dispatcher
    for {
      groups ← Future.sequence(groupIds map (GroupExtension(system).getApiStruct(_, clientUserId)))
      memberIds = getUserIds(groups)
      users ← Future.sequence((userIds.toSet ++ memberIds).filterNot(_ == 0) map (UserUtils.safeGetUser(_, clientUserId, clientAuthId))) map (_.flatten)
    } yield (groups, users.toSeq)
  }
}
