package im.actor.server.user

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

import akka.actor._
import akka.pattern.pipe
import akka.persistence.RecoveryFailure
import akka.util.Timeout
import com.github.benmanes.caffeine.cache.Cache
import org.joda.time.DateTime
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc.messaging._
import im.actor.api.rpc.peers.{ Peer, PeerType }
import im.actor.server.commons.serialization.ActorSerializer
import im.actor.server.office.PeerOffice
import im.actor.server.office.PeerOffice.MessageSentComplete
import im.actor.server.push.{ SeqUpdatesManager, SeqUpdatesManagerRegion }
import im.actor.server.sequence.{ SeqState, SeqStateDate }
import im.actor.server.social.{ SocialManager, SocialManagerRegion }
import im.actor.server.util.{ ACLUtils, HistoryUtils, UserUtils }
import im.actor.server.{ models, persist ⇒ p }
import im.actor.utils.cache.CacheHelpers._

trait UserEvent

trait UserCommand {
  val userId: Int
}

object UserOfficeActor {
  ActorSerializer.register(3000, classOf[UserEnvelope])
  ActorSerializer.register(3001, classOf[UserEnvelope.NewAuth])
  ActorSerializer.register(3002, classOf[UserEnvelope.NewAuthResponse])
  ActorSerializer.register(3003, classOf[UserEnvelope.SendMessage])
  ActorSerializer.register(3004, classOf[UserEnvelope.MessageReceived])
  ActorSerializer.register(3005, classOf[UserEnvelope.BroadcastUpdate])
  ActorSerializer.register(3006, classOf[UserEnvelope.BroadcastUpdateResponse])
  ActorSerializer.register(3007, classOf[UserEnvelope.RemoveAuth])
  ActorSerializer.register(3008, classOf[UserEnvelope.UserInfo])
  ActorSerializer.register(3009, classOf[UserEnvelope.MessageRead])

  ActorSerializer.register(4001, classOf[UserEvents.AuthAdded])
  ActorSerializer.register(4002, classOf[UserEvents.AuthRemoved])
  ActorSerializer.register(4003, classOf[UserEvents.UserInfoAdded])
  ActorSerializer.register(4004, classOf[UserEvents.MessageReceived])
  ActorSerializer.register(4005, classOf[UserEvents.MessageRead])

  private[user] case class User(id: Int, accessSalt: String, name: String)

  def props(
    implicit
    db:                  Database,
    seqUpdManagerRegion: SeqUpdatesManagerRegion,
    socialManagerRegion: SocialManagerRegion
  ): Props =
    Props(classOf[UserOfficeActor], db, seqUpdManagerRegion, socialManagerRegion)
}

class UserOfficeActor(
  implicit
  db:                  Database,
  seqUpdManagerRegion: SeqUpdatesManagerRegion,
  socialManagerRegion: SocialManagerRegion
) extends PeerOffice with ActorLogging {

  import HistoryUtils._
  import SeqUpdatesManager._
  import SocialManager._
  import UserEnvelope._
  import UserOffice._
  import UserUtils._

  private val MaxCacheSize = 100L

  implicit val region: UserOfficeRegion = UserOfficeRegion(context.parent)
  implicit private val timeout: Timeout = Timeout(10.seconds)

  implicit private val system: ActorSystem = context.system
  implicit private val ec: ExecutionContext = context.dispatcher

  private val userId = self.path.name.toInt

  override def persistenceId = persistenceIdFor(userId)

  private[this] var lastReceivedDate: Option[Long] = None
  private[this] var lastReadDate: Option[Long] = None
  private[this] var authIds = Set.empty[Long]
  private[this] var accessSalt: Option[String] = None

  type AuthIdRandomId = (Long, Long)
  implicit val sendResponseCache: Cache[AuthIdRandomId, Future[SeqStateDate]] =
    createCache[AuthIdRandomId, Future[SeqStateDate]](MaxCacheSize)

  def receiveCommand: Receive = {
    case Payload.NewAuth(NewAuth(authId)) ⇒
      persist(UserEvents.AuthAdded(authId)) { _ ⇒
        authIds += authId
        if (accessSalt.isEmpty) {
          initAccessSalt()
          context become {
            case Payload.UserInfo(UserEnvelope.UserInfo(salt)) ⇒
              persist(UserEvents.UserInfoAdded(salt)) { _ ⇒
                accessSalt = Some(salt)
                unstashAll()
                context become receiveCommand
              }
            case FailedToFetchInfo ⇒ self ! Kill
            case msg               ⇒ stash()
          }
        }
        sender() ! Status.Success(())
      }
    case Payload.RemoveAuth(RemoveAuth(authId)) ⇒
      persist(UserEvents.AuthRemoved(authId)) { _ ⇒
        authIds -= authId
        sender() ! Status.Success(())
      }
    case Payload.DeliverMessage(DeliverMessage(peer, senderUserId, randomId, date, message, isFat)) ⇒
      val update = UpdateMessage(
        peer = peer,
        senderUserId = senderUserId,
        date = date.getMillis,
        randomId = randomId,
        message = message
      )
      db.run {
        for {
          senderUser ← getUserUnsafe(senderUserId)
          pushText ← getPushText(message, senderUser, userId)
          seqs ← persistAndPushUpdates(authIds, update, Some(pushText), isFat)
        } yield seqs
      }
    case Payload.DeliverOwnMessage(DeliverOwnMessage(peer, senderAuthId, randomId, date, message, isFat)) ⇒
      val update = UpdateMessage(
        peer = peer,
        senderUserId = userId,
        date = date.getMillis,
        randomId = randomId,
        message = message
      )

      persistAndPushUpdates(authIds filterNot (_ == senderAuthId), update, None, isFat)

      val ownUpdate = UpdateMessageSent(peer, randomId, date.getMillis)
      db.run(persistAndPushUpdate(senderAuthId, ownUpdate, None, isFat)) pipeTo sender()
    case Payload.SendMessage(SendMessage(senderUserId, senderAuthId, accessHash, randomId, message, isFat)) ⇒
      val isCorrectHash = accessSalt.exists { salt ⇒
        accessHash == ACLUtils.userAccessHash(senderAuthId, userId, salt)
      }
      if (isCorrectHash) {
        val replyTo = sender()
        context become {
          case MessageSentComplete ⇒
            unstashAll()
            context become receiveCommand
          case msg ⇒ stash()
        }
        val date = new DateTime
        val dateMillis = date.getMillis

        val sendFuture: Future[SeqStateDate] =
          withCachedFuture[AuthIdRandomId, SeqStateDate](senderAuthId → randomId) { () ⇒
            for {
              _ ← Future.successful(UserOffice.deliverMessage(userId, privatePeerStruct(senderUserId), senderUserId, randomId, date, message, isFat))
              SeqState(seq, state) ← UserOffice.deliverOwnMessage(senderUserId, privatePeerStruct(userId), senderAuthId, randomId, date, message, isFat)
              _ ← Future.successful(recordRelation(senderUserId, userId))
            } yield {
              db.run(writeHistoryMessage(models.Peer.privat(senderUserId), models.Peer.privat(userId), date, randomId, message.header, message.toByteArray))
              SeqStateDate(seq, state, dateMillis)
            }
          }
        sendFuture onComplete {
          case Success(seqstate) ⇒
            replyTo ! seqstate
            self ! MessageSentComplete
          case Failure(e) ⇒
            replyTo ! Status.Failure(e)
            log.error(e, "Failed to send message")
            self ! MessageSentComplete
        }
      } else {
        sender() ! Status.Failure(InvalidAccessHash)
      }
    case Payload.MessageReceived(MessageReceived(receiverUserId, _, date, receivedDate)) ⇒
      if (!lastReceivedDate.exists(_ > date)) {
        persist(UserEvents.MessageReceived(date)) { _ ⇒
          lastReceivedDate = Some(date)
          val update = UpdateMessageReceived(Peer(PeerType.Private, receiverUserId), date, receivedDate)

          db.run(for {
            _ ← persistAndPushUpdates(authIds, update, None)
          } yield {
            // TODO: report errors
            db.run(markMessagesReceived(models.Peer.privat(receiverUserId), models.Peer.privat(userId), new DateTime(date)))
          }) onFailure {
            case e ⇒
              log.error(e, "Failed to mark messages received")
          }
        }
      }
    case Payload.MessageRead(MessageRead(readerUserId, _, date, readDate)) ⇒
      if (!lastReadDate.exists(_ > date)) {
        persist(UserEvents.MessageRead(date)) { _ ⇒
          lastReadDate = Some(date)
          val update = UpdateMessageRead(Peer(PeerType.Private, readerUserId), date, readDate)
          val readerUpdate = UpdateMessageReadByMe(Peer(PeerType.Private, userId), date)

          db.run(for {
            _ ← persistAndPushUpdates(authIds, update, None)
            _ ← broadcastUserUpdate(readerUserId, readerUpdate, None) //todo: may be replace with MessageReadOwn
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

  override def receiveRecover: Receive = {
    case UserEvents.AuthAdded(authId) ⇒
      authIds += authId
    case UserEvents.AuthRemoved(authId) ⇒
      authIds -= authId
    case UserEvents.UserInfoAdded(salt) ⇒
      accessSalt = Some(salt)
    case UserEvents.MessageRead(date) ⇒
      lastReadDate = Some(date)
    case UserEvents.MessageReceived(date) ⇒
      lastReceivedDate = Some(date)
    case RecoveryFailure(e) ⇒
      log.error(e, "Failed to recover")
  }

  private def initAccessSalt(): Unit =
    db.run(p.User.find(userId).headOption) onComplete {
      case Success(user) ⇒ user.map(_.accessSalt) foreach { salt ⇒ self ! Payload.UserInfo(UserEnvelope.UserInfo(salt)) }
      case Failure(_)    ⇒ self ! FailedToFetchInfo
    }

}