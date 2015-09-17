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

  val processorRegion: GroupProcessorRegion

  implicit val timeout: Timeout
  implicit val ec: ExecutionContext

  def create(groupId: Int, title: String, randomId: Long, userIds: Set[Int])(implicit client: AuthorizedClientData): Future[CreateAck] =
    create(groupId, client.userId, client.authId, title, randomId, userIds)

  def create(groupId: Int, clientUserId: Int, clientAuthId: Long, title: String, randomId: Long, userIds: Set[Int], typ: GroupType.ValueType = GroupType.General): Future[CreateAck] =
    (processorRegion.ref ? Create(groupId, typ, clientUserId, clientAuthId, title, randomId, userIds.toSeq)).mapTo[CreateAck]

  def createInternal(groupId: Int, typ: GroupType.ValueType, creatorUserId: Int, title: String, userIds: Set[Int], isHidden: Boolean, isHistoryShared: Boolean): Future[CreateInternalAck] =
    (processorRegion.ref ? CreateInternal(groupId, typ, creatorUserId, title, userIds.toSeq, isHidden = Some(isHidden), isHistoryShared = Some(isHistoryShared))).mapTo[CreateInternalAck]

  def makePublic(groupId: Int, description: String): Future[MakePublicAck] =
    (processorRegion.ref ? MakePublic(groupId, Some(description))).mapTo[MakePublicAck]

  def leaveGroup(groupId: Int, randomId: Long)(
    implicit
    client: AuthorizedClientData
  ): Future[SeqStateDate] =
    (processorRegion.ref ? Leave(groupId, client.userId, client.authId, randomId)).mapTo[SeqStateDate]

  def kickUser(groupId: Int, kickedUserId: Int, randomId: Long)(implicit client: AuthorizedClientData): Future[SeqStateDate] =
    (processorRegion.ref ? Kick(groupId, kickedUserId, client.userId, client.authId, randomId)).mapTo[SeqStateDate]

  def joinGroup(groupId: Int, joiningUserId: Int, joiningUserAuthId: Long, invitingUserId: Int): Future[(SeqStateDate, Vector[Int], Long)] =
    (processorRegion.ref ? Join(groupId, joiningUserId, joiningUserAuthId, invitingUserId)).mapTo[(SeqStateDate, Vector[Int], Long)]

  def joinAfterFirstRead(groupId: Int, joiningUserId: Int, joiningUserAuthId: Long): Future[Unit] =
    (processorRegion.ref ? JoinAfterFirstRead(groupId, joiningUserId, joiningUserAuthId)) map (_ ⇒ ())

  def inviteToGroup(groupId: Int, inviteeUserId: Int, randomId: Long)(implicit client: AuthorizedClientData): Future[SeqStateDate] =
    (processorRegion.ref ? Invite(groupId, inviteeUserId, client.userId, client.authId, randomId)).mapTo[SeqStateDate]

  def updateAvatar(groupId: Int, clientUserId: Int, clientAuthId: Long, avatarOpt: Option[Avatar], randomId: Long): Future[UpdateAvatarAck] =
    (processorRegion.ref ? UpdateAvatar(groupId, clientUserId, clientAuthId, avatarOpt, randomId)).mapTo[UpdateAvatarAck]

  def updateTitle(groupId: Int, clientUserId: Int, clientAuthId: Long, title: String, randomId: Long): Future[SeqStateDate] =
    (processorRegion.ref ? UpdateTitle(groupId, clientUserId, clientAuthId, title, randomId)).mapTo[SeqStateDate]

  def updateTopic(groupId: Int, clientUserId: Int, clientAuthId: Long, topic: Option[String], randomId: Long): Future[SeqStateDate] =
    (processorRegion.ref ? ChangeTopic(groupId, clientUserId, clientAuthId, topic, randomId)).mapTo[SeqStateDate]

  def updateAbout(groupId: Int, clientUserId: Int, clientAuthId: Long, about: Option[String], randomId: Long): Future[SeqStateDate] =
    (processorRegion.ref ? ChangeAbout(groupId, clientUserId, clientAuthId, about, randomId)).mapTo[SeqStateDate]

  def makeUserAdmin(groupId: Int, clientUserId: Int, clientAuthId: Long, candidateId: Int): Future[(Vector[ApiMember], SeqState)] =
    (processorRegion.ref ? MakeUserAdmin(groupId, clientUserId, clientAuthId, candidateId)).mapTo[(Vector[ApiMember], SeqState)]

  def revokeIntegrationToken(groupId: Int, clientUserId: Int): Future[String] =
    (processorRegion.ref ? RevokeIntegrationToken(groupId, clientUserId)).mapTo[RevokeIntegrationTokenAck] map (_.token)
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

  def isPublic(groupId: Int): Future[Boolean] =
    (viewRegion.ref ? IsPublic(groupId)).mapTo[IsPublicResponse] map (_.isPublic)

  def isHistoryShared(groupId: Int): Future[Boolean] =
    (viewRegion.ref ? IsHistoryShared(groupId)).mapTo[IsHistorySharedResponse] map (_.isHistoryShared)

  def checkAccessHash(groupId: Int, hash: Long): Future[Boolean] =
    (viewRegion.ref ? CheckAccessHash(groupId, hash)).mapTo[CheckAccessHashResponse] map (_.isCorrect)

  def getMemberIds(groupId: Int): Future[(Seq[Int], Seq[Int], Option[Int])] =
    (viewRegion.ref ? GetMembers(groupId)).mapTo[GetMembersResponse] map (r ⇒ (r.memberIds, r.invitedUserIds, r.botId))

  def getAccessHash(groupId: Int): Future[Long] =
    (viewRegion.ref ? GetAccessHash(groupId)).mapTo[GetAccessHashResponse] map (_.accessHash)
}