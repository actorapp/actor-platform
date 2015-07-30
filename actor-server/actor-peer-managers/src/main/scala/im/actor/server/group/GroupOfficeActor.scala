package im.actor.server.group

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
import im.actor.server.office.PeerOffice.MessageSentComplete
import im.actor.server.office.{ PeerOffice, StopOffice }
import im.actor.server.push.SeqUpdatesManagerRegion
import im.actor.server.sequence.SeqStateDate
import im.actor.server.user.UserOfficeRegion
import im.actor.server.util.FileStorageAdapter
import im.actor.utils.cache.CacheHelpers._

private[group] case class Member(
  userId:        Int,
  inviterUserId: Int,
  invitedAt:     DateTime
)

private[group] case class Bot(
  userId: Int,
  token:  String
)

private[group] case class Group(
  id:               Int,
  accessHash:       Long,
  creatorUserId:    Int,
  createdAt:        DateTime,
  members:          Map[Int, Member],
  invitedUserIds:   Set[Int],
  title:            String,
  description:      String,
  isPublic:         Boolean,
  lastSenderId:     Option[Int],
  lastReceivedDate: Option[DateTime],
  lastReadDate:     Option[DateTime],
  bot:              Option[Bot],
  avatar:           Option[Avatar]
)

trait GroupCommand {
  val groupId: Int
}

trait GroupEvent

private[group] object GroupOfficeActor {

  private case class Initialized(groupUsersIds: Set[Int], invitedUsersIds: Set[Int], isPublic: Boolean)

  ActorSerializer.register(5001, classOf[GroupCommands.Create])
  ActorSerializer.register(5002, classOf[GroupCommands.CreateAck])
  ActorSerializer.register(5003, classOf[GroupCommands.Invite])
  ActorSerializer.register(5004, classOf[GroupCommands.Join])
  ActorSerializer.register(5005, classOf[GroupCommands.Kick])
  ActorSerializer.register(5006, classOf[GroupCommands.Leave])
  ActorSerializer.register(5007, classOf[GroupCommands.SendMessage])
  ActorSerializer.register(5008, classOf[GroupCommands.MessageReceived])
  ActorSerializer.register(5009, classOf[GroupCommands.MessageRead])
  ActorSerializer.register(5010, classOf[GroupCommands.UpdateAvatar])
  ActorSerializer.register(5011, classOf[GroupCommands.MakePublic])
  ActorSerializer.register(5012, classOf[GroupCommands.MakePublicAck])
  ActorSerializer.register(5013, classOf[GroupCommands.UpdateTitle])

  ActorSerializer.register(6001, classOf[GroupEvents.MessageRead])
  ActorSerializer.register(6002, classOf[GroupEvents.MessageReceived])
  ActorSerializer.register(6003, classOf[GroupEvents.UserInvited])
  ActorSerializer.register(6004, classOf[GroupEvents.UserJoined])
  ActorSerializer.register(6005, classOf[GroupEvents.Created])
  ActorSerializer.register(6006, classOf[GroupEvents.BotAdded])
  ActorSerializer.register(6007, classOf[GroupEvents.UserKicked])
  ActorSerializer.register(6008, classOf[GroupEvents.UserLeft])
  ActorSerializer.register(6009, classOf[GroupEvents.AvatarUpdated])
  ActorSerializer.register(6010, classOf[GroupEvents.BecamePublic])
  ActorSerializer.register(6011, classOf[GroupEvents.DescriptionUpdated])
  ActorSerializer.register(6012, classOf[GroupEvents.TitleUpdated])

  def props(
    implicit
    db:                  Database,
    seqUpdManagerRegion: SeqUpdatesManagerRegion,
    userOfficeRegion:    UserOfficeRegion,
    fsAdapter:           FileStorageAdapter
  ): Props = Props(classOf[GroupOfficeActor], db, seqUpdManagerRegion, userOfficeRegion, fsAdapter)
}

private[group] final class GroupOfficeActor(
  implicit
  protected val db:                  Database,
  protected val seqUpdManagerRegion: SeqUpdatesManagerRegion,
  protected val userOfficeRegion:    UserOfficeRegion,
  protected val fsAdapter:           FileStorageAdapter
) extends PeerOffice with GroupCommandHandlers with ActorLogging with Stash with GroupsImplicits {

  import GroupCommands._
  import GroupErrors._

  implicit protected val system: ActorSystem = context.system
  implicit protected val ec: ExecutionContext = context.dispatcher

  implicit protected val timeout: Timeout = Timeout(10.seconds)

  protected val groupId = self.path.name.toInt

  override def persistenceId = s"group_$groupId"

  override type OfficeEvent = GroupEvent
  override type OfficeState = Group

  context.setReceiveTimeout(15.minutes)

  private val MaxCacheSize = 100L

  implicit val sendResponseCache: Cache[AuthIdRandomId, Future[SeqStateDate]] =
    createCache[AuthIdRandomId, Future[SeqStateDate]](MaxCacheSize)

  def receiveCommand = creating

  def creating: Receive = {
    case Create(_, creatorUserId, creatorAuthId, title, randomId, userIds) ⇒
      create(groupId, creatorUserId, creatorAuthId, title, randomId, userIds.toSet)
  }

  def working(group: Group): Receive = {
    case SendMessage(_, senderUserId, senderAuthId, hash, randomId, message, isFat) ⇒
      if (hash == group.accessHash) {
        if (hasMember(group, senderUserId) || isBot(group, senderUserId)) {
          context.become {
            case MessageSentComplete ⇒
              unstashAll()
              context become working(group)
            case msg ⇒
              stash()
          }

          val date = new DateTime
          val replyTo = sender()

          sendMessage(group, senderUserId, senderAuthId, group.members.keySet, randomId, date, message, isFat) onComplete {
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
      messageReceived(group, receiverUserId, date, receivedDate)
    case MessageRead(_, readerUserId, readerAuthId, date, readDate) ⇒
      messageRead(group, readerUserId, readerAuthId, date, readDate)
    case Invite(_, inviteeUserId, inviterUserId, inviterAuthId, randomId) ⇒
      if (!hasMember(group, inviteeUserId)) {
        val dateMillis = System.currentTimeMillis()

        persist(GroupEvents.UserInvited(inviteeUserId, inviterUserId, dateMillis)) { evt ⇒
          context become working(updateState(evt, group))

          val replyTo = sender()
          val date = new DateTime(dateMillis)

          invite(group, inviteeUserId, inviterUserId, inviterAuthId, randomId, date) pipeTo replyTo onFailure {
            case e ⇒ replyTo ! Status.Failure(e)
          }
        }
      } else {
        sender() ! Status.Failure(GroupErrors.UserAlreadyInvited)
      }
    case Join(_, joiningUserId, joiningUserAuthId, invitingUserId) ⇒
      join(group, joiningUserId, joiningUserAuthId, invitingUserId)
    case Kick(_, kickedUserId, kickerUserId, kickerAuthId, randomId) ⇒
      kick(group, kickedUserId, kickerUserId, kickerAuthId, randomId)
    case Leave(_, userId, authId, randomId) ⇒
      leave(group, userId, authId, randomId)
    case UpdateAvatar(_, clientUserId, clientAuthId, avatarOpt, randomId) ⇒
      updateAvatar(group, clientUserId, clientAuthId, avatarOpt, randomId)
    case UpdateTitle(_, clientUserId, clientAuthId, title, randomId) ⇒
      updateTitle(group, clientUserId, clientAuthId, title, randomId)
    case MakePublic(_, description) ⇒
      makePublic(group, description.getOrElse(""))
    case StopOffice     ⇒ context stop self
    case ReceiveTimeout ⇒ context.parent ! ShardRegion.Passivate(stopMessage = StopOffice)
  }

  private[this] var groupStateMaybe: Option[Group] = None

  override def receiveRecover = {
    case evt: GroupEvents.Created ⇒
      groupStateMaybe = Some(initState(evt))
    case evt: GroupEvent ⇒
      groupStateMaybe = groupStateMaybe map (updateState(evt, _))
    case RecoveryFailure(e) ⇒
      log.error(e, "Failed to recover")
    case RecoveryCompleted ⇒
      groupStateMaybe match {
        case Some(group) ⇒ context become working(group)
        case None        ⇒ context become creating
      }
    case unmatched ⇒
      log.error("Unmatched recovery event {}", unmatched)
  }

  protected def initState(evt: GroupEvents.Created): Group = {
    Group(
      id = groupId,
      accessHash = evt.accessHash,
      title = evt.title,
      description = "",
      creatorUserId = evt.creatorUserId,
      createdAt = evt.createdAt,
      members = Map(evt.creatorUserId → Member(evt.creatorUserId, evt.creatorUserId, evt.createdAt)),
      isPublic = false,
      lastSenderId = None,
      lastReceivedDate = None,
      lastReadDate = None,
      bot = None,
      invitedUserIds = Set.empty,
      avatar = None
    )
  }

  override protected def workWith(e: GroupEvent, group: Group): Unit = context become working(updateState(e, group))

  override protected def updateState(evt: GroupEvent, state: Group): Group = {
    evt match {
      case GroupEvents.BotAdded(userId, token) ⇒
        state.copy(bot = Some(Bot(userId, token)))
      case GroupEvents.MessageReceived(date) ⇒
        state.copy(lastReceivedDate = Some(new DateTime(date)))
      case GroupEvents.MessageRead(userId, date) ⇒
        state.copy(
          lastReadDate = Some(new DateTime(date)),
          invitedUserIds = state.invitedUserIds - userId
        )
      case GroupEvents.UserInvited(userId, inviterUserId, invitedAt) ⇒
        state.copy(
          members = state.members + (userId → Member(userId, inviterUserId, new DateTime(invitedAt))),
          invitedUserIds = state.invitedUserIds + userId
        )
      case GroupEvents.UserJoined(userId, inviterUserId, invitedAt) ⇒
        state.copy(
          members = state.members + (userId → Member(userId, inviterUserId, new DateTime(invitedAt)))
        )
      case GroupEvents.UserKicked(userId, kickerUserId, _) ⇒
        state.copy(members = state.members - userId)
      case GroupEvents.UserLeft(userId, _) ⇒
        state.copy(members = state.members - userId)
      case GroupEvents.AvatarUpdated(avatar) ⇒
        state.copy(avatar = avatar)
      case GroupEvents.TitleUpdated(title) ⇒
        state.copy(title = title)
      case GroupEvents.BecamePublic() ⇒
        state.copy(isPublic = true)
      case GroupEvents.DescriptionUpdated(desc) ⇒
        state.copy(description = desc)
    }
  }

  protected def hasMember(group: Group, userId: Int): Boolean = group.members.keySet.contains(userId)

  protected def isBot(group: Group, userId: Int): Boolean = group.bot exists (_.userId == userId)
}