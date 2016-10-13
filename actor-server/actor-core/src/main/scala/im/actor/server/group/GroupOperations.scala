package im.actor.server.group

import akka.http.scaladsl.util.FastFuture
import akka.pattern.ask
import akka.util.Timeout
import com.google.protobuf.ByteString
import im.actor.api.rpc.AuthorizedClientData
import im.actor.api.rpc.groups.{ ApiAdminSettings, ApiGroup, ApiGroupFull, ApiMember }
import im.actor.server.dialog.UserAcl
import im.actor.server.file.Avatar
import im.actor.server.sequence.{ SeqState, SeqStateDate }

import scala.concurrent.{ ExecutionContext, Future }

trait GroupOperations extends Commands with Queries

private[group] sealed trait Commands extends UserAcl {
  import GroupCommands._

  val processorRegion: GroupProcessorRegion

  implicit val timeout: Timeout
  implicit val ec: ExecutionContext

  // TODO: find usages, replace GroupType
  def create(groupId: Int, clientUserId: Int, clientAuthId: Long, title: String, randomId: Long, userIds: Set[Int], typ: GroupType.ValueType = GroupType.General): Future[CreateAck] = {
    (processorRegion.ref ?
      GroupEnvelope(groupId)
      .withCreate(Create(typ.value, clientUserId, clientAuthId, title, randomId, userIds.toSeq))).mapTo[CreateAck] //FIXME: typ.value
  }

  // TODO: REMOVE. I guess it's obsolete
  def makePublic(groupId: Int, description: String): Future[Unit] = FastFuture.failed(new RuntimeException("Unimplemented!"))
  //      (processorRegion.ref ? MakePublic(groupId, Some(description))).mapTo[MakePublicAck]

  def leaveGroup(groupId: Int, randomId: Long)(implicit client: AuthorizedClientData): Future[SeqStateDate] =
    (processorRegion.ref ?
      GroupEnvelope(groupId)
      .withLeave(Leave(client.userId, client.authId, randomId))).mapTo[SeqStateDate]

  def kickUser(groupId: Int, kickedUserId: Int, randomId: Long)(implicit client: AuthorizedClientData): Future[SeqStateDate] =
    (processorRegion.ref ?
      GroupEnvelope(groupId)
      .withKick(Kick(kickedUserId, client.userId, client.authId, randomId))).mapTo[SeqStateDate]

  def joinGroup(groupId: Int, joiningUserId: Int, joiningUserAuthId: Long, invitingUserId: Option[Int]): Future[(SeqStateDate, Vector[Int], Long)] =
    (processorRegion.ref ?
      GroupEnvelope(groupId)
      .withJoin(Join(joiningUserId, joiningUserAuthId, invitingUserId = invitingUserId))).mapTo[(SeqStateDate, Vector[Int], Long)]

  def inviteToGroup(groupId: Int, inviteeUserId: Int, randomId: Long)(implicit client: AuthorizedClientData): Future[SeqStateDate] =
    inviteToGroup(client.userId, client.authId, groupId, inviteeUserId, randomId)

  /**
   * The reason we make block check here is cause invite happens in two major places across server code:
   * • when group is created. we check that only group creator adds only users, that didn't block him.
   * • when user invites another user. We need to check if invitee blocked inviter.
   *
   * We don't need double check on both group creation and invite send. So we do this check here
   */
  def inviteToGroup(clientUserId: Int, clientAuthId: Long, groupId: Int, inviteeUserId: Int, randomId: Long): Future[SeqStateDate] = {
    withNonBlockedUser(clientUserId, inviteeUserId)(
      default = (processorRegion.ref ?
      GroupEnvelope(groupId)
      .withInvite(Invite(inviteeUserId, clientUserId, clientAuthId, randomId))).mapTo[SeqStateDate],
      failed = FastFuture.failed(GroupErrors.BlockedByUser)
    )
  }

  def updateAvatar(groupId: Int, clientUserId: Int, clientAuthId: Long, avatarOpt: Option[Avatar], randomId: Long): Future[UpdateAvatarAck] =
    (processorRegion.ref ?
      GroupEnvelope(groupId)
      .withUpdateAvatar(UpdateAvatar(clientUserId, clientAuthId, avatarOpt, randomId))).mapTo[UpdateAvatarAck]

  def updateTitle(groupId: Int, clientUserId: Int, clientAuthId: Long, title: String, randomId: Long): Future[SeqStateDate] =
    (processorRegion.ref ?
      GroupEnvelope(groupId)
      .withUpdateTitle(UpdateTitle(clientUserId, clientAuthId, title, randomId))).mapTo[SeqStateDate]

  def updateTopic(groupId: Int, clientUserId: Int, clientAuthId: Long, topic: Option[String], randomId: Long): Future[SeqStateDate] =
    (processorRegion.ref ?
      GroupEnvelope(groupId)
      .withUpdateTopic(UpdateTopic(clientUserId, clientAuthId, topic, randomId))).mapTo[SeqStateDate]

  def updateAbout(groupId: Int, clientUserId: Int, clientAuthId: Long, about: Option[String], randomId: Long): Future[SeqStateDate] =
    (processorRegion.ref ?
      GroupEnvelope(groupId)
      .withUpdateAbout(UpdateAbout(clientUserId, clientAuthId, about, randomId))).mapTo[SeqStateDate]

  def updateShortName(groupId: Int, clientUserId: Int, clientAuthId: Long, shortName: Option[String]): Future[SeqState] =
    (processorRegion.ref ?
      GroupEnvelope(groupId)
      .withUpdateShortName(UpdateShortName(clientUserId, clientAuthId, shortName))).mapTo[SeqState]

  def makeUserAdmin(groupId: Int, clientUserId: Int, clientAuthId: Long, candidateId: Int): Future[(Vector[ApiMember], SeqStateDate)] =
    (processorRegion.ref ?
      GroupEnvelope(groupId)
      .withMakeUserAdmin(MakeUserAdmin(clientUserId, clientAuthId, candidateId))).mapTo[(Vector[ApiMember], SeqStateDate)]

  def dismissUserAdmin(groupId: Int, clientUserId: Int, clientAuthId: Long, targetUserId: Int): Future[SeqState] =
    (processorRegion.ref ?
      GroupEnvelope(groupId)
      .withDismissUserAdmin(DismissUserAdmin(clientUserId, clientAuthId, targetUserId))).mapTo[SeqState]

  def revokeIntegrationToken(groupId: Int, clientUserId: Int): Future[String] =
    (processorRegion.ref ?
      GroupEnvelope(groupId)
      .withRevokeToken(RevokeIntegrationToken(clientUserId))).mapTo[RevokeIntegrationTokenAck] map (_.token)

  def transferOwnership(groupId: Int, clientUserId: Int, clientAuthId: Long, newOwnerId: Int): Future[SeqState] =
    (processorRegion.ref ?
      GroupEnvelope(groupId)
      .withTransferOwnership(TransferOwnership(clientUserId, clientAuthId, newOwnerId))).mapTo[SeqState]

  def updateAdminSettings(groupId: Int, clientUserId: Int, settings: ApiAdminSettings): Future[Unit] =
    (processorRegion.ref ?
      GroupEnvelope(groupId)
      .withUpdateAdminSettings(UpdateAdminSettings(clientUserId, AdminSettings.apiToBitMask(settings)))).mapTo[UpdateAdminSettingsAck] map (_ ⇒ ())

  def makeHistoryShared(groupId: Int, clientUserId: Int, clientAuthId: Long): Future[SeqState] =
    (processorRegion.ref ?
      GroupEnvelope(groupId)
      .withMakeHistoryShared(MakeHistoryShared(clientUserId, clientAuthId))).mapTo[SeqState]

  def deleteGroup(groupId: Int, clientUserId: Int, clientAuthId: Long): Future[SeqState] =
    (processorRegion.ref ?
      GroupEnvelope(groupId)
      .withDeleteGroup(DeleteGroup(clientUserId, clientAuthId))).mapTo[SeqState]

  def addExt(groupId: Int, ext: GroupExt): Future[Unit] =
    (processorRegion.ref ?
      GroupEnvelope(groupId)
      .withAddExt(AddExt(Some(ext)))).mapTo[AddExtAck] map (_ ⇒ ())

  def removeExt(groupId: Int, key: String): Future[Unit] =
    (processorRegion.ref ?
      GroupEnvelope(groupId)
      .withRemoveExt(RemoveExt(key))).mapTo[RemoveExtAck] map (_ ⇒ ())

}

private[group] sealed trait Queries {
  import GroupQueries._

  val viewRegion: GroupViewRegion

  implicit val timeout: Timeout
  implicit val ec: ExecutionContext

  def getIntegrationToken(groupId: Int, clientUserId: Int): Future[Option[String]] =
    (viewRegion.ref ?
      GroupEnvelope(groupId)
      .withGetIntegrationToken(GetIntegrationToken(Some(clientUserId)))).mapTo[GetIntegrationTokenResponse] map (_.token)

  //for use in inner services only
  def getIntegrationToken(groupId: Int): Future[Option[String]] =
    (viewRegion.ref ?
      GroupEnvelope(groupId)
      .withGetIntegrationToken(GetIntegrationToken(clientUserId = None))).mapTo[GetIntegrationTokenResponse] map (_.token)

  def getApiStruct(groupId: Int, clientUserId: Int, loadGroupMembers: Boolean = true): Future[ApiGroup] =
    (viewRegion.ref ?
      GroupEnvelope(groupId)
      .withGetApiStruct(GetApiStruct(clientUserId, loadGroupMembers))).mapTo[GetApiStructResponse] map (_.struct)

  def getApiFullStruct(groupId: Int, clientUserId: Int): Future[ApiGroupFull] =
    (viewRegion.ref ?
      GroupEnvelope(groupId)
      .withGetApiFullStruct(GetApiFullStruct(clientUserId))).mapTo[GetApiFullStructResponse] map (_.struct)

  def isChannel(groupId: Int): Future[Boolean] =
    (viewRegion.ref ?
      GroupEnvelope(groupId)
      .withIsChannel(IsChannel())).mapTo[IsChannelResponse] map (_.isChannel)

  def isHistoryShared(groupId: Int): Future[Boolean] =
    (viewRegion.ref ?
      GroupEnvelope(groupId)
      .withIsHistoryShared(IsHistoryShared())).mapTo[IsHistorySharedResponse] map (_.isHistoryShared)

  def checkAccessHash(groupId: Int, hash: Long): Future[Boolean] =
    (viewRegion.ref ?
      GroupEnvelope(groupId)
      .withCheckAccessHash(CheckAccessHash(hash))).mapTo[CheckAccessHashResponse] map (_.isCorrect)

  // TODO: should be signed as internal API, and become narrowly scoped
  // never use it in for client queries
  //(memberIds, invitedUserIds, botId)
  def getMemberIds(groupId: Int): Future[(Seq[Int], Seq[Int], Option[Int])] = //TODO: prepare for channel
    (viewRegion.ref ?
      GroupEnvelope(groupId)
      .withGetMembers(GetMembers())).mapTo[GetMembersResponse] map (r ⇒ (r.memberIds, r.invitedUserIds, r.botId))

  // TODO: should be signed as internal API
  // TODO: better name maybe
  def canSendMessage(groupId: Int, clientUserId: Int): Future[CanSendMessageInfo] =
    (viewRegion.ref ?
      GroupEnvelope(groupId)
      .withCanSendMessage(CanSendMessage(clientUserId))).mapTo[CanSendMessageResponse] map {
        case CanSendMessageResponse(canSend, isChannel, memberIds, botId) ⇒
          CanSendMessageInfo(canSend, isChannel, memberIds.toSet, botId)
      }

  //TODO: move to separate Query.
  def isMember(groupId: Int, userId: Int): Future[Boolean] =
    getMemberIds(groupId) map (_._1.contains(userId))

  def getAccessHash(groupId: Int): Future[Long] =
    (viewRegion.ref ?
      GroupEnvelope(groupId)
      .withGetAccessHash(GetAccessHash())).mapTo[GetAccessHashResponse] map (_.accessHash)

  def getTitle(groupId: Int): Future[String] =
    (viewRegion.ref ?
      GroupEnvelope(groupId)
      .withGetTitle(GetTitle())).mapTo[GetTitleResponse] map (_.title)

  def loadMembers(groupId: Int, clientUserId: Int, limit: Int, offset: Option[Array[Byte]]) =
    (viewRegion.ref ?
      GroupEnvelope(groupId)
      .withLoadMembers(LoadMembers(clientUserId, limit, offset map ByteString.copyFrom))).mapTo[LoadMembersResponse] map { resp ⇒
        (
          resp.members map { m ⇒
            ApiMember(m.userId, m.inviterUserId, m.invitedAt, isAdmin = Some(m.isAdmin))
          },
          resp.offset.map(_.toByteArray)
        )
      }

  def loadAdminSettings(groupId: Int, clientUserId: Int): Future[ApiAdminSettings] = {
    (viewRegion.ref ?
      GroupEnvelope(groupId)
      .withLoadAdminSettings(LoadAdminSettings(clientUserId))).mapTo[LoadAdminSettingsResponse] map (_.settings)
  }
}

final case class CanSendMessageInfo(
  canSend:   Boolean,
  isChannel: Boolean,
  memberIds: Set[Int],
  botId:     Option[Int]
)
