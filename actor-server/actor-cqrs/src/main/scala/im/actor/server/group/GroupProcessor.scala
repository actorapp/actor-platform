package im.actor.server.group

import im.actor.server.db.DbExtension
import im.actor.server.event.TSEvent

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

import akka.actor._
import akka.contrib.pattern.ShardRegion
import akka.pattern.pipe
import akka.persistence.{ RecoveryCompleted, RecoveryFailure }
import akka.util.Timeout
import com.github.benmanes.caffeine.cache.Cache
import org.joda.time.DateTime
import slick.driver.PostgresDriver.api._

import im.actor.server.commons.serialization.ActorSerializer
import im.actor.server.file.Avatar
import im.actor.server.office.PeerProcessor.MessageSentComplete
import im.actor.server.office.{ ProcessorState, PeerProcessor, StopOffice }
import im.actor.server.push.{ SeqUpdatesExtension, SeqUpdatesManagerRegion }
import im.actor.server.sequence.SeqStateDate
import im.actor.server.user.{ UserExtension, UserViewRegion, UserProcessorRegion }
import im.actor.server.util.{ S3StorageExtension, FileStorageAdapter }
import im.actor.utils.cache.CacheHelpers._

private[group] case class Member(
  userId:        Int,
  inviterUserId: Int,
  invitedAt:     DateTime,
  isAdmin:       Boolean
)

private[group] case class Bot(
  userId: Int,
  token:  String
)

private[group] case class Group(
  id:             Int,
  accessHash:     Long,
  creatorUserId:  Int,
  createdAt:      DateTime,
  members:        Map[Int, Member],
  invitedUserIds: Set[Int],
  title:          String,
  about:          Option[String],
  isPublic:       Boolean,
  bot:            Option[Bot],
  avatar:         Option[Avatar],
  topic:          Option[String]
) extends ProcessorState

trait GroupCommand {
  val groupId: Int
}

trait GroupEvent

trait GroupQuery {
  val groupId: Int
}

object GroupProcessor {

  private case class Initialized(groupUsersIds: Set[Int], invitedUsersIds: Set[Int], isPublic: Boolean)

  def register(): Unit = {
    ActorSerializer.register(20001, classOf[GroupCommands.Create])
    ActorSerializer.register(20002, classOf[GroupCommands.CreateAck])
    ActorSerializer.register(20003, classOf[GroupCommands.Invite])
    ActorSerializer.register(20004, classOf[GroupCommands.Join])
    ActorSerializer.register(20005, classOf[GroupCommands.Kick])
    ActorSerializer.register(20006, classOf[GroupCommands.Leave])
    ActorSerializer.register(20007, classOf[GroupCommands.SendMessage])
    ActorSerializer.register(20008, classOf[GroupCommands.MessageReceived])
    ActorSerializer.register(20009, classOf[GroupCommands.MessageRead])
    ActorSerializer.register(20010, classOf[GroupCommands.UpdateAvatar])
    ActorSerializer.register(20011, classOf[GroupCommands.MakePublic])
    ActorSerializer.register(20012, classOf[GroupCommands.MakePublicAck])
    ActorSerializer.register(20013, classOf[GroupCommands.UpdateTitle])
    ActorSerializer.register(20014, classOf[GroupCommands.UpdateTitle])
    ActorSerializer.register(20015, classOf[GroupCommands.ChangeTopic])
    ActorSerializer.register(20016, classOf[GroupCommands.ChangeAbout])
    ActorSerializer.register(20017, classOf[GroupCommands.MakeUserAdmin])
    ActorSerializer.register(20018, classOf[GroupCommands.RevokeIntegrationToken])
    ActorSerializer.register(20020, classOf[GroupCommands.RevokeIntegrationTokenAck])
    ActorSerializer.register(20021, classOf[GroupCommands.MessageReceivedAck])
    ActorSerializer.register(20022, classOf[GroupCommands.MessageReadAck])

    ActorSerializer.register(21001, classOf[GroupQueries.GetIntegrationToken])
    ActorSerializer.register(21002, classOf[GroupQueries.GetIntegrationTokenResponse])

    ActorSerializer.register(22003, classOf[GroupEvents.UserInvited])
    ActorSerializer.register(22004, classOf[GroupEvents.UserJoined])
    ActorSerializer.register(22005, classOf[GroupEvents.Created])
    ActorSerializer.register(22006, classOf[GroupEvents.BotAdded])
    ActorSerializer.register(22007, classOf[GroupEvents.UserKicked])
    ActorSerializer.register(22008, classOf[GroupEvents.UserLeft])
    ActorSerializer.register(22009, classOf[GroupEvents.AvatarUpdated])
    ActorSerializer.register(22010, classOf[GroupEvents.BecamePublic])
    ActorSerializer.register(22011, classOf[GroupEvents.AboutUpdated])
    ActorSerializer.register(22012, classOf[GroupEvents.TitleUpdated])
    ActorSerializer.register(22013, classOf[GroupEvents.TopicUpdated])
    ActorSerializer.register(22014, classOf[GroupEvents.AboutUpdated])
    ActorSerializer.register(22015, classOf[GroupEvents.UserBecameAdmin])
    ActorSerializer.register(22016, classOf[GroupEvents.IntegrationTokenRevoked])
  }

  def props: Props = Props(classOf[GroupProcessor])
}

private[group] final class GroupProcessor
  extends PeerProcessor[Group, TSEvent]
  with GroupCommandHandlers
  with GroupQueryHandlers
  with ActorLogging
  with Stash
  with GroupsImplicits {

  import GroupCommands._
  import GroupErrors._

  protected implicit val system: ActorSystem = context.system
  protected implicit val ec: ExecutionContext = context.dispatcher

  protected implicit val timeout: Timeout = Timeout(10.seconds)

  protected implicit val db: Database = DbExtension(system).db
  protected implicit val seqUpdatesExt: SeqUpdatesExtension = SeqUpdatesExtension(system)
  protected implicit val groupViewRegion: GroupViewRegion = GroupViewRegion(context.parent)
  protected implicit val userProcessorRegion: UserProcessorRegion = UserExtension(context.system).processorRegion
  protected implicit val userViewRegion: UserViewRegion = UserExtension(context.system).viewRegion
  protected implicit val fileStorageAdapter: FileStorageAdapter = S3StorageExtension(context.system).s3StorageAdapter

  protected val groupId = self.path.name.toInt

  override def persistenceId = GroupOffice.persistenceIdFor(groupId)

  context.setReceiveTimeout(5.hours)

  private val MaxCacheSize = 100L

  protected var lastSenderId: Option[Int] = None

  protected implicit val sendResponseCache: Cache[AuthIdRandomId, Future[SeqStateDate]] =
    createCache[AuthIdRandomId, Future[SeqStateDate]](MaxCacheSize)

  def updatedState(evt: TSEvent, state: Group): Group = {
    evt match {
      case TSEvent(_, GroupEvents.BotAdded(userId, token)) ⇒
        state.copy(bot = Some(Bot(userId, token)))
      case TSEvent(ts, GroupEvents.UserInvited(userId, inviterUserId)) ⇒
        state.copy(
          members = state.members + (userId → Member(userId, inviterUserId, ts, isAdmin = false)),
          invitedUserIds = state.invitedUserIds + userId
        )
      case TSEvent(ts, GroupEvents.UserJoined(userId, inviterUserId)) ⇒
        state.copy(
          members = state.members + (userId → Member(userId, inviterUserId, ts, isAdmin = false)),
          invitedUserIds = state.invitedUserIds - userId
        )
      case TSEvent(_, GroupEvents.UserKicked(userId, kickerUserId, _)) ⇒
        state.copy(members = state.members - userId)
      case TSEvent(_, GroupEvents.UserLeft(userId, _)) ⇒
        state.copy(members = state.members - userId)
      case TSEvent(_, GroupEvents.AvatarUpdated(avatar)) ⇒
        state.copy(avatar = avatar)
      case TSEvent(_, GroupEvents.TitleUpdated(title)) ⇒
        state.copy(title = title)
      case TSEvent(_, GroupEvents.BecamePublic()) ⇒
        state.copy(isPublic = true)
      case TSEvent(_, GroupEvents.AboutUpdated(about)) ⇒
        state.copy(about = about)
      case TSEvent(_, GroupEvents.TopicUpdated(topic)) ⇒
        state.copy(topic = topic)
      case TSEvent(_, GroupEvents.UserBecameAdmin(userId, _)) ⇒
        state.copy(members = state.members.updated(userId, state.members(userId).copy(isAdmin = true)))
      case TSEvent(_, GroupEvents.IntegrationTokenRevoked(token)) ⇒
        state.copy(bot = state.bot.map(_.copy(token = token)))
    }
  }

  override def handleQuery(state: Group): Receive = {
    case GroupQueries.GetIntegrationToken(_, userId) ⇒ getIntegrationToken(state, userId)
    case GroupQueries.GetApiStruct(_, userId)        ⇒ getApiStruct(state, userId)
  }

  override def handleInitCommand: Receive = {
    case Create(_, creatorUserId, creatorAuthId, title, randomId, userIds) ⇒
      create(groupId, creatorUserId, creatorAuthId, title, randomId, userIds.toSet)
  }

  override def handleCommand(state: Group): Receive = {
    case SendMessage(_, senderUserId, senderAuthId, hash, randomId, message, isFat) ⇒
      if (hash == state.accessHash) {
        if (hasMember(state, senderUserId) || isBot(state, senderUserId)) {
          context.become {
            case MessageSentComplete ⇒
              unstashAll()
              context become working(state)
            case msg ⇒
              stash()
          }

          val date = new DateTime
          val replyTo = sender()

          sendMessage(state, senderUserId, senderAuthId, state.members.keySet, randomId, date, message, isFat) onComplete {
            case Success(seqstatedate) ⇒
              replyTo ! seqstatedate
              self ! MessageSentComplete
            case Failure(e) ⇒
              replyTo ! Status.Failure(e)
              log.error(e, "Failed to send message")
              self ! MessageSentComplete
          }
        } else {
          sender() ! Status.Failure(NotAMember)
        }
      } else {
        sender() ! Status.Failure(InvalidAccessHash)
      }
    case MessageReceived(_, receiverUserId, _, date, receivedDate) ⇒
      messageReceived(state, receiverUserId, date, receivedDate)
    case MessageRead(_, readerUserId, readerAuthId, date, readDate) ⇒
      messageRead(state, readerUserId, readerAuthId, date, readDate)
    case Invite(_, inviteeUserId, inviterUserId, inviterAuthId, randomId) ⇒
      if (!hasMember(state, inviteeUserId)) {
        persist(TSEvent(now(), GroupEvents.UserInvited(inviteeUserId, inviterUserId))) { evt ⇒
          workWith(evt, state)

          val replyTo = sender()

          invite(state, inviteeUserId, inviterUserId, inviterAuthId, randomId, evt.ts) pipeTo replyTo onFailure {
            case e ⇒ replyTo ! Status.Failure(e)
          }
        }
      } else {
        sender() ! Status.Failure(GroupErrors.UserAlreadyInvited)
      }
    case Join(_, joiningUserId, joiningUserAuthId, invitingUserId) ⇒
      join(state, joiningUserId, joiningUserAuthId, invitingUserId)
    case Kick(_, kickedUserId, kickerUserId, kickerAuthId, randomId) ⇒
      kick(state, kickedUserId, kickerUserId, kickerAuthId, randomId)
    case Leave(_, userId, authId, randomId) ⇒
      leave(state, userId, authId, randomId)
    case UpdateAvatar(_, clientUserId, clientAuthId, avatarOpt, randomId) ⇒
      updateAvatar(state, clientUserId, clientAuthId, avatarOpt, randomId)
    case UpdateTitle(_, clientUserId, clientAuthId, title, randomId) ⇒
      updateTitle(state, clientUserId, clientAuthId, title, randomId)
    case MakePublic(_, description) ⇒
      makePublic(state, description.getOrElse(""))
    case ChangeTopic(_, clientUserId, clientAuthId, topic, randomId) ⇒
      updateTopic(state, clientUserId, clientAuthId, topic, randomId)
    case ChangeAbout(_, clientUserId, clientAuthId, about, randomId) ⇒
      updateAbout(state, clientUserId, clientAuthId, about, randomId)
    case MakeUserAdmin(_, clientUserId, clientAuthId, candidateId) ⇒
      makeUserAdmin(state, clientUserId, clientAuthId, candidateId)
    case RevokeIntegrationToken(_, userId) ⇒
      revokeIntegrationToken(state, userId)
    case StopOffice     ⇒ context stop self
    case ReceiveTimeout ⇒ context.parent ! ShardRegion.Passivate(stopMessage = StopOffice)
  }

  private[this] var groupStateMaybe: Option[Group] = None

  override def receiveRecover = {
    case TSEvent(ts, created: GroupEvents.Created) ⇒
      groupStateMaybe = Some(initState(ts, created))
    case evt: TSEvent ⇒
      groupStateMaybe = groupStateMaybe map (updatedState(evt, _))
    case RecoveryFailure(e) ⇒
      log.error(e, "Failed to recover")
    case RecoveryCompleted ⇒
      groupStateMaybe match {
        case Some(group) ⇒ context become working(group)
        case None        ⇒ context become initializing
      }
    case unmatched ⇒
      log.error("Unmatched recovery event {}", unmatched)
  }

  protected def initState(ts: DateTime, evt: GroupEvents.Created): Group = {
    Group(
      id = groupId,
      accessHash = evt.accessHash,
      title = evt.title,
      about = None,
      creatorUserId = evt.creatorUserId,
      createdAt = ts,
      members = Map(evt.creatorUserId → Member(evt.creatorUserId, evt.creatorUserId, ts, isAdmin = true)),
      isPublic = false,
      bot = None,
      invitedUserIds = Set.empty,
      avatar = None,
      topic = None
    )
  }

  protected def hasMember(group: Group, userId: Int): Boolean = group.members.keySet.contains(userId)

  protected def isBot(group: Group, userId: Int): Boolean = userId == 0 || (group.bot exists (_.userId == userId))

  protected def isAdmin(group: Group, userId: Int): Boolean = group.members.get(userId) exists (_.isAdmin)
}