package im.actor.server.user

import im.actor.serialization.ActorSerializer

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

import akka.actor._
import akka.contrib.pattern.ShardRegion
import akka.persistence.{ RecoveryCompleted, RecoveryFailure }
import akka.util.Timeout
import com.github.benmanes.caffeine.cache.Cache
import org.joda.time.DateTime
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc.users.ApiSex
import im.actor.server.db.DbExtension
import im.actor.server.event.TSEvent
import im.actor.server.file.Avatar
import im.actor.server.office.{ ProcessorState, PeerProcessor, StopOffice }
import im.actor.server.sequence.SeqUpdatesExtension
import im.actor.server.sequence.SeqStateDate
import im.actor.server.social.{ SocialExtension, SocialManagerRegion }
import im.actor.util.cache.CacheHelpers._

trait UserEvent

trait UserCommand {
  val userId: Int
}

trait UserQuery {
  val userId: Int
}

private[user] case class User(
  id:          Int,
  accessSalt:  String,
  name:        String,
  countryCode: String,
  sex:         ApiSex.ApiSex,
  phones:      Seq[Long],
  emails:      Seq[String],
  authIds:     Set[Long],
  isDeleted:   Boolean,
  isBot:       Boolean,
  nickname:    Option[String],
  about:       Option[String],
  avatar:      Option[Avatar],
  createdAt:   DateTime
) extends ProcessorState

private[user] object User {
  def apply(ts: DateTime, e: UserEvents.Created): User =
    User(
      id = e.userId,
      accessSalt = e.accessSalt,
      name = e.name,
      countryCode = e.countryCode,
      sex = e.sex,
      phones = Seq.empty[Long],
      emails = Seq.empty[String],
      authIds = Set.empty[Long],
      isDeleted = false,
      isBot = e.isBot,
      nickname = None,
      about = None,
      avatar = None,
      createdAt = ts
    )
}

object UserProcessor {
  def register(): Unit =
    ActorSerializer.register(
      10001 → classOf[UserCommands.NewAuth],
      10002 → classOf[UserCommands.NewAuthAck],
      10005 → classOf[UserCommands.BroadcastUpdate],
      10006 → classOf[UserCommands.BroadcastUpdateResponse],
      10007 → classOf[UserCommands.RemoveAuth],
      10008 → classOf[UserCommands.Create],
      10010 → classOf[UserCommands.Delete],
      10012 → classOf[UserCommands.ChangeName],
      10013 → classOf[UserCommands.CreateAck],
      10014 → classOf[UserCommands.ChangeCountryCode],
      10015 → classOf[UserCommands.DeliverMessage],
      10016 → classOf[UserCommands.DeliverOwnMessage],
      10017 → classOf[UserCommands.RemoveAuthAck],
      10018 → classOf[UserCommands.DeleteAck],
      10019 → classOf[UserCommands.AddPhone],
      10020 → classOf[UserCommands.AddPhoneAck],
      10021 → classOf[UserCommands.AddEmail],
      10022 → classOf[UserCommands.AddEmailAck],
      10023 → classOf[UserCommands.ChangeCountryCodeAck],
      10024 → classOf[UserCommands.ChangeNickname],
      10025 → classOf[UserCommands.ChangeAbout],
      10026 → classOf[UserCommands.UpdateAvatar],
      10027 → classOf[UserCommands.UpdateAvatarAck],
      10028 → classOf[UserCommands.DeliverMessageAck],

      11001 → classOf[UserQueries.GetAuthIds],
      11002 → classOf[UserQueries.GetAuthIdsResponse],
      11003 → classOf[UserQueries.GetContactRecords],
      11004 → classOf[UserQueries.GetContactRecordsResponse],
      11005 → classOf[UserQueries.CheckAccessHash],
      11006 → classOf[UserQueries.CheckAccessHashResponse],
      11007 → classOf[UserQueries.GetApiStruct],
      11008 → classOf[UserQueries.GetApiStructResponse],
      11009 → classOf[UserQueries.GetAccessHash],
      11010 → classOf[UserQueries.GetAccessHashResponse],

      12001 → classOf[UserEvents.AuthAdded],
      12002 → classOf[UserEvents.AuthRemoved],
      12003 → classOf[UserEvents.Created],
      12006 → classOf[UserEvents.Deleted],
      12007 → classOf[UserEvents.NameChanged],
      12008 → classOf[UserEvents.CountryCodeChanged],
      12009 → classOf[UserEvents.PhoneAdded],
      12010 → classOf[UserEvents.EmailAdded],
      12011 → classOf[UserEvents.NicknameChanged],
      12012 → classOf[UserEvents.AboutChanged],
      12013 → classOf[UserEvents.AvatarUpdated]
    )

  def props: Props =
    Props(classOf[UserProcessor])
}

private[user] final class UserProcessor
  extends PeerProcessor[User, TSEvent]
  with UserCommandHandlers
  with UserQueriesHandlers
  with ActorLogging {

  import UserCommands._
  import UserOffice._
  import UserQueries._

  private val MaxCacheSize = 100L

  protected implicit val db: Database = DbExtension(context.system).db
  protected implicit val seqUpdatesExt: SeqUpdatesExtension = SeqUpdatesExtension(context.system)
  protected implicit val region: UserProcessorRegion = UserProcessorRegion.get(context.system)
  protected implicit val viewRegion: UserViewRegion = UserViewRegion(context.parent)
  protected implicit val socialRegion: SocialManagerRegion = SocialExtension(context.system).region

  protected implicit val timeout: Timeout = Timeout(10.seconds)

  protected implicit val system: ActorSystem = context.system
  protected implicit val ec: ExecutionContext = context.dispatcher

  protected val userId = self.path.name.toInt

  override def persistenceId = persistenceIdFor(userId)

  context.setReceiveTimeout(1.hour)

  override def updatedState(evt: TSEvent, state: User): User = {
    evt match {
      case TSEvent(_, UserEvents.AuthAdded(authId)) ⇒
        state.copy(authIds = state.authIds + authId)
      case TSEvent(_, UserEvents.AuthRemoved(authId)) ⇒
        state.copy(authIds = state.authIds - authId)
      case TSEvent(_, UserEvents.CountryCodeChanged(countryCode)) ⇒
        state.copy(countryCode = countryCode)
      case TSEvent(_, UserEvents.NameChanged(name)) ⇒
        state.copy(name = name)
      case TSEvent(_, UserEvents.PhoneAdded(phone)) ⇒
        state.copy(phones = state.phones :+ phone)
      case TSEvent(_, UserEvents.EmailAdded(email)) ⇒
        state.copy(emails = state.emails :+ email)
      case TSEvent(_, UserEvents.Deleted()) ⇒
        state.copy(isDeleted = true)
      case TSEvent(_, UserEvents.NicknameChanged(nickname)) ⇒
        state.copy(nickname = nickname)
      case TSEvent(_, UserEvents.AboutChanged(about)) ⇒
        state.copy(about = about)
      case TSEvent(_, UserEvents.AvatarUpdated(avatar)) ⇒
        state.copy(avatar = avatar)
      case TSEvent(_, _: UserEvents.Created) ⇒ state
    }
  }

  override protected def handleInitCommand: Receive = {
    case Create(_, accessSalt, name, countryCode, sex, isBot) ⇒
      create(accessSalt, name, countryCode, sex, isBot)
  }

  override protected def handleCommand(state: User): Receive = {
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
    case ChangeNickname(_, clientAuthId, nickname) ⇒ changeNickname(state, clientAuthId, nickname)
    case ChangeAbout(_, clientAuthId, about)       ⇒ changeAbout(state, clientAuthId, about)
    case UpdateAvatar(_, clientAuthId, avatarOpt)  ⇒ updateAvatar(state, clientAuthId, avatarOpt)
    case StopOffice                                ⇒ context stop self
    case ReceiveTimeout                            ⇒ context.parent ! ShardRegion.Passivate(stopMessage = StopOffice)
  }

  override protected def handleQuery(state: User): Receive = {
    case GetAuthIds(_)                                ⇒ getAuthIds(state)
    case GetApiStruct(_, clientUserId, clientAuthId)  ⇒ getApiStruct(state, clientUserId, clientAuthId)
    case GetContactRecords(_)                         ⇒ getContactRecords(state)
    case CheckAccessHash(_, senderAuthId, accessHash) ⇒ checkAccessHash(state, senderAuthId, accessHash)
    case GetAccessHash(_, clientAuthId)               ⇒ getAccessHash(state, clientAuthId)
  }

  protected[this] var userStateMaybe: Option[User] = None

  override def receiveRecover: Receive = {
    case TSEvent(ts, evt: UserEvents.Created) ⇒
      userStateMaybe = Some(User(ts, evt))
    case evt: TSEvent ⇒
      userStateMaybe = userStateMaybe map (updatedState(evt, _))
    case RecoveryFailure(e) ⇒
      log.error(e, "Failed to recover")
    case RecoveryCompleted ⇒
      userStateMaybe match {
        case Some(state) ⇒
          context become working(state)
        case None ⇒
          context become initializing
      }
    case unmatched ⇒
      log.error("Unmatched recovery event {}", unmatched)
  }

}