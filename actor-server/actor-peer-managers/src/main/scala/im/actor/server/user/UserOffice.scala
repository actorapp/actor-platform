package im.actor.server.user

import scala.concurrent.{ ExecutionContext, Future }

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import org.joda.time.DateTime

import im.actor.api.rpc.messaging.{ Message ⇒ ApiMessage }
import im.actor.server.commons.serialization.ActorSerializer
import im.actor.server.office.user
import im.actor.server.sequence.{ SeqState, SeqStateDate }

object UserOffice {

  import user._
  import UserEnvelope._

  ActorSerializer.register(3000, classOf[UserEnvelope])
  ActorSerializer.register(3001, classOf[UserEnvelope.NewAuth])
  ActorSerializer.register(3002, classOf[UserEnvelope.NewAuthResponse])
  ActorSerializer.register(3003, classOf[UserEnvelope.SendMessage])
  ActorSerializer.register(3004, classOf[UserEnvelope.MessageReceived])
  ActorSerializer.register(3005, classOf[UserEnvelope.BroadcastUpdate])
  ActorSerializer.register(3006, classOf[UserEnvelope.BroadcastUpdateResponse])

  ActorSerializer.register(4001, classOf[UserEvents.AuthAdded])

  def persistenceIdFor(userId: Int): String = s"user_${userId}"

  def auth(userId: Int, authId: Long)(
    implicit
    userOfficeRegion: UserOfficeRegion,
    timeout:          Timeout,
    ec:               ExecutionContext

  ): Future[Unit] = (userOfficeRegion.ref ? UserEnvelope(userId).withNewAuth(NewAuth(authId))) map (_ ⇒ ())

  def sendMessage(userId: Int, senderUserId: Int, senderAuthId: Long, accessHash: Long, randomId: Long, message: ApiMessage)(
    implicit
    peerManagerRegion: UserOfficeRegion,
    timeout:           Timeout,
    ec:                ExecutionContext
  ): Future[SeqStateDate] = {
    (peerManagerRegion.ref ? UserEnvelope(userId).withSendMessage(SendMessage(senderUserId, senderAuthId, accessHash, randomId, message))).mapTo[SeqStateDate]
  }

  def deliverGroupMessage(userId: Int, groupId: Int, senderUserId: Int, randomId: Long, date: DateTime, message: ApiMessage, isFat: Boolean)(
    implicit
    region:  UserOfficeRegion,
    timeout: Timeout,
    ec:      ExecutionContext
  ): Unit =
    region.ref ! UserEnvelope(userId).withDeliverGroupMessage(DeliverGroupMessage(groupId, senderUserId, randomId, date, message, isFat))

  def deliverOwnGroupMessage(userId: Int, groupId: Int, senderAuthId: Long, randomId: Long, date: DateTime, message: ApiMessage, isFat: Boolean)(
    implicit
    region:  UserOfficeRegion,
    timeout: Timeout,
    ec:      ExecutionContext
  ): Future[SeqState] =
    (region.ref ? UserEnvelope(userId).withDeliverOwnGroupMessage(DeliverOwnGroupMessage(groupId, senderAuthId, randomId, date, message, isFat))).mapTo[SeqState]

  def messageReceived(userId: Int, receiverUserId: Int, receiverAuthId: Long, date: Long, receivedDate: Long)(implicit peerManagerRegion: UserOfficeRegion): Unit = {
    peerManagerRegion.ref ! UserEnvelope(userId).withMessageReceived(MessageReceived(receiverUserId, receiverAuthId, date, receivedDate))
  }

  def messageRead(userId: Int, readerUserId: Int, readerAuthId: Long, date: Long, readDate: Long)(implicit peerManagerRegion: UserOfficeRegion): Unit = {
    peerManagerRegion.ref ! UserEnvelope(userId).withMessageRead(MessageRead(readerUserId, readerAuthId, date, readDate))
  }
}
