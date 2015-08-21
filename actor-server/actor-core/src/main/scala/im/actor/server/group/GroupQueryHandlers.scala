package im.actor.server.group

import akka.actor.Status
import im.actor.server.api.ApiConversions._
import im.actor.api.rpc.groups.{ Group ⇒ ApiGroup, Member ⇒ ApiMember }
import im.actor.server.group.GroupErrors.NoBotFound

private[group] trait GroupQueryHandlers extends GroupCommandHelpers {
  this: GroupProcessor ⇒

  import GroupQueries._

  def getIntegrationToken(group: Group, userId: Int): Unit =
    withGroupMember(group, userId) { _ ⇒
      sender() ! GetIntegrationTokenResponse(group.bot.map(_.token))
    }

  def getApiStruct(group: Group, clientUserId: Int): Unit = {
    val apiMembers = group.members.toVector map {
      case (_, m) ⇒
        ApiMember(m.userId, m.inviterUserId, m.invitedAt.getMillis, Some(m.isAdmin))
    }

    val struct = ApiGroup(
      group.id,
      accessHash = group.accessHash,
      title = group.title,
      avatar = group.avatar,
      isMember = hasMember(group, clientUserId),
      creatorUserId = group.creatorUserId,
      members = apiMembers,
      createDate = group.createdAt.getMillis,
      disableEdit = None,
      disableInviteView = None,
      disableInviteRevoke = None,
      disableIntegrationView = None,
      disableIntegrationsRevoke = None,
      isAdmin = Some(isAdmin(group, clientUserId)),
      theme = group.topic,
      about = group.about
    )

    sender() ! GetApiStructResponse(struct)
  }

  def checkAccessHash(group: Group, hash: Long): Unit =
    sender() ! CheckAccessHashResponse(isCorrect = group.accessHash == hash)

  def getMembers(group: Group): Unit = {
    group.bot map { bot ⇒
      val members = group.members.keySet.toSeq
      val invited = group.invitedUserIds.toSeq
      sender() ! GetMembersResponse(members, invited, bot.userId)
    } getOrElse {
      sender() ! Status.Failure(NoBotFound)
    }
  }

  def isPublic(group: Group): Unit = {
    sender() ! IsPublicResponse(isPublic = group.isPublic)
  }

}
