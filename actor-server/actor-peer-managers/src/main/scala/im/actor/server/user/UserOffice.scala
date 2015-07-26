package im.actor.server.user

import im.actor.api.rpc.peers.Peer

import scala.concurrent.{ ExecutionContext, Future }

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import org.joda.time.DateTime

import im.actor.api.rpc.messaging.{ Message ⇒ ApiMessage }
import im.actor.server.office.user
import im.actor.server.sequence.{ SeqState, SeqStateDate }

import scala.util.control.NoStackTrace

object UserOffice {

  import user._
  import UserEnvelope._

  case object InvalidAccessHash extends Exception with NoStackTrace

  case object FailedToFetchInfo

  def persistenceIdFor(userId: Int): String = s"user_${userId}"

  def auth(userId: Int, authId: Long)(
    implicit
    userOfficeRegion: UserOfficeRegion,
    timeout:          Timeout,
    ec:               ExecutionContext

  ): Future[Unit] = (userOfficeRegion.ref ? UserEnvelope(userId).withNewAuth(NewAuth(authId))) map (_ ⇒ ())

  def removeAuth(userId: Int, authId: Long)(
    implicit
    userOfficeRegion: UserOfficeRegion,
    timeout:          Timeout,
    ec:               ExecutionContext

  ): Future[Unit] = (userOfficeRegion.ref ? UserEnvelope(userId).withRemoveAuth(RemoveAuth(authId))) map (_ ⇒ ())

  def sendMessage(userId: Int, senderUserId: Int, senderAuthId: Long, accessHash: Long, randomId: Long, message: ApiMessage)(
    implicit
    peerManagerRegion: UserOfficeRegion,
    timeout:           Timeout,
    ec:                ExecutionContext
  ): Future[SeqStateDate] = {
    (peerManagerRegion.ref ? UserEnvelope(userId).withSendMessage(SendMessage(senderUserId, senderAuthId, accessHash, randomId, message))).mapTo[SeqStateDate]
  }

  def deliverMessage(userId: Int, peer: Peer, senderUserId: Int, randomId: Long, date: DateTime, message: ApiMessage, isFat: Boolean)(
    implicit
    region:  UserOfficeRegion,
    timeout: Timeout,
    ec:      ExecutionContext
  ): Unit =
    region.ref ! UserEnvelope(userId).withDeliverMessage(DeliverMessage(peer, senderUserId, randomId, date, message, isFat))

  def deliverOwnMessage(userId: Int, peer: Peer, senderAuthId: Long, randomId: Long, date: DateTime, message: ApiMessage, isFat: Boolean)(
    implicit
    region:  UserOfficeRegion,
    timeout: Timeout,
    ec:      ExecutionContext
  ): Future[SeqState] =
    (region.ref ? UserEnvelope(userId).withDeliverOwnMessage(DeliverOwnMessage(peer, senderAuthId, randomId, date, message, isFat))).mapTo[SeqState]

  def messageReceived(userId: Int, receiverUserId: Int, receiverAuthId: Long, date: Long, receivedDate: Long)(implicit peerManagerRegion: UserOfficeRegion): Unit = {
    peerManagerRegion.ref ! UserEnvelope(userId).withMessageReceived(MessageReceived(receiverUserId, receiverAuthId, date, receivedDate))
  }

  def messageRead(userId: Int, readerUserId: Int, readerAuthId: Long, date: Long, readDate: Long)(implicit peerManagerRegion: UserOfficeRegion): Unit = {
    peerManagerRegion.ref ! UserEnvelope(userId).withMessageRead(MessageRead(readerUserId, readerAuthId, date, readDate))
  }
}
