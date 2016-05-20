package im.actor.server.group

import akka.http.scaladsl.util.FastFuture
import akka.pattern.ask
import akka.util.Timeout
import com.google.protobuf.ByteString
import im.actor.api.rpc.AuthorizedClientData
import im.actor.api.rpc.groups.{ ApiGroup, ApiGroupFull, ApiMember }
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

  def create(groupId: Int, title: String, randomId: Long, userIds: Set[Int])(implicit client: AuthorizedClientData): Future[CreateAck] =
    create(groupId, client.userId, client.authSid, title, randomId, userIds)

  def create(groupId: Int, clientUserId: Int, clientAuthSid: Int, title: String, randomId: Long, userIds: Set[Int], typ: GroupType.ValueType = GroupType.General): Future[CreateAck] =
    (processorRegion.ref ? Create(groupId, typ, clientUserId, clientAuthSid, title, randomId, userIds.toSeq)).mapTo[CreateAck]

  def createInternal(groupId: Int, typ: GroupType.ValueType, creatorUserId: Int, title: String, userIds: Set[Int], isHidden: Boolean, isHistoryShared: Boolean): Future[CreateInternalAck] =
    (processorRegion.ref ? CreateInternal(groupId, typ, creatorUserId, title, userIds.toSeq, isHidden = Some(isHidden), isHistoryShared = Some(isHistoryShared))).mapTo[CreateInternalAck]

  def makePublic(groupId: Int, description: String): Future[MakePublicAck] =
    (processorRegion.ref ? MakePublic(groupId, Some(description))).mapTo[MakePublicAck]

  def leaveGroup(groupId: Int, randomId: Long)(
    implicit
    client: AuthorizedClientData
  ): Future[SeqStateDate] =
    (processorRegion.ref ? Leave(groupId, client.userId, client.authSid, randomId)).mapTo[SeqStateDate]

  def kickUser(groupId: Int, kickedUserId: Int, randomId: Long)(implicit client: AuthorizedClientData): Future[SeqStateDate] =
    (processorRegion.ref ? Kick(groupId, kickedUserId, client.userId, client.authSid, randomId)).mapTo[SeqStateDate]

  def joinGroup(groupId: Int, joiningUserId: Int, joiningUserAuthSid: Int, invitingUserId: Int): Future[(SeqStateDate, Vector[Int], Long)] =
    (processorRegion.ref ? Join(groupId, joiningUserId, joiningUserAuthSid, invitingUserId)).mapTo[(SeqStateDate, Vector[Int], Long)]

  def joinAfterFirstRead(groupId: Int, joiningUserId: Int, joiningUserAuthSid: Int): Future[Unit] =
    (processorRegion.ref ? JoinAfterFirstRead(groupId, joiningUserId, joiningUserAuthSid)) map (_ ⇒ ())

  def inviteToGroup(groupId: Int, inviteeUserId: Int, randomId: Long)(implicit client: AuthorizedClientData): Future[SeqStateDate] =
    inviteToGroup(client.userId, groupId, inviteeUserId, randomId)

  /**
   * The reason we make block check here is cause invite happens in two major places across server code:
   * • when group is created. we check that only group creator adds only users, that didn't block him.
   * • when user invites another user. We need to check if invitee blocked inviter.
   *
   * We don't need double check on both group creation and invite send. So we do this check here
   */
  def inviteToGroup(clientUserId: Int, groupId: Int, inviteeUserId: Int, randomId: Long): Future[SeqStateDate] = {
    withNonBlockedUser(clientUserId, inviteeUserId)(
      default = (processorRegion.ref ? Invite(groupId, inviteeUserId, clientUserId, randomId)).mapTo[SeqStateDate],
      failed = FastFuture.failed(GroupErrors.BlockedByUser)
    )
  }

  def updateAvatar(groupId: Int, clientUserId: Int, avatarOpt: Option[Avatar], randomId: Long): Future[UpdateAvatarAck] =
    (processorRegion.ref ? UpdateAvatar(groupId, clientUserId, avatarOpt, randomId)).mapTo[UpdateAvatarAck]

  def updateTitle(groupId: Int, clientUserId: Int, title: String, randomId: Long): Future[SeqStateDate] =
    (processorRegion.ref ? UpdateTitle(groupId, clientUserId, title, randomId)).mapTo[SeqStateDate]

  def updateTopic(groupId: Int, clientUserId: Int, topic: Option[String], randomId: Long): Future[SeqStateDate] =
    (processorRegion.ref ? ChangeTopic(groupId, clientUserId, topic, randomId)).mapTo[SeqStateDate]

  def updateAbout(groupId: Int, clientUserId: Int, about: Option[String], randomId: Long): Future[SeqStateDate] =
    (processorRegion.ref ? ChangeAbout(groupId, clientUserId, about, randomId)).mapTo[SeqStateDate]

  def makeUserAdmin(groupId: Int, clientUserId: Int, candidateId: Int): Future[(Vector[ApiMember], SeqStateDate)] =
    (processorRegion.ref ? MakeUserAdmin(groupId, clientUserId, candidateId)).mapTo[(Vector[ApiMember], SeqStateDate)]

  def revokeIntegrationToken(groupId: Int, clientUserId: Int): Future[String] =
    (processorRegion.ref ? RevokeIntegrationToken(groupId, clientUserId)).mapTo[RevokeIntegrationTokenAck] map (_.token)

  def transferOwnership(groupId: Int, clientUserId: Int, userId: Int): Future[SeqState] =
    (processorRegion.ref ? TransferOwnership(groupId, clientUserId, userId)).mapTo[SeqState]
}

private[group] sealed trait Queries {
  import GroupQueries._

  val viewRegion: GroupViewRegion

  implicit val timeout: Timeout
  implicit val ec: ExecutionContext

  def getIntegrationToken(groupId: Int, clientUserId: Int): Future[Option[String]] =
    (viewRegion.ref ? GetIntegrationToken(groupId, clientUserId)).mapTo[GetIntegrationTokenResponse] map (_.token)

  //for use in inner services only
  def getIntegrationToken(groupId: Int): Future[Option[String]] =
    (viewRegion.ref ? GetIntegrationTokenInternal(groupId)).mapTo[GetIntegrationTokenResponse] map (_.token) //FIXME

  def getApiStruct(groupId: Int, clientUserId: Int): Future[ApiGroup] =
    (viewRegion.ref ? GetApiStruct(groupId, clientUserId)).mapTo[GetApiStructResponse] map (_.struct)

  def getApiFullStruct(groupId: Int, clientUserId: Int): Future[ApiGroupFull] =
    (viewRegion.ref ? GetApiFullStruct(groupId, clientUserId)).mapTo[GetApiFullStructResponse] map (_.struct)

  def isPublic(groupId: Int): Future[Boolean] =
    (viewRegion.ref ? IsPublic(groupId)).mapTo[IsPublicResponse] map (_.isPublic)

  def isHistoryShared(groupId: Int): Future[Boolean] =
    (viewRegion.ref ? IsHistoryShared(groupId)).mapTo[IsHistorySharedResponse] map (_.isHistoryShared)

  def checkAccessHash(groupId: Int, hash: Long): Future[Boolean] =
    (viewRegion.ref ? CheckAccessHash(groupId, hash)).mapTo[CheckAccessHashResponse] map (_.isCorrect)

  def getMemberIds(groupId: Int): Future[(Seq[Int], Seq[Int], Option[Int])] =
    (viewRegion.ref ? GetMembers(groupId)).mapTo[GetMembersResponse] map (r ⇒ (r.memberIds, r.invitedUserIds, r.botId))

  def isMember(groupId: Int, userId: Int): Future[Boolean] =
    getMemberIds(groupId) map (_._1.contains(userId))

  def getAccessHash(groupId: Int): Future[Long] =
    (viewRegion.ref ? GetAccessHash(groupId)).mapTo[GetAccessHashResponse] map (_.accessHash)

  def getTitle(groupId: Int): Future[String] =
    (viewRegion.ref ? GetTitle(groupId)).mapTo[GetTitleResponse] map (_.title)

  def loadMembers(groupId: Int, clientUserId: Int, limit: Int, offset: Option[Array[Byte]]) =
    (viewRegion.ref ? LoadMembers(groupId, clientUserId, limit, offset map ByteString.copyFrom)).mapTo[LoadMembersResponse] map (r ⇒ r.userIds → r.offset.map(_.toByteArray))
}