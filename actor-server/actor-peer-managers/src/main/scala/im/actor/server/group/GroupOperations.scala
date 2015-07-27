package im.actor.server.group

import scala.concurrent.{ ExecutionContext, Future }

import akka.pattern.ask
import akka.util.Timeout

import im.actor.api.rpc.AuthorizedClientData
import im.actor.api.rpc.files.{ FileLocation ⇒ ApiFileLocation }
import im.actor.api.rpc.messaging.{ Message ⇒ ApiMessage }
import im.actor.server.file.FileLocation
import im.actor.server.sequence.SeqStateDate

trait GroupOperations {

  import GroupCommands._

  def create(groupId: Int, title: String, randomId: Long, userIds: Set[Int])(
    implicit
    peerManagerRegion: GroupOfficeRegion,
    timeout:           Timeout,
    ec:                ExecutionContext,
    client:            AuthorizedClientData
  ): Future[CreateResponse] =
    (peerManagerRegion.ref ? Create(groupId, client.userId, client.authId, title, randomId, userIds.toSeq))
      .mapTo[CreateResponse]

  def sendMessage(groupId: Int, senderUserId: Int, senderAuthId: Long, accessHash: Long, randomId: Long, message: ApiMessage, isFat: Boolean = false)(
    implicit
    peerManagerRegion: GroupOfficeRegion,
    timeout:           Timeout,
    ec:                ExecutionContext
  ): Future[SeqStateDate] =
    (peerManagerRegion.ref ? SendMessage(groupId, senderUserId, senderAuthId, accessHash, randomId, message, isFat))
      .mapTo[SeqStateDate]

  def leaveGroup(groupId: Int, randomId: Long)(
    implicit
    timeout:           Timeout,
    peerManagerRegion: GroupOfficeRegion,
    ec:                ExecutionContext,
    client:            AuthorizedClientData
  ): Future[SeqStateDate] =
    (peerManagerRegion.ref ? Leave(groupId, client.userId, client.authId, randomId)).mapTo[SeqStateDate]

  def kickUser(groupId: Int, kickedUserId: Int, randomId: Long)(
    implicit
    timeout:           Timeout,
    peerManagerRegion: GroupOfficeRegion,
    ec:                ExecutionContext,
    client:            AuthorizedClientData
  ): Future[SeqStateDate] =
    (peerManagerRegion.ref ? Kick(groupId, kickedUserId, client.userId, client.authId, randomId)).mapTo[SeqStateDate]

  def joinGroup(groupId: Int, joiningUserId: Int, joiningUserAuthId: Long, invitingUserId: Int)(
    implicit
    timeout:           Timeout,
    peerManagerRegion: GroupOfficeRegion,
    ec:                ExecutionContext
  ): Future[(SeqStateDate, Vector[Int], Long)] =
    (peerManagerRegion.ref ? Join(groupId, joiningUserId, joiningUserAuthId, invitingUserId)).mapTo[(SeqStateDate, Vector[Int], Long)]

  def inviteToGroup(groupId: Int, inviteeUserId: Int, randomId: Long)(
    implicit
    timeout:           Timeout,
    peerManagerRegion: GroupOfficeRegion,
    ec:                ExecutionContext,
    client:            AuthorizedClientData
  ): Future[SeqStateDate] =
    (peerManagerRegion.ref ? Invite(groupId, inviteeUserId, client.userId, client.authId, randomId)).mapTo[SeqStateDate]

  def messageReceived(groupId: Int, receiverUserId: Int, receiverAuthId: Long, date: Long, receivedDate: Long)(implicit peerManagerRegion: GroupOfficeRegion): Unit = {
    peerManagerRegion.ref ! MessageReceived(groupId, receiverUserId, receiverAuthId, date, receivedDate)
  }

  def messageRead(groupId: Int, readerUserId: Int, readerAuthId: Long, date: Long, readDate: Long)(implicit peerManagerRegion: GroupOfficeRegion): Unit = {
    peerManagerRegion.ref ! MessageRead(groupId, readerUserId, readerAuthId, date, readDate)
  }

  def updateAvatar(groupId: Int, clientUserId: Int, clientAuthId: Long, fileLocationOpt: Option[ApiFileLocation], randomId: Long)(
    implicit
    region:  GroupOfficeRegion,
    timeout: Timeout,
    ec:      ExecutionContext
  ): Future[UpdateAvatarResponse] = {
    val fl = fileLocationOpt map (f ⇒ FileLocation(f.fileId, f.accessHash))

    (region.ref ? UpdateAvatar(groupId, clientUserId, clientAuthId, fl, randomId)).mapTo[UpdateAvatarResponse]
  }
}
