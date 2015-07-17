package im.actor.server.peermanagers

import scala.concurrent.{ ExecutionContext, Future }

import akka.actor.{ ActorRef, ActorSystem, Props, Status }
import akka.contrib.pattern.{ ClusterSharding, ShardRegion }
import akka.pattern.{ ask, pipe }
import akka.util.Timeout
import org.joda.time.DateTime
import slick.driver.PostgresDriver.api._

import im.actor.server.models
import im.actor.api.rpc.messaging.{ Message ⇒ ApiMessage, _ }
import im.actor.api.rpc.peers.{ Peer, PeerType }
import im.actor.server.push.{ SeqUpdatesManager, SeqUpdatesManagerRegion }
import im.actor.server.social.{ SocialManager, SocialManagerRegion }
import im.actor.server.util.{ HistoryUtils, UserUtils }
import im.actor.server.entity.user._

case class UserEntityRegion(ref: ActorRef)

object UserEntity {
  private val idExtractor: ShardRegion.IdExtractor = {
    case Envelope(userId, payload) ⇒ (userId.toString, payload)
  }

  private val shardResolver: ShardRegion.ShardResolver = msg ⇒ msg match {
    case Envelope(userId, _) ⇒ (userId % 100).toString // TODO: configurable
  }

  private def startRegion(props: Option[Props])(implicit system: ActorSystem): UserEntityRegion =
    UserEntityRegion(ClusterSharding(system).start(
      typeName = "UserEntity",
      entryProps = props,
      idExtractor = idExtractor,
      shardResolver = shardResolver
    ))

  def startRegion()(
    implicit
    system:              ActorSystem,
    db:                  Database,
    seqUpdManagerRegion: SeqUpdatesManagerRegion,
    socialManagerRegion: SocialManagerRegion
  ): UserEntityRegion =
    startRegion(Some(props))

  def startRegionProxy()(implicit system: ActorSystem): UserEntityRegion =
    startRegion(None)

  def props(
    implicit
    db:                  Database,
    seqUpdManagerRegion: SeqUpdatesManagerRegion,
    socialManagerRegion: SocialManagerRegion
  ): Props =
    Props(classOf[UserEntity], db, seqUpdManagerRegion, socialManagerRegion)

  def sendMessage(userId: Int, senderUserId: Int, senderAuthId: Long, randomId: Long, date: DateTime, message: ApiMessage)(
    implicit
    peerManagerRegion: UserEntityRegion,
    timeout:           Timeout,
    ec:                ExecutionContext
  ): Future[SeqUpdatesManager.SequenceState] = {
    (peerManagerRegion.ref ? Envelope(userId).withSendMessage(SendMessage(senderUserId, senderAuthId, randomId, date.getMillis, message))).mapTo[SeqUpdatesManager.SequenceState]
  }

  def messageReceived(userId: Int, receiverUserId: Int, receiverAuthId: Long, date: Long, receivedDate: Long)(implicit peerManagerRegion: UserEntityRegion): Unit = {
    peerManagerRegion.ref ! Envelope(userId).withMessageReceived(MessageReceived(receiverUserId, receiverAuthId, date, receivedDate))
  }

  def messageRead(userId: Int, readerUserId: Int, readerAuthId: Long, date: Long, readDate: Long)(implicit peerManagerRegion: UserEntityRegion): Unit = {
    peerManagerRegion.ref ! Envelope(userId).withMessageRead(MessageRead(readerUserId, readerAuthId, date, readDate))
  }
}

class UserEntity(
  implicit
  db:                  Database,
  seqUpdManagerRegion: SeqUpdatesManagerRegion,
  socialManagerRegion: SocialManagerRegion
) extends PeerManager {
  import HistoryUtils._
  import SeqUpdatesManager._
  import SocialManager._
  import UserUtils._
  import Envelope.Payload

  implicit private val ec: ExecutionContext = context.dispatcher

  private val userId = self.path.name.toInt

  private[this] var lastReceivedDate: Option[Long] = None
  private[this] var lastReadDate: Option[Long] = None

  def receive = {
    case Payload.SendMessage(SendMessage(senderUserId, senderAuthId, randomId, date, message, _)) ⇒
      val replyTo = sender()

      val peerUpdate = UpdateMessage(
        peer = privatePeerStruct(senderUserId),
        senderUserId = senderUserId,
        date = date,
        randomId = randomId,
        message = message
      )

      val senderUpdate = UpdateMessage(
        peer = privatePeerStruct(userId),
        senderUserId = senderUserId,
        date = date,
        randomId = randomId,
        message = message
      )

      val clientUpdate = UpdateMessageSent(privatePeerStruct(userId), randomId, date)

      db.run(for {

        clientUser ← getUserUnsafe(senderUserId)
        pushText ← getPushText(message, clientUser, userId)

        _ ← broadcastUserUpdate(userId, peerUpdate, Some(pushText))

        _ ← notifyUserUpdate(senderUserId, senderAuthId, senderUpdate, None)
        seqstate ← persistAndPushUpdate(senderAuthId, clientUpdate, None)
      } yield {
        recordRelation(senderUserId, userId)
        db.run(writeHistoryMessage(models.Peer.privat(senderUserId), models.Peer.privat(userId), new DateTime(date), randomId, message.header, message.toByteArray))
        seqstate
      }) pipeTo replyTo onFailure {
        case e ⇒
          log.error(e, "Failed to send message")
          sender() ! Status.Failure(e)
      }
    case Payload.MessageReceived(MessageReceived(receiverUserId, _, date, receivedDate)) ⇒
      if (!lastReceivedDate.exists(_ > date)) {
        lastReceivedDate = Some(date)
        val update = UpdateMessageReceived(Peer(PeerType.Private, receiverUserId), date, receivedDate)

        db.run(for {
          _ ← broadcastUserUpdate(userId, update, None)
        } yield {
          // TODO: report errors
          db.run(markMessagesReceived(models.Peer.privat(receiverUserId), models.Peer.privat(userId), new DateTime(date)))
        }) onFailure {
          case e ⇒
            log.error(e, "Failed to mark messages received")
        }
      }
    case Payload.MessageRead(MessageRead(readerUserId, _, date, readDate)) ⇒
      if (!lastReadDate.exists(_ > date)) {
        lastReadDate = Some(date)
        val update = UpdateMessageRead(Peer(PeerType.Private, readerUserId), date, readDate)
        val readerUpdate = UpdateMessageReadByMe(Peer(PeerType.Private, userId), date)

        db.run(for {
          _ ← broadcastUserUpdate(userId, update, None)
          _ ← broadcastUserUpdate(readerUserId, readerUpdate, None)
        } yield {
          // TODO: report errors
          db.run(markMessagesRead(models.Peer.privat(readerUserId), models.Peer.privat(userId), new DateTime(date)))
        }) onFailure {
          case e ⇒
            log.error(e, "Failed to mark messages read")
        }
      }
  }
}