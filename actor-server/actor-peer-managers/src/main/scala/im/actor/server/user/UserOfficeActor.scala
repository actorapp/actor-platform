package im.actor.server.user

import akka.actor._
import akka.contrib.pattern.ShardRegion
import akka.persistence.{ RecoveryCompleted, RecoveryFailure }
import akka.util.Timeout
import com.github.benmanes.caffeine.cache.Cache
import im.actor.server.commons.serialization.ActorSerializer
import im.actor.server.office.{ PeerOffice, StopOffice }
import im.actor.server.push.SeqUpdatesManagerRegion
import im.actor.server.sequence.SeqStateDate
import im.actor.server.social.SocialManagerRegion
import im.actor.utils.cache.CacheHelpers._
import slick.driver.PostgresDriver.api._

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

trait UserEvent

trait UserCommand {
  val userId: Int
}

case class User(
  id:               Int,
  accessSalt:       String,
  name:             String,
  countryCode:      String,
  phones:           Seq[Long],
  emails:           Seq[String],
  lastReceivedDate: Option[Long],
  lastReadDate:     Option[Long],
  authIds:          Set[Long],
  isDeleted:        Boolean
)

object UserOfficeActor {
  ActorSerializer.register(3000, classOf[UserCommands])
  ActorSerializer.register(3001, classOf[UserCommands.NewAuth])
  ActorSerializer.register(3002, classOf[UserCommands.NewAuthAck])
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
  ActorSerializer.register(3017, classOf[UserCommands.RemoveAuthAck])
  ActorSerializer.register(3018, classOf[UserCommands.DeleteAck])
  ActorSerializer.register(3019, classOf[UserCommands.AddPhone])
  ActorSerializer.register(3020, classOf[UserCommands.AddPhoneAck])
  ActorSerializer.register(3021, classOf[UserCommands.AddEmail])
  ActorSerializer.register(3022, classOf[UserCommands.AddEmailAck])
  ActorSerializer.register(3023, classOf[UserCommands.ChangeCountryCodeAck])

  ActorSerializer.register(4001, classOf[UserEvents.AuthAdded])
  ActorSerializer.register(4002, classOf[UserEvents.AuthRemoved])
  ActorSerializer.register(4003, classOf[UserEvents.Created])
  ActorSerializer.register(4004, classOf[UserEvents.MessageReceived])
  ActorSerializer.register(4005, classOf[UserEvents.MessageRead])
  ActorSerializer.register(4006, classOf[UserEvents.Deleted])
  ActorSerializer.register(4007, classOf[UserEvents.NameChanged])
  ActorSerializer.register(4008, classOf[UserEvents.CountryCodeChanged])
  ActorSerializer.register(4009, classOf[UserEvents.PhoneAdded])
  ActorSerializer.register(4010, classOf[UserEvents.EmailAdded])

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
) extends PeerOffice with UserCommandHandlers with ActorLogging {

  import UserCommands._
  import UserOffice._

  override type OfficeState = User
  override type OfficeEvent = UserEvent

  override protected def workWith(evt: OfficeEvent, user: OfficeState): Unit = context become working(updateState(evt, user))

  context.setReceiveTimeout(15.minutes)

  private val MaxCacheSize = 100L

  implicit val region: UserOfficeRegion = UserOfficeRegion(context.parent)
  implicit val timeout: Timeout = Timeout(10.seconds)

  implicit val system: ActorSystem = context.system
  implicit val ec: ExecutionContext = context.dispatcher

  protected val userId = self.path.name.toInt

  override def persistenceId = persistenceIdFor(userId)

  implicit val sendResponseCache: Cache[AuthIdRandomId, Future[SeqStateDate]] =
    createCache[AuthIdRandomId, Future[SeqStateDate]](MaxCacheSize)

  override def receiveCommand = creating

  private[this] def creating: Receive = {
    case Create(_, accessSalt, name, countryCode, sex, clientAuthId) ⇒ create(accessSalt, name, countryCode, sex, clientAuthId)
  }

  protected def working(state: User): Receive = {
    case NewAuth(_, authId) ⇒
      addAuth(state, authId)
    case RemoveAuth(_, authId)             ⇒ removeAuth(state, authId)
    case ChangeCountryCode(_, countryCode) ⇒ changeCountryCode(state, countryCode)
    case ChangeName(_, name, clientAuthId) ⇒ changeName(state, name, clientAuthId)
    case Delete(_)                         ⇒ delete(state)
    case AddPhone(_, phone)                ⇒ addPhone(state, phone)
    case AddEmail(_, email)                ⇒ addEmail(state, email)
    case DeliverMessage(_, peer, senderUserId, randomId, date, message, isFat) ⇒
      deliverMessage(state, peer, senderUserId, randomId, date, message, isFat)
    case DeliverOwnMessage(_, peer, senderAuthId, randomId, date, message, isFat) ⇒
      deliverOwnMessage(state, peer, senderAuthId, randomId, date, message, isFat)
    case SendMessage(_, senderUserId, senderAuthId, accessHash, randomId, message, isFat) ⇒
      sendMessage(state, senderUserId, senderAuthId, accessHash, randomId, message, isFat)
    case MessageReceived(_, receiverUserId, _, date, receivedDate) ⇒
      messageReceived(state, receiverUserId, date, receivedDate)
    case MessageRead(_, readerUserId, _, date, readDate) ⇒ messageRead(state, readerUserId, date, readDate)
    case StopOffice                                      ⇒ context stop self
    case ReceiveTimeout                                  ⇒ context.parent ! ShardRegion.Passivate(stopMessage = StopOffice)
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
      case UserEvents.PhoneAdded(phone) ⇒
        user.copy(phones = user.phones :+ phone)
      case UserEvents.EmailAdded(email) ⇒
        user.copy(emails = user.emails :+ email)
      case UserEvents.Deleted() ⇒
        user.copy(isDeleted = true)
      case UserEvents.MessageReceived(date) ⇒
        user.copy(lastReceivedDate = Some(date))
      case UserEvents.MessageRead(date) ⇒
        user.copy(lastReadDate = Some(date))
      case _: UserEvents.Created ⇒ user
    }
  }

  protected def initState(evt: UserEvents.Created): User =
    User(
      id = evt.userId,
      accessSalt = evt.accessSalt,
      name = evt.name,
      countryCode = evt.countryCode,
      phones = Seq.empty[Long],
      emails = Seq.empty[String],
      lastReceivedDate = None,
      lastReadDate = None,
      authIds = Set.empty[Long],
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
}