package im.actor.server.user

import java.time.{ LocalDateTime, ZoneOffset }

import akka.actor._
import akka.pattern.pipe
import akka.util.Timeout
import com.github.benmanes.caffeine.cache.Cache
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
import org.joda.time.DateTime
import slick.driver.PostgresDriver.api._

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

trait UserEvent

trait UserCommand {
  val userId: Int
}

case class User(
  id:               Int,
  accessSalt:       String,
  name:             String,
  lastReceivedDate: Option[Long],
  lastReadDate:     Option[Long],
  authIds:          Set[Long]
)

object UserOfficeActor {
  ActorSerializer.register(3000, classOf[UserCommands])
  ActorSerializer.register(3001, classOf[UserCommands.NewAuth])
  ActorSerializer.register(3002, classOf[UserCommands.NewAuthResponse])
  ActorSerializer.register(3003, classOf[UserCommands.SendMessage])
  ActorSerializer.register(3004, classOf[UserCommands.MessageReceived])
  ActorSerializer.register(3005, classOf[UserCommands.BroadcastUpdate])
  ActorSerializer.register(3006, classOf[UserCommands.BroadcastUpdateResponse])
  ActorSerializer.register(3007, classOf[UserCommands.RemoveAuth])
  ActorSerializer.register(3008, classOf[UserCommands.Create])
  ActorSerializer.register(3009, classOf[UserCommands.MessageRead])

  ActorSerializer.register(4001, classOf[UserEvents.AuthAdded])
  ActorSerializer.register(4002, classOf[UserEvents.AuthRemoved])
  ActorSerializer.register(4003, classOf[UserEvents.Created])
  ActorSerializer.register(4004, classOf[UserEvents.MessageReceived])
  ActorSerializer.register(4005, classOf[UserEvents.MessageRead])

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
  import UserCommands._
  import UserOffice._
  import UserUtils._

  private val MaxCacheSize = 100L

  implicit val region: UserOfficeRegion = UserOfficeRegion(context.parent)
  implicit private val timeout: Timeout = Timeout(10.seconds)

  implicit private val system: ActorSystem = context.system
  implicit private val ec: ExecutionContext = context.dispatcher

  private val userId = self.path.name.toInt

  override def persistenceId = persistenceIdFor(userId)

  type AuthIdRandomId = (Long, Long)
  implicit val sendResponseCache: Cache[AuthIdRandomId, Future[SeqStateDate]] =
    createCache[AuthIdRandomId, Future[SeqStateDate]](MaxCacheSize)

  override def receiveCommand = creating

  def creating: Receive = {
    case evt: Create ⇒
      val user = models.User(
        id = evt.userId,
        accessSalt = evt.accessSalt,
        name = evt.name,
        countryCode = evt.countryCode,
        sex = null, //TODO: models.Sex.fromInt(evt.sex),
        state = models.UserState.Registered,
        createdAt = LocalDateTime.now(ZoneOffset.UTC)
      )
      persist(UserEvents.Created) { _ ⇒
        val state = initState(evt)
        context become working(state)
        db.run(for {
          _ ← p.User.create(user)
        } yield ()) pipeTo sender()
      }
  }

  private def initState(evt: UserCommands.Create): User =
    User(
      evt.userId,
      evt.accessSalt,
      evt.name,
      None,
      None,
      Set.empty[Long]
    )

  def working(state: User): Receive = {
    case NewAuth(userId, authId) ⇒
      persist(UserEvents.AuthAdded(authId)) { _ ⇒
        context become working(updateState(UserEvents.AuthAdded(authId), state))
        sender() ! Status.Success(())
      }
    case RemoveAuth(userId, authId) ⇒
      persist(UserEvents.AuthRemoved(authId)) { _ ⇒
        context become working(updateState(UserEvents.AuthRemoved(authId), state))
        sender() ! Status.Success(())
      }
    case DeliverMessage(userId, peer, senderUserId, randomId, date, message, isFat) ⇒
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
          seqs ← persistAndPushUpdates(state.authIds, update, Some(pushText), isFat)
        } yield seqs
      }
    case DeliverOwnMessage(userId, peer, senderAuthId, randomId, date, message, isFat) ⇒
      val update = UpdateMessage(
        peer = peer,
        senderUserId = userId,
        date = date.getMillis,
        randomId = randomId,
        message = message
      )

      persistAndPushUpdates(state.authIds filterNot (_ == senderAuthId), update, None, isFat)

      val ownUpdate = UpdateMessageSent(peer, randomId, date.getMillis)
      db.run(persistAndPushUpdate(senderAuthId, ownUpdate, None, isFat)) pipeTo sender()
    case SendMessage(userId, senderUserId, senderAuthId, accessHash, randomId, message, isFat) ⇒
      if (accessHash == ACLUtils.userAccessHash(senderAuthId, userId, state.accessSalt)) {
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
    case MessageReceived(userId, receiverUserId, _, date, receivedDate) ⇒
      if (!state.lastReceivedDate.exists(_ > date)) {
        persist(UserEvents.MessageReceived(date)) { _ ⇒
          context become working(updateState(UserEvents.MessageReceived(date), state))
          val update = UpdateMessageReceived(Peer(PeerType.Private, receiverUserId), date, receivedDate)

          db.run(for {
            _ ← persistAndPushUpdates(state.authIds, update, None)
          } yield {
            // TODO: report errors
            db.run(markMessagesReceived(models.Peer.privat(receiverUserId), models.Peer.privat(userId), new DateTime(date)))
          }) onFailure {
            case e ⇒
              log.error(e, "Failed to mark messages received")
          }
        }
      }
    case MessageRead(userId, readerUserId, _, date, readDate) ⇒
      if (!state.lastReadDate.exists(_ > date)) {
        persist(UserEvents.MessageRead(date)) { _ ⇒
          context become working(updateState(UserEvents.MessageRead(date), state))
          val update = UpdateMessageRead(Peer(PeerType.Private, readerUserId), date, readDate)
          val readerUpdate = UpdateMessageReadByMe(Peer(PeerType.Private, userId), date)

          db.run(for {
            _ ← persistAndPushUpdates(state.authIds, update, None)
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

  protected def updateState(evt: UserEvent, state: User): User = ???
  //  {
  //    evt match {
  //      case UserEvents.AuthAdded(authId) ⇒
  //        state.copy(state.authIds :+ authId)
  //      case UserEvents.MessageReceived(date) ⇒
  //        state.copy(lastReceivedDate = Some(new DateTime(date)))
  //      case GroupEvents.MessageRead(userId, date) ⇒
  //        state.copy(
  //          lastReadDate = Some(new DateTime(date)),
  //          invitedUserIds = state.invitedUserIds - userId
  //        )
  //      case GroupEvents.UserInvited(userId, inviterUserId, invitedAt) ⇒
  //        state.copy(
  //          members = state.members + (userId → Member(userId, inviterUserId, new DateTime(invitedAt))),
  //          invitedUserIds = state.invitedUserIds + userId
  //        )
  //      case GroupEvents.UserJoined(userId, inviterUserId, invitedAt) ⇒
  //        state.copy(
  //          members = state.members + (userId → Member(userId, inviterUserId, new DateTime(invitedAt)))
  //        )
  //      case GroupEvents.UserKicked(userId, kickerUserId, _) ⇒
  //        state.copy(members = state.members - userId)
  //      case GroupEvents.UserLeft(userId, _) ⇒
  //        state.copy(members = state.members - userId)
  //      case GroupEvents.AvatarUpdated(avatar) ⇒
  //        state.copy(avatar = avatar)
  //    }
  //  }

  override def receiveRecover: Receive = ???

  //  {
  //    case UserEvents.AuthAdded(authId) ⇒
  //      authIds += authId
  //    case UserEvents.AuthRemoved(authId) ⇒
  //      authIds -= authId
  //    case UserEvents.UserInfoAdded(salt) ⇒
  //      accessSalt = Some(salt)
  //    case UserEvents.MessageRead(date) ⇒
  //      lastReadDate = Some(date)
  //    case UserEvents.MessageReceived(date) ⇒
  //      lastReceivedDate = Some(date)
  //    case RecoveryFailure(e) ⇒
  //      log.error(e, "Failed to recover")
  //  }

}