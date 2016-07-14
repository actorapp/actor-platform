package im.actor.server.group

import akka.http.scaladsl.util.FastFuture
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.google.protobuf.ByteString
import com.google.protobuf.wrappers.Int32Value
import im.actor.api.rpc.groups.{ ApiGroup, ApiGroupFull, ApiGroupType, ApiMember }
import im.actor.server.group.GroupQueries._
import im.actor.server.group.GroupType.{ Channel, General, Public }

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
    FastFuture.successful(GetIntegrationTokenResponse(
      if (canViewToken) state.bot.map(_.token) else None
    ))
  }

  //TODO: do something with this method. Will this method used in "client" context.
  // If not - don't change it. Maybe rename to `getMembersInternal`
  protected def getMembers: Future[GetMembersResponse] =
    FastFuture.successful {
      GetMembersResponse(
        memberIds = state.members.keySet.toSeq,
        invitedUserIds = state.invitedUserIds.toSeq,
        botId = state.bot.map(_.userId)
      )
    }

  //TODO: rewrite to sort by online + name. Won't work like this
  // we can subscribe group object to group onlines! When online comes, we reorder key-set. Use that key set as source.
  protected def loadMembers(clientUserId: Int, limit: Int, offsetBs: Option[ByteString]): Future[LoadMembersResponse] = {
    def load = {
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

    state.typ match {
      case General | Public ⇒ load
      case Channel ⇒
        if (state.isAdmin(clientUserId)) load
        else FastFuture.successful(LoadMembersResponse(Seq.empty, offsetBs))
    }
  }

  protected def isPublic =
    FastFuture.successful(IsPublicResponse(isPublic = state.typ == GroupType.Public))

  protected def isHistoryShared =
    FastFuture.successful(IsHistorySharedResponse(state.isHistoryShared))

  //TODO: add ext!
  //TODO: what if state changes during request?
  protected def getApiStruct(clientUserId: Int) = {
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
          members = members,
          createDate = extractCreatedAtMillis(state),
          isAdmin = Some(state.isAdmin(clientUserId)),
          theme = state.topic,
          about = state.about,
          isHidden = Some(state.isHidden),
          ext = None,
          membersCount = Some(count),
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
          createDate = extractCreatedAtMillis(state),
          ext = None,
          canViewMembers = Some(state.canViewMembers(clientUserId)),
          canInvitePeople = Some(state.canInvitePeople(clientUserId)),
          isSharedHistory = Some(state.isHistoryShared),
          isAsyncMembers = Some(state.members.size > 100),
          members = membersAndCount(state, clientUserId)._1
        )
      )
    }

  protected def checkAccessHash(hash: Long) =
    FastFuture.successful(CheckAccessHashResponse(isCorrect = state.accessHash == hash))

  protected def canSendMessage(clientUserId: Int): Future[CanSendMessageResponse] =
    FastFuture.successful {
      val canSend = state.bot.exists(_.userId == clientUserId) || {
        state.typ match {
          case General | Public ⇒ state.isMember(clientUserId)
          case Channel          ⇒ state.isAdmin(clientUserId)
        }
      }
      CanSendMessageResponse(
        canSend = canSend,
        isChannel = state.typ.isChannel,
        memberIds = state.memberIds.toSeq,
        botId = state.bot.map(_.userId)
      )
    }

  private def extractCreatedAtMillis(group: GroupState): Long =
    group.createdAt.map(_.toEpochMilli).getOrElse(throw new RuntimeException("No date created provided for group!"))

  /**
   * Return group members, and number of members.
   * If `clientUserId` is not a group member, return empty members list and 0
   * For `General` and `Public` groups return all members and their number.
   * For `Channel` return members list only if `clientUserId` is group admin. Otherwise return empty members list and real members count
   */
  private def membersAndCount(group: GroupState, clientUserId: Int): (Vector[ApiMember], Int) = {
    def apiMembers = group.members.toVector map {
      case (_, m) ⇒
        ApiMember(m.userId, m.inviterUserId, m.invitedAt.toEpochMilli, Some(m.isAdmin))
    }

    if (state.isMember(clientUserId)) {
      state.typ match {
        case General | Public ⇒
          apiMembers → group.membersCount
        case Channel ⇒
          if (state.isAdmin(clientUserId))
            apiMembers → group.membersCount
          else
            Vector.empty[ApiMember] → group.membersCount
      }
    } else {
      Vector.empty[ApiMember] → 0
    }
  }

}
