package im.actor.server.group

import scala.concurrent.{ ExecutionContext, Future }

import akka.pattern.ask
import akka.util.Timeout
import org.joda.time.DateTime

import im.actor.api.rpc.AuthorizedClientData
import im.actor.api.rpc.messaging.{ Message ⇒ ApiMessage }
import im.actor.server.models
import im.actor.server.office.group.GroupEnvelope
import im.actor.server.push.SeqUpdatesManager._
import im.actor.server.sequence.SeqStateDate

trait GroupOperations {

  import GroupEnvelope._
  import GroupErrors._

  def create(groupId: Int, title: String, randomId: Long, userIds: Set[Int])(
    implicit
    peerManagerRegion: GroupOfficeRegion,
    timeout:           Timeout,
    ec:                ExecutionContext,
    client:            AuthorizedClientData
  ): Future[CreateResponse] =
    (peerManagerRegion.ref ? GroupEnvelope(groupId).withCreate(Create(client.userId, client.authId, title, randomId, userIds.toSeq)))
      .mapTo[CreateResponse]

  def sendMessage(groupId: Int, senderUserId: Int, senderAuthId: Long, accessHash: Long, randomId: Long, message: ApiMessage, isFat: Boolean = false)(
    implicit
    peerManagerRegion: GroupOfficeRegion,
    timeout:           Timeout,
    ec:                ExecutionContext
  ): Future[SeqStateDate] =
    (peerManagerRegion.ref ? GroupEnvelope(groupId).withSendMessage(SendMessage(senderUserId, senderAuthId, accessHash, randomId, message, isFat)))
      .mapTo[SeqStateDate]

  def leaveGroup(groupId: Int, randomId: Long)(
    implicit
    timeout:           Timeout,
    peerManagerRegion: GroupOfficeRegion,
    ec:                ExecutionContext,
    client:            AuthorizedClientData
  ): Future[SequenceStateDate] =
    (peerManagerRegion.ref ? GroupEnvelope(groupId).withLeave(Leave(client.userId, client.authId, randomId))).mapTo[SequenceStateDate]

  def kickUser(groupId: Int, kickedUserId: Int, randomId: Long)(
    implicit
    timeout:           Timeout,
    peerManagerRegion: GroupOfficeRegion,
    ec:                ExecutionContext,
    client:            AuthorizedClientData
  ): Future[SequenceStateDate] =
    (peerManagerRegion.ref ? GroupEnvelope(groupId).withKick(Kick(kickedUserId, client.userId, client.authId, randomId))).mapTo[SequenceStateDate]

  def joinGroup(groupId: Int, joiningUserId: Int, joiningUserAuthId: Long, invitingUserId: Int)(
    implicit
    timeout:           Timeout,
    peerManagerRegion: GroupOfficeRegion,
    ec:                ExecutionContext
  ): Future[Option[(SeqStateDate, Vector[Int], Long)]] =
    (peerManagerRegion.ref ? GroupEnvelope(groupId).withJoin(Join(joiningUserId, joiningUserAuthId, invitingUserId)))
      .mapTo[(SeqStateDate, Vector[Int], Long)].map(Some(_)).recover { case UserAlreadyJoined ⇒ None }

  //TODO: remove group from here
  def inviteToGroup(group: models.FullGroup, inviteeUserId: Int, randomId: Long)(
    implicit
    timeout:           Timeout,
    peerManagerRegion: GroupOfficeRegion,
    ec:                ExecutionContext,
    client:            AuthorizedClientData
  ): Future[Option[SequenceStateDate]] =
    (peerManagerRegion.ref ? GroupEnvelope(group.id).withInvite(Invite(inviteeUserId, client.userId, client.authId, randomId))).mapTo[SequenceStateDate]
      .map(Some(_)).recover { case UserAlreadyInvited ⇒ None }

  def messageReceived(groupId: Int, receiverUserId: Int, receiverAuthId: Long, date: Long, receivedDate: Long)(implicit peerManagerRegion: GroupOfficeRegion): Unit = {
    peerManagerRegion.ref ! GroupEnvelope(groupId).withMessageReceived(MessageReceived(receiverUserId, receiverAuthId, date, receivedDate))
  }

  def messageRead(groupId: Int, readerUserId: Int, readerAuthId: Long, date: Long, readDate: Long)(implicit peerManagerRegion: GroupOfficeRegion): Unit = {
    peerManagerRegion.ref ! GroupEnvelope(groupId).withMessageRead(MessageRead(readerUserId, readerAuthId, date, readDate))
  }

}
