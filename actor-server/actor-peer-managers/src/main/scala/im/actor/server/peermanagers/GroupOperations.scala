package im.actor.server.peermanagers

import scala.concurrent.{ Future, ExecutionContext }
import scala.util.control.NoStackTrace

import akka.pattern.ask
import akka.util.Timeout
import org.joda.time.DateTime

import im.actor.api.rpc.messaging.{ Message ⇒ ApiMessage }
import im.actor.api.rpc.AuthorizedClientData
import im.actor.server.models
import im.actor.server.peermanagers.PeerManager._
import im.actor.server.push.SeqUpdatesManager._

trait GroupOperations {

  case object UserAlreadyJoined extends Exception with NoStackTrace
  case object UserAlreadyInvited extends Exception with NoStackTrace

  def sendMessage(groupId: Int, senderUserId: Int, senderAuthId: Long, randomId: Long, date: DateTime, message: ApiMessage, isFat: Boolean = false)(
    implicit
    peerManagerRegion: GroupPeerManagerRegion,
    timeout:           Timeout,
    ec:                ExecutionContext
  ): Future[SequenceState] =
    (peerManagerRegion.ref ? Envelope(groupId, SendMessage(senderUserId, senderAuthId, randomId, date, message, isFat)))
      .mapTo[SequenceState]

  def leaveGroup(groupId: Int, randomId: Long)(
    implicit
    timeout:           Timeout,
    peerManagerRegion: GroupPeerManagerRegion,
    ec:                ExecutionContext,
    client:            AuthorizedClientData
  ): Future[SequenceStateDate] =
    (peerManagerRegion.ref ? Envelope(groupId, LeaveGroup(client, randomId))).mapTo[SequenceStateDate]

  def kickUser(groupId: Int, kickedUserId: Int, randomId: Long)(
    implicit
    timeout:           Timeout,
    peerManagerRegion: GroupPeerManagerRegion,
    ec:                ExecutionContext,
    client:            AuthorizedClientData
  ): Future[SequenceStateDate] =
    (peerManagerRegion.ref ? Envelope(groupId, KickUser(kickedUserId, client, randomId))).mapTo[SequenceStateDate]

  def joinGroup(groupId: Int, joiningUserId: Int, joiningUserAuthId: Long, invitingUserId: Int)(
    implicit
    timeout:           Timeout,
    peerManagerRegion: GroupPeerManagerRegion,
    ec:                ExecutionContext
  ): Future[Option[(SequenceState, Vector[Int], Long, Long)]] =
    (peerManagerRegion.ref ? Envelope(groupId, JoinGroup(joiningUserId, joiningUserAuthId, invitingUserId)))
      .mapTo[(SequenceState, Vector[Int], Long, Long)].map(Some(_)).recover { case UserAlreadyJoined ⇒ None }

  //TODO: remove group from here
  def inviteToGroup(group: models.FullGroup, inviteeUserId: Int, randomId: Long)(
    implicit
    timeout:           Timeout,
    peerManagerRegion: GroupPeerManagerRegion,
    ec:                ExecutionContext,
    client:            AuthorizedClientData
  ): Future[Option[SequenceStateDate]] =
    (peerManagerRegion.ref ? Envelope(group.id, InviteToGroup(group, inviteeUserId, client, randomId))).mapTo[SequenceStateDate]
      .map(Some(_)).recover { case UserAlreadyInvited ⇒ None }

  def messageReceived(groupId: Int, receiverUserId: Int, receiverAuthId: Long, date: Long, receivedDate: Long)(implicit peerManagerRegion: GroupPeerManagerRegion): Unit = {
    peerManagerRegion.ref ! Envelope(groupId, MessageReceived(receiverUserId, receiverAuthId, date, receivedDate))
  }

  def messageRead(groupId: Int, readerUserId: Int, readerAuthId: Long, date: Long, readDate: Long)(implicit peerManagerRegion: GroupPeerManagerRegion): Unit = {
    peerManagerRegion.ref ! Envelope(groupId, MessageRead(readerUserId, readerAuthId, date, readDate))
  }

}
