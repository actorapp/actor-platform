package im.actor.server.group

import akka.pattern.pipe
import akka.stream.scaladsl.Source
import com.google.protobuf.ByteString
import com.google.protobuf.wrappers.Int32Value
import im.actor.server.ApiConversions._
import im.actor.api.rpc.groups.{ ApiGroup, ApiGroupFull, ApiGroupType, ApiMember }
import im.actor.types.UserId

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
    val apiMembers = getApiMembers(group, clientUserId)

    val struct = ApiGroup(
      group.id,
      accessHash = group.accessHash,
      title = group.title,
      avatar = group.avatar,
      isMember = Some(isMember),
      creatorUserId = group.creatorUserId,
      members = apiMembers,
      createDate = group.createdAt.toEpochMilli,
      isAdmin = Some(isAdmin(group, clientUserId)),
      theme = group.topic,
      about = group.about,
      isHidden = Some(group.isHidden),
      ext = None,
      membersCount = Some(apiMembers.size),
      groupType = Some(group.typ match {
        case GroupType.Channel ⇒ ApiGroupType.CHANNEL
        case GroupType.General | GroupType.Public | GroupType.Unrecognized(_) ⇒ ApiGroupType.GROUP
      }),
      canSendMessage = None
    )

    sender() ! GetApiStructResponse(struct)
  }

  def getApiFullStruct(group: GroupState, clientUserId: Int): Unit = {
    val apiMembers = getApiMembers(group, clientUserId)

    val struct = ApiGroupFull(
      group.id,
      theme = Some(group.title),
      about = group.about,
      ownerUserId = group.creatorUserId,
      createDate = group.createdAt.toEpochMilli,
      ext = None,
      canViewMembers = Some(canViewMembers(group, clientUserId)),
      canInvitePeople = Some(canInvitePeople(group, clientUserId)),
      isSharedHistory = Some(group.isHistoryShared),
      isAsyncMembers = Some(group.members.size > 100),
      members = apiMembers
    )

    sender() ! GetApiFullStructResponse(struct)
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

  def loadMembers(group: GroupState, clientUserId: Int, limit: Int, offsetBs: Option[ByteString]) = {
    val offset = offsetBs map (_.toByteArray) map (Int32Value.parseFrom(_).value) getOrElse 0

    (for {
      (userIds, nextOffset) ← Source(group.members.keySet)
        .mapAsync(1)(userId ⇒ userExt.getName(userId, clientUserId) map (userId → _))
        .runFold(Vector.empty[(UserId, String)])(_ :+ _) map { users ⇒
          val tail = users.sortBy(_._2).map(_._1).drop(offset)
          val nextOffset = if (tail.length > limit) Some(Int32Value(offset + limit).toByteArray) else None
          (tail.take(limit), nextOffset)
        }
    } yield LoadMembersResponse(
      userIds = userIds,
      offset = nextOffset map ByteString.copyFrom
    )) pipeTo sender()
  }

  private def getApiMembers(group: GroupState, clientUserId: Int) = {
    if (isMember(group, clientUserId)) {
      group.members.toVector map {
        case (_, m) ⇒
          ApiMember(m.userId, m.inviterUserId, m.invitedAt.toEpochMilli, Some(m.isAdmin))
      }
    } else Vector.empty[ApiMember]
  }
}
