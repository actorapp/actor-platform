package im.actor.server.group

import akka.http.scaladsl.util.FastFuture
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.google.protobuf.ByteString
import com.google.protobuf.wrappers.Int32Value
import im.actor.api.rpc.groups.{ ApiGroup, ApiGroupFull, ApiGroupType, ApiMember }
import im.actor.server.group.GroupQueries._

trait GroupQueryHandlers {
  self: GroupProcessor ⇒

  import im.actor.server.ApiConversions._

  protected def getAccessHash =
    FastFuture.successful(GetAccessHashResponse(state.accessHash))

  protected def getTitle =
    FastFuture.successful(GetTitleResponse(state.title))

  protected def getIntegrationToken(optUserId: Option[Int]) = {
    val canViewToken = optUserId.forall(state.isAdmin)
    FastFuture.successful(GetIntegrationTokenResponse(
      if (canViewToken) state.bot.map(_.token) else None
    ))
  }

  protected def getMembers =
    FastFuture.successful {
      GetMembersResponse(
        memberIds = state.members.keySet.toSeq,
        invitedUserIds = state.invitedUserIds.toSeq,
        botId = state.bot.map(_.userId)
      )
    }

  protected def loadMembers(clientUserId: Int, limit: Int, offsetBs: Option[ByteString]) = {
    implicit val mat = ActorMaterializer()
    val offset = offsetBs map (_.toByteArray) map (Int32Value.parseFrom(_).value) getOrElse 0

    for {
      (userIds, nextOffset) ← Source(state.members.keySet)
        .mapAsync(1)(userId ⇒ userExt.getName(userId, clientUserId) map (userId → _))
        .runFold(Vector.empty[(Int, String)])(_ :+ _) map { users ⇒
          val tail = users.sortBy(_._2).map(_._1).drop(offset)
          val nextOffset = if (tail.length > limit) Some(Int32Value(offset + limit).toByteArray) else None
          (tail.take(limit), nextOffset)
        }
    } yield LoadMembersResponse(
      userIds = userIds,
      offset = nextOffset map ByteString.copyFrom
    )
  }

  protected def isPublic =
    FastFuture.successful(IsPublicResponse(isPublic = state.typ == GroupType.Public))

  protected def isHistoryShared =
    FastFuture.successful(IsHistorySharedResponse(state.isHistoryShared))

  //TODO: add ext!
  //TODO: what if state changes during request?
  protected def getApiStruct(clientUserId: Int) = {
    val isMember = state.isMember(clientUserId)
    val apiMembers = getApiMembers(state, clientUserId)

    FastFuture.successful {
      GetApiStructResponse(
        ApiGroup(
          groupId,
          accessHash = state.accessHash,
          title = state.title,
          avatar = state.avatar,
          isMember = Some(isMember),
          creatorUserId = state.creatorUserId,
          members = apiMembers,
          createDate = extractCreatedMillis(state),
          isAdmin = Some(state.isAdmin(clientUserId)),
          theme = state.topic,
          about = state.about,
          isHidden = Some(state.isHidden),
          ext = None,
          membersCount = Some(apiMembers.size),
          groupType = Some(state.typ match {
            case GroupType.Channel ⇒ ApiGroupType.CHANNEL
            case GroupType.General | GroupType.Public | GroupType.Unrecognized(_) ⇒ ApiGroupType.GROUP
          }),
          canSendMessage = None
        )
      )
    }
  }

  //TODO: add ext!
  protected def getApiFullStruct(clientUserId: Int) =
    FastFuture.successful {
      GetApiFullStructResponse(
        ApiGroupFull(
          groupId,
          theme = state.topic,
          about = state.about,
          ownerUserId = state.creatorUserId,
          createDate = extractCreatedMillis(state),
          ext = None,
          canViewMembers = Some(state.canViewMembers(clientUserId)),
          canInvitePeople = Some(state.canInvitePeople(clientUserId)),
          isSharedHistory = Some(state.isHistoryShared),
          isAsyncMembers = Some(state.members.size > 100),
          members = getApiMembers(state, clientUserId)
        )
      )
    }

  protected def checkAccessHash(hash: Long) =
    FastFuture.successful(CheckAccessHashResponse(isCorrect = state.accessHash == hash))

  private def extractCreatedMillis(group: GroupState): Long =
    group.createdAt.map(_.toEpochMilli).getOrElse(throw new RuntimeException("No date created provided for group!"))

  private def getApiMembers(group: GroupState, clientUserId: Int) =
    if (state.isMember(clientUserId)) {
      group.members.toVector map {
        case (_, m) ⇒
          ApiMember(m.userId, m.inviterUserId, m.invitedAt.toEpochMilli, Some(m.isAdmin))
      }
    } else Vector.empty[ApiMember]

}
