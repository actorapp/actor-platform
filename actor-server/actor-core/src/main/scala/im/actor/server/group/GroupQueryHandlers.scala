package im.actor.server.group

import im.actor.server.ApiConversions._
import im.actor.api.rpc.groups.{ ApiGroup, ApiMember }

private[group] trait GroupQueryHandlers extends GroupCommandHelpers {
  this: GroupProcessor ⇒

  import GroupQueries._

  def getIntegrationToken(group: GroupState, userId: Int): Unit =
    withGroupMember(group, userId) { member ⇒
      val optToken = if (member.isAdmin) group.bot.map(_.token) else None
      sender() ! GetIntegrationTokenResponse(optToken)
    }

  def getIntegrationToken(group: GroupState): Unit = {
    sender() ! GetIntegrationTokenResponse(group.bot.map(_.token))
  }

  def getApiStruct(group: GroupState, clientUserId: Int): Unit = {
    val isMember = hasMember(group, clientUserId)
    val apiMembers =
      if (isMember) {
        group.members.toVector map {
          case (_, m) ⇒
            ApiMember(m.userId, m.inviterUserId, m.invitedAt.toEpochMilli, Some(m.isAdmin))
        }
      } else Vector.empty[ApiMember]

    val struct = ApiGroup(
      group.id,
      accessHash = group.accessHash,
      title = group.title,
      avatar = group.avatar,
      isMember = isMember,
      creatorUserId = group.creatorUserId,
      members = apiMembers,
      createDate = group.createdAt.toEpochMilli,
      isAdmin = Some(isAdmin(group, clientUserId)),
      theme = group.topic,
      about = group.about,
      isHidden = Some(group.isHidden),
      ext = None,
      membersCount = Some(apiMembers.size)
    )

    sender() ! GetApiStructResponse(struct)
  }

  def checkAccessHash(group: GroupState, hash: Long): Unit =
    sender() ! CheckAccessHashResponse(isCorrect = group.accessHash == hash)

  def getMembers(group: GroupState): Unit = {
    val members = group.members.keySet.toSeq
    val invited = group.invitedUserIds.toSeq
    val bot = group.bot.map(_.userId)
    sender() ! GetMembersResponse(members, invited, bot)
  }

  def isPublic(group: GroupState): Unit = {
    sender() ! IsPublicResponse(isPublic = group.typ == GroupType.Public)
  }

  def getAccessHash(group: GroupState): Unit =
    sender() ! GetAccessHashResponse(group.accessHash)

  def isHistoryShared(group: GroupState): Unit =
    sender() ! IsHistorySharedResponse(group.isHistoryShared)

  def getTitle(group: GroupState): Unit =
    sender() ! GetTitleResponse(group.title)
}
