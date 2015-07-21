package im.actor.server.user

import akka.actor._
import akka.contrib.pattern.{ ClusterSharding, ShardRegion }
import akka.pattern.{ ask, pipe }
import akka.persistence.RecoveryFailure
import akka.util.Timeout
import com.google.protobuf.ByteString
import im.actor.api.rpc.messaging.{ Message ⇒ ApiMessage, _ }
import im.actor.api.rpc.peers.{ Peer, PeerType }
import im.actor.server.commons.serialization.ActorSerializer
import im.actor.server.models
import im.actor.server.office.user._
import im.actor.server.office.{ PeerOffice, Office, user }
import im.actor.server.push.SeqUpdatesManager.SequenceState
import im.actor.server.push.{ SeqUpdatesManager, SeqUpdatesManagerRegion }
import im.actor.server.sequence.SeqState
import im.actor.server.social.{ SocialManager, SocialManagerRegion }
import im.actor.server.util.{ HistoryUtils, UserUtils }
import org.joda.time.DateTime
import slick.driver.PostgresDriver.api._

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

case class UserOfficeRegion(val ref: ActorRef)

object UserOffice {
  import user._
  import UserEnvelope._

  private val idExtractor: ShardRegion.IdExtractor = {
    case UserEnvelope(userId, payload) ⇒ (userId.toString, payload)
  }

  private val shardResolver: ShardRegion.ShardResolver = msg ⇒ msg match {
    case UserEnvelope(userId, _) ⇒ (userId % 100).toString // TODO: configurable
  }

  ActorSerializer.register(3000, classOf[UserEnvelope])
  ActorSerializer.register(3001, classOf[UserEnvelope.NewAuth])
  ActorSerializer.register(3002, classOf[UserEnvelope.NewAuthResponse])
  ActorSerializer.register(3003, classOf[UserEnvelope.SendMessage])
  ActorSerializer.register(3004, classOf[UserEnvelope.MessageReceived])
  ActorSerializer.register(3005, classOf[UserEnvelope.BroadcastUpdate])
  ActorSerializer.register(3006, classOf[UserEnvelope.BroadcastUpdateResponse])

  ActorSerializer.register(3500, classOf[UserEvent])
  ActorSerializer.register(3501, classOf[UserEvent.AuthAdded])

  private def startRegion(props: Option[Props])(implicit system: ActorSystem): UserOfficeRegion =
    UserOfficeRegion(ClusterSharding(system).start(
      typeName = "UserOffice",
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
  ): UserOfficeRegion =
    startRegion(Some(props))

  def startRegionProxy()(implicit system: ActorSystem): UserOfficeRegion =
    startRegion(None)

  def props(
    implicit
    db:                  Database,
    seqUpdManagerRegion: SeqUpdatesManagerRegion,
    socialManagerRegion: SocialManagerRegion
  ): Props =
    Props(classOf[UserOffice], db, seqUpdManagerRegion, socialManagerRegion)

  def persistenceIdFor(userId: Int): String = s"user_${userId}"

  def auth(userId: Int, authId: Long)(
    implicit
    userOfficeRegion: UserOfficeRegion,
    timeout:          Timeout,
    ec:               ExecutionContext

  ): Future[Unit] = (userOfficeRegion.ref ? UserEnvelope(userId).withNewAuth(NewAuth(authId))) map (_ ⇒ ())

  def sendMessage(userId: Int, senderUserId: Int, senderAuthId: Long, randomId: Long, date: DateTime, message: ApiMessage)(
    implicit
    peerManagerRegion: UserOfficeRegion,
    timeout:           Timeout,
    ec:                ExecutionContext
  ): Future[SeqState] = {
    (peerManagerRegion.ref ? UserEnvelope(userId).withSendMessage(SendMessage(senderUserId, senderAuthId, randomId, date.getMillis, message))).mapTo[SeqState]
  }

  def deliverGroupMessage(userId: Int, groupId: Int, senderUserId: Int, randomId: Long, date: DateTime, message: ApiMessage, isFat: Boolean)(
    implicit
    region:  UserOfficeRegion,
    timeout: Timeout,
    ec:      ExecutionContext
  ): Unit =
    region.ref ! UserEnvelope(userId).withDeliverGroupMessage(DeliverGroupMessage(groupId, senderUserId, randomId, date.getMillis, message, isFat))

  def deliverOwnGroupMessage(userId: Int, groupId: Int, senderAuthId: Long, randomId: Long, date: DateTime, message: ApiMessage, isFat: Boolean)(
    implicit
    region:  UserOfficeRegion,
    timeout: Timeout,
    ec:      ExecutionContext
  ): Future[SeqState] =
    (region.ref ? UserEnvelope(userId).withDeliverOwnGroupMessage(DeliverOwnGroupMessage(groupId, senderAuthId, randomId, date.getMillis, message, isFat))).mapTo[SeqState]

  def messageReceived(userId: Int, receiverUserId: Int, receiverAuthId: Long, date: Long, receivedDate: Long)(implicit peerManagerRegion: UserOfficeRegion): Unit = {
    peerManagerRegion.ref ! UserEnvelope(userId).withMessageReceived(MessageReceived(receiverUserId, receiverAuthId, date, receivedDate))
  }

  def messageRead(userId: Int, readerUserId: Int, readerAuthId: Long, date: Long, readDate: Long)(implicit peerManagerRegion: UserOfficeRegion): Unit = {
    peerManagerRegion.ref ! UserEnvelope(userId).withMessageRead(MessageRead(readerUserId, readerAuthId, date, readDate))
  }
}

class UserOffice(
  implicit
  db:                  Database,
  seqUpdManagerRegion: SeqUpdatesManagerRegion,
  socialManagerRegion: SocialManagerRegion
) extends PeerOffice with ActorLogging {
  import HistoryUtils._
  import SeqUpdatesManager._
  import SocialManager._
  import UserEnvelope._
  import UserUtils._
  import UserOffice._

  implicit private val ec: ExecutionContext = context.dispatcher

  private val userId = self.path.name.toInt

  override def persistenceId = persistenceIdFor(userId)

  private[this] var lastReceivedDate: Option[Long] = None
  private[this] var lastReadDate: Option[Long] = None
  private[this] var authIds = Set.empty[Long]

  def receiveCommand = {
    case Payload.NewAuth(NewAuth(authId)) ⇒
      persist(UserEvent.AuthAdded(authId)) { _ ⇒
        authIds += authId
        sender() ! Status.Success(())
      }
    case Payload.DeliverGroupMessage(DeliverGroupMessage(groupId, senderUserId, randomId, date, message, isFat)) ⇒
      val update = UpdateMessage(
        peer = Peer(PeerType.Group, groupId),
        senderUserId = senderUserId,
        date = date,
        randomId = randomId,
        message = message
      )

      persistAndPushUpdates(authIds, update, None, isFat)
    case Payload.DeliverOwnGroupMessage(DeliverOwnGroupMessage(groupId, senderAuthId, randomId, date, message, isFat)) ⇒
      val groupPeer = Peer(PeerType.Group, groupId)

      val update = UpdateMessage(
        peer = groupPeer,
        senderUserId = userId,
        date = date,
        randomId = randomId,
        message = message
      )

      persistAndPushUpdates(authIds filterNot (_ == senderAuthId), update, None, isFat)

      val ownUpdate = UpdateMessageSent(groupPeer, randomId, date)
      db.run(for {
        (seq, state) ← persistAndPushUpdate(senderAuthId, ownUpdate, None, isFat)
      } yield (SeqState(seq, ByteString.copyFrom(state)))) pipeTo sender()
    /*
      db.run {
        for {
          pushText <- getPushText(message, userId, senderUserId)
        } yield {
          persistAndPushUpdates(authIds, update, pushText, isFat)
        }
      }*/
    case Payload.SendMessage(SendMessage(senderUserId, senderAuthId, randomId, date, message, _)) ⇒
      context become {
        case MessageSentComplete ⇒
          unstashAll()
          context become receiveCommand
        case msg ⇒ stash()
      }

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

      val sendFuture = db.run(for {

        clientUser ← getUserUnsafe(senderUserId)
        pushText ← getPushText(message, clientUser, userId)

        _ ← broadcastUserUpdate(userId, peerUpdate, Some(pushText))

        _ ← notifyUserUpdate(senderUserId, senderAuthId, senderUpdate, None)
        (seq, state) ← persistAndPushUpdate(senderAuthId, clientUpdate, None)
      } yield {
        recordRelation(senderUserId, userId)
        db.run(writeHistoryMessage(models.Peer.privat(senderUserId), models.Peer.privat(userId), new DateTime(date), randomId, message.header, message.toByteArray))
        SeqState(seq, ByteString.copyFrom(state))
      })

      sendFuture onComplete {
        case Success(seqstate) ⇒
          replyTo ! seqstate
          self ! MessageSentComplete
        case Failure(e) ⇒
          replyTo ! Status.Failure(e)
          log.error(e, "Failed to send message")
          self ! MessageSentComplete
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

  override def receiveRecover = {
    case UserEvent.AuthAdded(authId) ⇒
      authIds += authId
    case RecoveryFailure(e) ⇒
      log.error(e, "Failed to recover")
  }
}