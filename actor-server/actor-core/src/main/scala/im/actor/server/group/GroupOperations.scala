package im.actor.server.group

import akka.pattern.ask
import akka.util.Timeout
import im.actor.api.rpc.AuthorizedClientData
import im.actor.api.rpc.groups.{ ApiGroup, ApiMember }
import im.actor.server.file.Avatar
import im.actor.server.sequence.{ SeqState, SeqStateDate }

import scala.concurrent.{ ExecutionContext, Future }

trait GroupOperations extends Commands with Queries

private[group] sealed trait Commands {
  import GroupCommands._

  def create(groupId: Int, title: String, randomId: Long, userIds: Set[Int])(
    implicit
    peerManagerRegion: GroupProcessorRegion,
    timeout:           Timeout,
    ec:                ExecutionContext,
    client:            AuthorizedClientData
  ): Future[CreateAck] = create(groupId, client.userId, client.authId, title, randomId, userIds)

  def create(groupId: Int, clientUserId: Int, clientAuthId: Long, title: String, randomId: Long, userIds: Set[Int], typ: GroupType.ValueType = GroupType.General)(
    implicit
    peerManagerRegion: GroupProcessorRegion,
    timeout:           Timeout,
    ec:                ExecutionContext
  ): Future[CreateAck] =
    (peerManagerRegion.ref ? Create(groupId, typ, clientUserId, clientAuthId, title, randomId, userIds.toSeq)).mapTo[CreateAck]

  def createInternal(groupId: Int, typ: GroupType.ValueType, creatorUserId: Int, title: String, userIds: Set[Int], isHidden: Boolean, isHistoryShared: Boolean)(
    implicit
    region:  GroupProcessorRegion,
    timeout: Timeout,
    ec:      ExecutionContext
  ): Future[CreateInternalAck] =
    (region.ref ? CreateInternal(groupId, typ, creatorUserId, title, userIds.toSeq, isHidden = Some(isHidden), isHistoryShared = Some(isHistoryShared))).mapTo[CreateInternalAck]

  def makePublic(groupId: Int, description: String)(
    implicit
    region:  GroupProcessorRegion,
    timeout: Timeout,
    ec:      ExecutionContext
  ): Future[MakePublicAck] =
    (region.ref ? MakePublic(groupId, Some(description))).mapTo[MakePublicAck]

  def leaveGroup(groupId: Int, randomId: Long)(
    implicit
    timeout:           Timeout,
    peerManagerRegion: GroupProcessorRegion,
    ec:                ExecutionContext,
    client:            AuthorizedClientData
  ): Future[SeqStateDate] =
    (peerManagerRegion.ref ? Leave(groupId, client.userId, client.authId, randomId)).mapTo[SeqStateDate]

  def kickUser(groupId: Int, kickedUserId: Int, randomId: Long)(
    implicit
    timeout:           Timeout,
    peerManagerRegion: GroupProcessorRegion,
    ec:                ExecutionContext,
    client:            AuthorizedClientData
  ): Future[SeqStateDate] =
    (peerManagerRegion.ref ? Kick(groupId, kickedUserId, client.userId, client.authId, randomId)).mapTo[SeqStateDate]

  def joinGroup(groupId: Int, joiningUserId: Int, joiningUserAuthId: Long, invitingUserId: Int)(
    implicit
    timeout:           Timeout,
    peerManagerRegion: GroupProcessorRegion,
    ec:                ExecutionContext
  ): Future[(SeqStateDate, Vector[Int], Long)] =
    (peerManagerRegion.ref ? Join(groupId, joiningUserId, joiningUserAuthId, invitingUserId)).mapTo[(SeqStateDate, Vector[Int], Long)]

  def joinAfterFirstRead(groupId: Int, joiningUserId: Int, joiningUserAuthId: Long)(
    implicit
    region:  GroupProcessorRegion,
    timeout: Timeout,
    ec:      ExecutionContext
  ): Future[Unit] = (region.ref ? JoinAfterFirstRead(groupId, joiningUserId, joiningUserAuthId)) map (_ ⇒ ())

  def inviteToGroup(groupId: Int, inviteeUserId: Int, randomId: Long)(
    implicit
    timeout:           Timeout,
    peerManagerRegion: GroupProcessorRegion,
    ec:                ExecutionContext,
    client:            AuthorizedClientData
  ): Future[SeqStateDate] =
    (peerManagerRegion.ref ? Invite(groupId, inviteeUserId, client.userId, client.authId, randomId)).mapTo[SeqStateDate]

  def updateAvatar(groupId: Int, clientUserId: Int, clientAuthId: Long, avatarOpt: Option[Avatar], randomId: Long)(
    implicit
    region:  GroupProcessorRegion,
    timeout: Timeout,
    ec:      ExecutionContext
  ): Future[UpdateAvatarAck] = (region.ref ? UpdateAvatar(groupId, clientUserId, clientAuthId, avatarOpt, randomId)).mapTo[UpdateAvatarAck]

  def updateTitle(groupId: Int, clientUserId: Int, clientAuthId: Long, title: String, randomId: Long)(
    implicit
    region:  GroupProcessorRegion,
    timeout: Timeout,
    ec:      ExecutionContext
  ): Future[SeqStateDate] = (region.ref ? UpdateTitle(groupId, clientUserId, clientAuthId, title, randomId)).mapTo[SeqStateDate]

  def updateTopic(groupId: Int, clientUserId: Int, clientAuthId: Long, topic: Option[String], randomId: Long)(
    implicit
    region:  GroupProcessorRegion,
    timeout: Timeout,
    ec:      ExecutionContext
  ): Future[SeqStateDate] = (region.ref ? ChangeTopic(groupId, clientUserId, clientAuthId, topic, randomId)).mapTo[SeqStateDate]

  def updateAbout(groupId: Int, clientUserId: Int, clientAuthId: Long, about: Option[String], randomId: Long)(
    implicit
    region:  GroupProcessorRegion,
    timeout: Timeout,
    ec:      ExecutionContext
  ): Future[SeqStateDate] = (region.ref ? ChangeAbout(groupId, clientUserId, clientAuthId, about, randomId)).mapTo[SeqStateDate]

  def makeUserAdmin(groupId: Int, clientUserId: Int, clientAuthId: Long, candidateId: Int)(
    implicit
    region:  GroupProcessorRegion,
    timeout: Timeout,
    ec:      ExecutionContext
  ): Future[(Vector[ApiMember], SeqState)] = (region.ref ? MakeUserAdmin(groupId, clientUserId, clientAuthId, candidateId)).mapTo[(Vector[ApiMember], SeqState)]

  def revokeIntegrationToken(groupId: Int, clientUserId: Int)(
    implicit
    region:  GroupProcessorRegion,
    timeout: Timeout,
    ec:      ExecutionContext
  ): Future[String] = (region.ref ? RevokeIntegrationToken(groupId, clientUserId)).mapTo[RevokeIntegrationTokenAck] map (_.token)
}

private[group] sealed trait Queries {
  import GroupQueries._

  def getIntegrationToken(groupId: Int, clientUserId: Int)(
    implicit
    region:  GroupViewRegion,
    timeout: Timeout,
    ec:      ExecutionContext
  ): Future[Option[String]] = (region.ref ? GetIntegrationToken(groupId, clientUserId)).mapTo[GetIntegrationTokenResponse] map (_.token)

  //for use in inner services only
  def getIntegrationToken(groupId: Int)(
    implicit
    region:  GroupViewRegion,
    timeout: Timeout,
    ec:      ExecutionContext
  ): Future[Option[String]] = (region.ref ? GetIntegrationTokenInternal(groupId)).mapTo[GetIntegrationTokenResponse] map (_.token) //FIXME

  def getApiStruct(groupId: Int, clientUserId: Int)(
    implicit
    region:  GroupViewRegion,
    timeout: Timeout,
    ec:      ExecutionContext
  ): Future[ApiGroup] = (region.ref ? GetApiStruct(groupId, clientUserId)).mapTo[GetApiStructResponse] map (_.struct)

  def isPublic(groupId: Int)(
    implicit
    region:  GroupViewRegion,
    timeout: Timeout,
    ec:      ExecutionContext
  ): Future[Boolean] = (region.ref ? IsPublic(groupId)).mapTo[IsPublicResponse] map (_.isPublic)

  def isHistoryShared(groupId: Int)(
    implicit
    region:  GroupViewRegion,
    timeout: Timeout,
    ec:      ExecutionContext
  ): Future[Boolean] = (region.ref ? IsHistoryShared(groupId)).mapTo[IsHistorySharedResponse] map (_.isHistoryShared)

  def checkAccessHash(groupId: Int, hash: Long)(
    implicit
    region:  GroupViewRegion,
    timeout: Timeout,
    ec:      ExecutionContext
  ): Future[Boolean] = (region.ref ? CheckAccessHash(groupId, hash)).mapTo[CheckAccessHashResponse] map (_.isCorrect)

  def getMemberIds(groupId: Int)(
    implicit
    region:  GroupViewRegion,
    timeout: Timeout,
    ec:      ExecutionContext
  ): Future[(Seq[Int], Seq[Int], Option[Int])] = (region.ref ? GetMembers(groupId)).mapTo[GetMembersResponse] map (r ⇒ (r.memberIds, r.invitedUserIds, r.botId))

  def getAccessHash(groupId: Int)(
    implicit
    region:  GroupViewRegion,
    timeout: Timeout,
    ec:      ExecutionContext
  ): Future[Long] = (region.ref ? GetAccessHash(groupId)).mapTo[GetAccessHashResponse] map (_.accessHash)
}