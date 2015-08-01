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

trait UserQuery {
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
  isDeleted:        Boolean,
  nickname:         Option[String],
  about:            Option[String]
)

object UserOfficeActor {
  ActorSerializer.register(10000, classOf[UserCommands])
  ActorSerializer.register(10001, classOf[UserCommands.NewAuth])
  ActorSerializer.register(10002, classOf[UserCommands.NewAuthAck])
  ActorSerializer.register(10003, classOf[UserCommands.SendMessage])
  ActorSerializer.register(10004, classOf[UserCommands.MessageReceived])
  ActorSerializer.register(10005, classOf[UserCommands.BroadcastUpdate])
  ActorSerializer.register(10006, classOf[UserCommands.BroadcastUpdateResponse])
  ActorSerializer.register(10007, classOf[UserCommands.RemoveAuth])
  ActorSerializer.register(10008, classOf[UserCommands.Create])
  ActorSerializer.register(10009, classOf[UserCommands.MessageRead])
  ActorSerializer.register(10010, classOf[UserCommands.Delete])
  ActorSerializer.register(10011, classOf[UserCommands.ChangeNameAck])
  ActorSerializer.register(10012, classOf[UserCommands.ChangeName])
  ActorSerializer.register(10013, classOf[UserCommands.CreateAck])
  ActorSerializer.register(10014, classOf[UserCommands.ChangeCountryCode])
  ActorSerializer.register(10015, classOf[UserCommands.DeliverMessage])
  ActorSerializer.register(10016, classOf[UserCommands.DeliverOwnMessage])
  ActorSerializer.register(10017, classOf[UserCommands.RemoveAuthAck])
  ActorSerializer.register(10018, classOf[UserCommands.DeleteAck])
  ActorSerializer.register(10019, classOf[UserCommands.AddPhone])
  ActorSerializer.register(10020, classOf[UserCommands.AddPhoneAck])
  ActorSerializer.register(10021, classOf[UserCommands.AddEmail])
  ActorSerializer.register(10022, classOf[UserCommands.AddEmailAck])
  ActorSerializer.register(10023, classOf[UserCommands.ChangeCountryCodeAck])
  ActorSerializer.register(10024, classOf[UserCommands.ChangeNickname])
  ActorSerializer.register(10025, classOf[UserCommands.ChangeAbout])

  ActorSerializer.register(11001, classOf[UserQueries.GetAuthIds])
  ActorSerializer.register(11002, classOf[UserQueries.GetAuthIdsResponse])

  ActorSerializer.register(12001, classOf[UserEvents.AuthAdded])
  ActorSerializer.register(12002, classOf[UserEvents.AuthRemoved])
  ActorSerializer.register(12003, classOf[UserEvents.Created])
  ActorSerializer.register(12004, classOf[UserEvents.MessageReceived])
  ActorSerializer.register(12005, classOf[UserEvents.MessageRead])
  ActorSerializer.register(12006, classOf[UserEvents.Deleted])
  ActorSerializer.register(12007, classOf[UserEvents.NameChanged])
  ActorSerializer.register(12008, classOf[UserEvents.CountryCodeChanged])
  ActorSerializer.register(12009, classOf[UserEvents.PhoneAdded])
  ActorSerializer.register(12010, classOf[UserEvents.EmailAdded])
  ActorSerializer.register(12011, classOf[UserEvents.NicknameChanged])
  ActorSerializer.register(12012, classOf[UserEvents.AboutChanged])

  def props(
    implicit
    db:                  Database,
    seqUpdManagerRegion: SeqUpdatesManagerRegion,
    socialManagerRegion: SocialManagerRegion
  ): Props =
    Props(classOf[UserOfficeActor], db, seqUpdManagerRegion, socialManagerRegion)
}

private[user] final class UserOfficeActor(
  implicit
  protected val db:                  Database,
  protected val seqUpdManagerRegion: SeqUpdatesManagerRegion,
  protected val socialManagerRegion: SocialManagerRegion
) extends PeerOffice with UserCommandHandlers with UserQueriesHandlers with ActorLogging {

  import UserCommands._
  import UserQueries._
  import UserOffice._

  override type OfficeState = User
  override type OfficeEvent = UserEvent

  override protected def workWith(evt: OfficeEvent, user: OfficeState): Unit = context become working(updateState(evt, user))

  private val MaxCacheSize = 100L

  protected implicit val region: UserOfficeRegion = UserOfficeRegion(context.parent)
  protected implicit val timeout: Timeout = Timeout(10.seconds)

  protected implicit val system: ActorSystem = context.system
  protected implicit val ec: ExecutionContext = context.dispatcher

  protected val userId = self.path.name.toInt

  override def persistenceId = persistenceIdFor(userId)

  protected implicit val sendResponseCache: Cache[AuthIdRandomId, Future[SeqStateDate]] =
    createCache[AuthIdRandomId, Future[SeqStateDate]](MaxCacheSize)

  context.setReceiveTimeout(15.minutes)

  override def receiveCommand = creating

  private[this] def creating: Receive = {
    case Create(_, accessSalt, name, countryCode, sex, clientAuthId) ⇒ create(accessSalt, name, countryCode, sex, clientAuthId)
  }

  protected def working(state: User): Receive = {
    case NewAuth(_, authId)                ⇒ addAuth(state, authId)
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
    case ChangeNickname(_, clientAuthId, nickname)       ⇒ changeNickname(state, clientAuthId, nickname)
    case ChangeAbout(_, clientAuthId, about)             ⇒ changeAbout(state, clientAuthId, about)

    case GetAuthIds(_)                                   ⇒ getAuthIds(state)
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
      case UserEvents.NicknameChanged(nickname) ⇒
        user.copy(nickname = nickname)
      case UserEvents.AboutChanged(about) ⇒
        user.copy(about = about)
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
      isDeleted = false,
      nickname = None,
      about = None
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