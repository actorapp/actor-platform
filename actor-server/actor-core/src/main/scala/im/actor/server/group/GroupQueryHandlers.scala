package im.actor.server.group

import akka.http.scaladsl.util.FastFuture
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.google.protobuf.ByteString
import com.google.protobuf.wrappers.Int32Value
import im.actor.api.rpc.groups._
import im.actor.server.group.GroupErrors.{ IncorrectGroupType, NoPermission, NotOwner }
import im.actor.server.group.GroupQueries._
import im.actor.server.group.GroupType.{ Channel, General, Unrecognized }
import im.actor.util.cache.CacheHelpers.withCachedFuture

import scala.concurrent.Future

trait GroupQueryHandlers {
  self: GroupProcessor ⇒

  import im.actor.server.ApiConversions._

  protected def getAccessHash =
    FastFuture.successful(GetAccessHashResponse(state.accessHash))

  protected def getTitle =
    FastFuture.successful(GetTitleResponse(state.title))

  protected def getIntegrationToken(optUserId: Option[Int]): Future[GetIntegrationTokenResponse] = {
    val canViewToken = optUserId.forall(state.isAdmin)
    val allowedToView = optUserId.forall(state.isMember)
    if (allowedToView) {
      FastFuture.successful(GetIntegrationTokenResponse(
        if (canViewToken) state.bot.map(_.token) else None
      ))
    } else {
      FastFuture.failed(NoPermission)
    }
  }

  //TODO: This is internal server API. Properly name it, for example `getMembersInternal`
  protected def getMembers: Future[GetMembersResponse] =
    FastFuture.successful {
      GetMembersResponse(
        memberIds = state.memberIds.toSeq,
        invitedUserIds = state.invitedUserIds.toSeq,
        botId = state.bot.map(_.userId)
      )
    }

  protected def loadMembers(clientUserId: Int, limit: Int, offsetBs: Option[ByteString]): Future[LoadMembersResponse] = {
    def load = {
      implicit val mat = ActorMaterializer()
      val offset = offsetBs map (_.toByteArray) map (Int32Value.parseFrom(_).value) getOrElse 0

      for {
        (members, nextOffset) ← Source(state.members)
          .mapAsync(1) {
            case (userId, member) ⇒
              withCachedFuture[java.lang.Integer, String](userId) {
                userExt.getName(userId, clientUserId)
              } map { name ⇒ member → name }
          }
          .runFold(Vector.empty[(Member, String)])(_ :+ _) map { users ⇒
            val tail = users.sortBy(_._2).map(_._1).drop(offset)
            val nextOffset = if (tail.length > limit) Some(Int32Value(offset + limit).toByteArray) else None
            (tail.take(limit), nextOffset)
          }
      } yield LoadMembersResponse(
        members = members map {
        case Member(userId, inviterUserId, invitedAt, isAdmin) ⇒
          GroupMember(
            userId,
            inviterUserId,
            invitedAt.toEpochMilli,
            isAdmin
          )
      },
        offset = nextOffset map ByteString.copyFrom
      )
    }

    state.groupType match {
      case General ⇒ load
      case Channel ⇒
        if (state.isAdmin(clientUserId)) load
        else FastFuture.successful(LoadMembersResponse(Seq.empty, offsetBs))
      case Unrecognized(v) ⇒ throw IncorrectGroupType(v)
    }
  }

  protected def isChannel =
    FastFuture.successful(IsChannelResponse(state.groupType.isChannel))

  protected def isHistoryShared =
    FastFuture.successful(IsHistorySharedResponse(state.isHistoryShared))

  //TODO: add ext!
  //TODO: what if state changes during request?
  protected def getApiStruct(clientUserId: Int, loadGroupMembers: Boolean) = {
    val isMember = state.isMember(clientUserId)
    val (members, count) = membersAndCount(state, clientUserId)

    FastFuture.successful {
      GetApiStructResponse(
        ApiGroup(
          groupId,
          accessHash = state.accessHash,
          title = state.title,
          avatar = state.avatar,
          isMember = Some(isMember),
          creatorUserId = state.creatorUserId,
          members = if (loadGroupMembers) members else Vector.empty,
          createDate = extractCreatedAtMillis(state),
          isAdmin = Some(state.isAdmin(clientUserId)),
          theme = state.topic,
          about = state.about,
          isHidden = Some(state.isHidden),
          ext = Some(extToApi(state.exts)),
          membersCount = Some(count),
          groupType = Some(state.groupType match {
            case Channel         ⇒ ApiGroupType.CHANNEL
            case General         ⇒ ApiGroupType.GROUP
            case Unrecognized(v) ⇒ throw IncorrectGroupType(v)
          }),
          permissions = Some(state.permissions.groupFor(clientUserId)),
          isDeleted = Some(state.isDeleted)
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
          ownerUserId = state.getShowableOwner(clientUserId),
          createDate = extractCreatedAtMillis(state),
          ext = None,
          isSharedHistory = Some(state.isHistoryShared),
          isAsyncMembers = Some(state.isAsyncMembers),
          members = membersAndCount(state, clientUserId)._1,
          shortName = state.shortName,
          permissions = Some(state.permissions.fullFor(clientUserId))
        )
      )
    }

  protected def checkAccessHash(hash: Long) =
    FastFuture.successful(CheckAccessHashResponse(isCorrect = state.accessHash == hash))

  protected def canSendMessage(clientUserId: Int): Future[CanSendMessageResponse] =
    FastFuture.successful {
      val canSend = state.bot.exists(_.userId == clientUserId) || {
        state.groupType match {
          case General         ⇒ state.isMember(clientUserId)
          case Channel         ⇒ state.isAdmin(clientUserId)
          case Unrecognized(v) ⇒ throw IncorrectGroupType(v)
        }
      }
      CanSendMessageResponse(
        canSend = canSend,
        isChannel = state.groupType.isChannel,
        memberIds = state.memberIds.toSeq,
        botId = state.bot.map(_.userId)
      )
    }

  protected def loadAdminSettings(clientUserId: Int): Future[LoadAdminSettingsResponse] = {
    if (state.permissions.canEditAdminSettings(clientUserId)) {
      FastFuture.successful {
        LoadAdminSettingsResponse(
          ApiAdminSettings(
            showAdminsToMembers = state.adminSettings.showAdminsToMembers,
            canMembersInvite = state.adminSettings.canMembersInvite,
            canMembersEditGroupInfo = state.adminSettings.canMembersEditGroupInfo,
            canAdminsEditGroupInfo = state.adminSettings.canAdminsEditGroupInfo,
            showJoinLeaveMessages = state.adminSettings.showJoinLeaveMessages
          )
        )
      }
    } else {
      FastFuture.failed(NotOwner)
    }
  }

  private def extractCreatedAtMillis(group: GroupState): Long =
    group.createdAt.map(_.toEpochMilli).getOrElse(throw new RuntimeException("No date created provided for group!"))

  /**
   * Return group members, and number of members.
   * If `clientUserId` is not a group member, return empty members list and 0 members count
   * If group is group with async members - return list with single client user and real members count
   * If group is regular group - return all group members and real members count
   */
  private def membersAndCount(group: GroupState, clientUserId: Int): (Vector[ApiMember], Int) = {
    def apiMembers = group.members.toVector map {
      case (_, m) ⇒
        ApiMember(m.userId, m.inviterUserId, m.invitedAt.toEpochMilli, Some(m.isAdmin))
    }

    if (state.isMember(clientUserId)) {
      if (state.isAsyncMembers) {
        // compatibility with old clients
        apiMembers.find(_.userId == clientUserId).toVector → group.membersCount
      } else {
        apiMembers → group.membersCount
      }
    } else {
      Vector.empty[ApiMember] → 0
    }
  }

}
