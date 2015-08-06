package im.actor.server.group

import scala.concurrent.{ ExecutionContext, Future }

import akka.pattern.ask
import akka.util.Timeout

import im.actor.api.rpc.AuthorizedClientData
import im.actor.api.rpc.groups.{ Group ⇒ ApiGroup, Member ⇒ ApiMember }
import im.actor.api.rpc.messaging.{ Message ⇒ ApiMessage }
import im.actor.server.file.Avatar
import im.actor.server.sequence.{ SeqState, SeqStateDate }

trait GroupOperations {

  import GroupCommands._
  import GroupQueries._

  def create(groupId: Int, title: String, randomId: Long, userIds: Set[Int])(
    implicit
    peerManagerRegion: GroupProcessorRegion,
    timeout:           Timeout,
    ec:                ExecutionContext,
    client:            AuthorizedClientData
  ): Future[CreateAck] = create(groupId, client.userId, client.authId, title, randomId, userIds)

  def create(groupId: Int, clientUserId: Int, clientAuthId: Long, title: String, randomId: Long, userIds: Set[Int])(
    implicit
    peerManagerRegion: GroupProcessorRegion,
    timeout:           Timeout,
    ec:                ExecutionContext
  ): Future[CreateAck] =
    (peerManagerRegion.ref ? Create(groupId, clientUserId, clientAuthId, title, randomId, userIds.toSeq)).mapTo[CreateAck]

  def makePublic(groupId: Int, description: String)(
    implicit
    region:  GroupProcessorRegion,
    timeout: Timeout,
    ec:      ExecutionContext
  ): Future[MakePublicAck] =
    (region.ref ? MakePublic(groupId, Some(description))).mapTo[MakePublicAck]

  def sendMessage(groupId: Int, senderUserId: Int, senderAuthId: Long, accessHash: Long, randomId: Long, message: ApiMessage, isFat: Boolean = false)(
    implicit
    peerManagerRegion: GroupProcessorRegion,
    timeout:           Timeout,
    ec:                ExecutionContext
  ): Future[SeqStateDate] =
    (peerManagerRegion.ref ? SendMessage(groupId, senderUserId, senderAuthId, accessHash, randomId, message, isFat))
      .mapTo[SeqStateDate]

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

  def inviteToGroup(groupId: Int, inviteeUserId: Int, randomId: Long)(
    implicit
    timeout:           Timeout,
    peerManagerRegion: GroupProcessorRegion,
    ec:                ExecutionContext,
    client:            AuthorizedClientData
  ): Future[SeqStateDate] =
    (peerManagerRegion.ref ? Invite(groupId, inviteeUserId, client.userId, client.authId, randomId)).mapTo[SeqStateDate]

  def messageReceived(groupId: Int, receiverUserId: Int, receiverAuthId: Long, date: Long, receivedDate: Long)(
    implicit
    timeout: Timeout,
    region:  GroupProcessorRegion,
    ec:      ExecutionContext
  ): Future[Unit] = {
    (region.ref ? MessageReceived(groupId, receiverUserId, receiverAuthId, date, receivedDate)).mapTo[MessageReceivedAck] map (_ ⇒ ())
  }

  def messageRead(groupId: Int, readerUserId: Int, readerAuthId: Long, date: Long, readDate: Long)(
    implicit
    timeout: Timeout,
    region:  GroupProcessorRegion,
    ec:      ExecutionContext
  ): Future[Unit] = {
    (region.ref ? MessageRead(groupId, readerUserId, readerAuthId, date, readDate)).mapTo[MessageReadAck] map (_ ⇒ ())
  }

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

  def getIntegrationToken(groupId: Int, clientUserId: Int)(
    implicit
    region:  GroupViewRegion,
    timeout: Timeout,
    ec:      ExecutionContext
  ): Future[Option[String]] = (region.ref ? GetIntegrationToken(groupId, clientUserId)).mapTo[GetIntegrationTokenResponse] map (_.token)

  def revokeIntegrationToken(groupId: Int, clientUserId: Int)(
    implicit
    region:  GroupProcessorRegion,
    timeout: Timeout,
    ec:      ExecutionContext
  ): Future[String] = (region.ref ? RevokeIntegrationToken(groupId, clientUserId)).mapTo[RevokeIntegrationTokenAck] map (_.token)

  def getApiStruct(groupId: Int, clientUserId: Int)(
    implicit
    region:  GroupViewRegion,
    timeout: Timeout,
    ec:      ExecutionContext
  ): Future[ApiGroup] = (region.ref ? GetApiStruct(groupId, clientUserId)).mapTo[GetApiStructResponse] map (_.struct)
}
