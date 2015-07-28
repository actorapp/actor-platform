package im.actor.server.user

import im.actor.api.rpc.users.Sex

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.control.NoStackTrace

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import org.joda.time.DateTime

import im.actor.api.rpc.messaging.{ Message ⇒ ApiMessage }
import im.actor.api.rpc.peers.Peer
import im.actor.server.sequence.{ SeqState, SeqStateDate }

object UserOffice {

  import UserCommands._

  case object InvalidAccessHash extends Exception with NoStackTrace

  case object FailedToFetchInfo

  def persistenceIdFor(userId: Int): String = s"user_${userId}"

  def create(userId: Int, accessSalt: String, name: String, countryCode: String, sex: Sex.Sex)(
    implicit
    userOfficeRegion: UserOfficeRegion,
    timeout:          Timeout,
    ec:               ExecutionContext

  ): Future[CreateAck] = {
    (userOfficeRegion.ref ? Create(userId, accessSalt, name, countryCode, sex)).mapTo[CreateAck]
  }

  def addPhone(userId: Int, phone: Long)(
    implicit
    userOfficeRegion: UserOfficeRegion,
    timeout:          Timeout,
    ec:               ExecutionContext

  ): Future[AddPhoneAck] = {
    (userOfficeRegion.ref ? AddPhone(userId, phone)).mapTo[AddPhoneAck]
  }

  def addEmail(userId: Int, email: String)(
    implicit
    userOfficeRegion: UserOfficeRegion,
    timeout:          Timeout,
    ec:               ExecutionContext

  ): Future[AddEmailAck] = {
    (userOfficeRegion.ref ? AddEmail(userId, email)).mapTo[AddEmailAck]
  }

  def delete(userId: Int)(
    implicit
    userOfficeRegion: UserOfficeRegion,
    timeout:          Timeout,
    ec:               ExecutionContext

  ): Future[DeleteAck] = {
    (userOfficeRegion.ref ? Delete(userId)).mapTo[DeleteAck]
  }

  def changeCountryCode(userId: Int, countryCode: String)(
    implicit
    userOfficeRegion: UserOfficeRegion,
    timeout:          Timeout,
    ec:               ExecutionContext

  ): Future[Unit] = {
    userOfficeRegion.ref ? ChangeCountryCode(userId, countryCode) map (_ ⇒ ())
  }

  def changeName(userId: Int, name: String)(
    implicit
    userOfficeRegion: UserOfficeRegion,
    timeout:          Timeout,
    ec:               ExecutionContext

  ): Future[ChangeNameAck] = {
    (userOfficeRegion.ref ? ChangeName(userId, name)).mapTo[ChangeNameAck]
  }

  def auth(userId: Int, authId: Long)(
    implicit
    userOfficeRegion: UserOfficeRegion,
    timeout:          Timeout,
    ec:               ExecutionContext

  ): Future[NewAuthAck] = (userOfficeRegion.ref ? NewAuth(userId, authId)).mapTo[NewAuthAck]

  def removeAuth(userId: Int, authId: Long)(
    implicit
    userOfficeRegion: UserOfficeRegion,
    timeout:          Timeout,
    ec:               ExecutionContext

  ): Future[RemoveAuthAck] = (userOfficeRegion.ref ? RemoveAuth(userId, authId)).mapTo[RemoveAuthAck]

  def sendMessage(userId: Int, senderUserId: Int, senderAuthId: Long, accessHash: Long, randomId: Long, message: ApiMessage)(
    implicit
    peerManagerRegion: UserOfficeRegion,
    timeout:           Timeout,
    ec:                ExecutionContext
  ): Future[SeqStateDate] = {
    (peerManagerRegion.ref ? SendMessage(userId, senderUserId, senderAuthId, accessHash, randomId, message)).mapTo[SeqStateDate]
  }

  def deliverMessage(userId: Int, peer: Peer, senderUserId: Int, randomId: Long, date: DateTime, message: ApiMessage, isFat: Boolean)(
    implicit
    region:  UserOfficeRegion,
    timeout: Timeout,
    ec:      ExecutionContext
  ): Unit =
    region.ref ! DeliverMessage(userId, peer, senderUserId, randomId, date, message, isFat)

  def deliverOwnMessage(userId: Int, peer: Peer, senderAuthId: Long, randomId: Long, date: DateTime, message: ApiMessage, isFat: Boolean)(
    implicit
    region:  UserOfficeRegion,
    timeout: Timeout,
    ec:      ExecutionContext
  ): Future[SeqState] =
    (region.ref ? DeliverOwnMessage(userId, peer, senderAuthId, randomId, date, message, isFat)).mapTo[SeqState]

  def messageReceived(userId: Int, receiverUserId: Int, receiverAuthId: Long, date: Long, receivedDate: Long)(implicit peerManagerRegion: UserOfficeRegion): Unit = {
    peerManagerRegion.ref ! MessageReceived(userId, receiverUserId, receiverAuthId, date, receivedDate)
  }

  def messageRead(userId: Int, readerUserId: Int, readerAuthId: Long, date: Long, readDate: Long)(implicit peerManagerRegion: UserOfficeRegion): Unit = {
    peerManagerRegion.ref ! MessageRead(userId, readerUserId, readerAuthId, date, readDate)
  }
}
