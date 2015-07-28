package im.actor.server.user

import java.time.{ LocalDateTime, ZoneOffset }

import akka.actor._
import akka.contrib.pattern.ShardRegion
import akka.pattern.pipe
import akka.persistence.{ RecoveryCompleted, RecoveryFailure }
import akka.util.Timeout
import com.github.benmanes.caffeine.cache.Cache
import im.actor.api.rpc.messaging._
import im.actor.api.rpc.peers.{ Peer, PeerType }
import im.actor.api.rpc.users.UpdateUserNameChanged
import im.actor.server.commons.serialization.ActorSerializer
import im.actor.server.office.{ StopOffice, PeerOffice }
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
  countryCode:      String,
  lastReceivedDate: Option[Long],
  lastReadDate:     Option[Long],
  authIds:          Set[Long],
  isDeleted:        Boolean
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
  ActorSerializer.register(3010, classOf[UserCommands.Delete])
  ActorSerializer.register(3011, classOf[UserCommands.ChangeNameAck])
  ActorSerializer.register(3012, classOf[UserCommands.ChangeName])
  ActorSerializer.register(3013, classOf[UserCommands.CreateAck])
  ActorSerializer.register(3014, classOf[UserCommands.ChangeCountryCode])
  ActorSerializer.register(3015, classOf[UserCommands.DeliverMessage])
  ActorSerializer.register(3016, classOf[UserCommands.DeliverOwnMessage])

  ActorSerializer.register(4001, classOf[UserEvents.AuthAdded])
  ActorSerializer.register(4002, classOf[UserEvents.AuthRemoved])
  ActorSerializer.register(4003, classOf[UserEvents.Created])
  ActorSerializer.register(4004, classOf[UserEvents.MessageReceived])
  ActorSerializer.register(4005, classOf[UserEvents.MessageRead])
  ActorSerializer.register(4006, classOf[UserEvents.Deleted])
  ActorSerializer.register(4007, classOf[UserEvents.NameChanged])
  ActorSerializer.register(4008, classOf[UserEvents.CountryCodeChanged])

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

  override type OfficeState = User
  override type OfficeEvent = UserEvent

  context.setReceiveTimeout(15.minutes)

  private val MaxCacheSize = 100L

  implicit val region: UserOfficeRegion = UserOfficeRegion(context.parent)
  implicit private val timeout: Timeout = Timeout(10.seconds)

  implicit private val system: ActorSystem = context.system
  implicit private val ec: ExecutionContext = context.dispatcher

  private val userId = self.path.name.toInt

  override def persistenceId = persistenceIdFor(userId)

  implicit val sendResponseCache: Cache[AuthIdRandomId, Future[SeqStateDate]] =
    createCache[AuthIdRandomId, Future[SeqStateDate]](MaxCacheSize)

  override def receiveCommand = creating

  def creating: Receive = {
    case Create(userId, accessSalt, name, countryCode, sex) ⇒
      val user = models.User(
        id = userId,
        accessSalt = accessSalt,
        name = name,
        countryCode = countryCode,
        sex = models.Sex.fromInt(sex.id),
        state = models.UserState.Registered,
        createdAt = LocalDateTime.now(ZoneOffset.UTC)
      )
      val evt = UserEvents.Created(userId, accessSalt, name, countryCode)
      persist(evt) { _ ⇒
        val state = initState(evt)
        context become working(state)
        db.run(for {
          _ ← p.User.create(user)
        } yield CreateAck) pipeTo sender()
      }
  }

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
    case ChangeCountryCode(userId, countryCode) ⇒
      persist(UserEvents.CountryCodeChanged(countryCode)) { _ ⇒
        db.run(p.User.setCountryCode(userId, countryCode))
      }
    case ChangeName(userId, name, authId) ⇒
      persist(UserEvents.NameChanged(name)) { _ ⇒
        val update = UpdateUserNameChanged(userId, name)
        val action = for {
          relatedUserIds ← DBIO.from(getRelations(userId))
          _ ← broadcastUsersUpdate(relatedUserIds, update, None)
          _ ← persistAndPushUpdates(state.authIds.filterNot(_ == authId), update, None)
          SeqState(seq, state) ← persistAndPushUpdate(authId, update, None)
        } yield ChangeNameAck(seq, state)
        db.run(action) pipeTo sender()
      }
    case Delete(userId) ⇒
      persist(UserEvents.Deleted) { _ ⇒
        db.run(p.User.setDeletedAt(userId))
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
            context become working(state)
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
    case StopOffice     ⇒ context stop self
    case ReceiveTimeout ⇒ context.parent ! ShardRegion.Passivate(stopMessage = StopOffice)

  }

  override protected def updateState(evt: OfficeEvent, user: OfficeState): OfficeState = {
    evt match {
      case UserEvents.AuthAdded(authId) ⇒
        user.copy(authIds = user.authIds + authId)
      case UserEvents.AuthRemoved(authId) ⇒
        user.copy(authIds = user.authIds - authId)
      case UserEvents.CountryCodeChanged(countryCode) ⇒
        user.copy(countryCode = countryCode)
      case UserEvents.NameChanged(name) ⇒
        user.copy(name = name)
      case UserEvents.Deleted() ⇒
        user.copy(isDeleted = true)
      case UserEvents.MessageReceived(date) ⇒
        user.copy(lastReceivedDate = Some(date))
      case UserEvents.MessageRead(date) ⇒
        user.copy(lastReadDate = Some(date))
    }
  }

  private[this] def initState(evt: UserEvents.Created): User =
    User(
      evt.userId,
      evt.accessSalt,
      evt.name,
      evt.countryCode,
      None,
      None,
      Set.empty[Long],
      isDeleted = false
    )

  private[this] var userStateMaybe: Option[OfficeState] = None

  override def receiveRecover: Receive = {
    case evt: UserEvents.Created ⇒
      userStateMaybe = Some(initState(evt))
    case evt: UserEvent ⇒
      userStateMaybe = userStateMaybe map (updateState(evt, _))
    case RecoveryFailure(e) ⇒
      log.error(e, "Failed to recover")
    case RecoveryCompleted ⇒
      userStateMaybe match {
        case Some(user) ⇒ context become working(user)
        case None       ⇒ context become creating
      }
    case unmatched ⇒
      log.error("Unmatched recovery event {}", unmatched)
  }

  override protected def workWith(evt: OfficeEvent, user: OfficeState): Unit = context become working(updateState(evt, user))
}