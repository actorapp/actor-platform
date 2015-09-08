package im.actor.server.group

import im.actor.server.ApiConversions._
import im.actor.api.rpc.groups.{ ApiGroup, ApiMember }

private[group] trait GroupQueryHandlers extends GroupCommandHelpers {
  this: GroupProcessor ⇒

  import GroupQueries._

  def getIntegrationToken(group: Group, userId: Int): Unit =
    withGroupMember(group, userId) { member ⇒
      val optToken = if (member.isAdmin) group.bot.map(_.token) else None
      sender() ! GetIntegrationTokenResponse(optToken)
    }

  def getIntegrationToken(group: Group): Unit = {
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
      about = group.about,
      isHidden = Some(group.isHidden),
      extensions = group.extensions.toVector
    )

    sender() ! GetApiStructResponse(struct)
  }

  def checkAccessHash(group: Group, hash: Long): Unit =
    sender() ! CheckAccessHashResponse(isCorrect = group.accessHash == hash)

  def getMembers(group: Group): Unit = {
    val members = group.members.keySet.toSeq
    val invited = group.invitedUserIds.toSeq
    val bot = group.bot.map(_.userId)
    sender() ! GetMembersResponse(members, invited, bot)
  }

  def isPublic(group: Group): Unit = {
    sender() ! IsPublicResponse(isPublic = group.typ == GroupType.Public)
  }

  def getAccessHash(group: Group): Unit =
    sender() ! GetAccessHashResponse(group.accessHash)

  def isHistoryShared(group: Group): Unit =
    sender() ! IsHistorySharedResponse(group.isHistoryShared)
}
