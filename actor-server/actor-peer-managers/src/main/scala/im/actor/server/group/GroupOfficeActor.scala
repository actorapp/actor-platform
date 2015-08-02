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
  invitedAt:     DateTime,
  isAdmin:       Boolean
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
  about:            Option[String],
  isPublic:         Boolean,
  lastSenderId:     Option[Int],
  lastReceivedDate: Option[DateTime],
  lastReadDate:     Option[DateTime],
  bot:              Option[Bot],
  avatar:           Option[Avatar],
  topic:            Option[String]
) {
  private[group] def updated(evt: GroupEvent): Group = {
    evt match {
      case GroupEvents.BotAdded(userId, token) ⇒
        this.copy(bot = Some(Bot(userId, token)))
      case GroupEvents.MessageReceived(date) ⇒
        this.copy(lastReceivedDate = Some(new DateTime(date)))
      case GroupEvents.MessageRead(userId, date) ⇒
        this.copy(
          lastReadDate = Some(new DateTime(date)),
          invitedUserIds = this.invitedUserIds - userId
        )
      case GroupEvents.UserInvited(userId, inviterUserId, invitedAt) ⇒
        this.copy(
          members = this.members + (userId → Member(userId, inviterUserId, new DateTime(invitedAt), isAdmin = false)),
          invitedUserIds = this.invitedUserIds + userId
        )
      case GroupEvents.UserJoined(userId, inviterUserId, invitedAt) ⇒
        this.copy(
          members = this.members + (userId → Member(userId, inviterUserId, new DateTime(invitedAt), isAdmin = false))
        )
      case GroupEvents.UserKicked(userId, kickerUserId, _) ⇒
        this.copy(members = this.members - userId)
      case GroupEvents.UserLeft(userId, _) ⇒
        this.copy(members = this.members - userId)
      case GroupEvents.AvatarUpdated(avatar) ⇒
        this.copy(avatar = avatar)
      case GroupEvents.TitleUpdated(title) ⇒
        this.copy(title = title)
      case GroupEvents.BecamePublic() ⇒
        this.copy(isPublic = true)
      case GroupEvents.AboutUpdated(about) ⇒
        this.copy(about = about)
      case GroupEvents.TopicUpdated(topic) ⇒
        this.copy(topic = topic)
      case GroupEvents.UserBecameAdmin(userId) ⇒
        this.copy(members = this.members.updated(userId, this.members(userId).copy(isAdmin = true)))
    }
  }
}

trait GroupCommand {
  val groupId: Int
}

trait GroupEvent

private[group] object GroupOfficeActor {

  private case class Initialized(groupUsersIds: Set[Int], invitedUsersIds: Set[Int], isPublic: Boolean)

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

  ActorSerializer.register(22001, classOf[GroupEvents.MessageRead])
  ActorSerializer.register(22002, classOf[GroupEvents.MessageReceived])
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
    case Invite(_, inviteeUserId, inviterUserId, inviterAuthId, randomId) ⇒ //isAdmin should be false here
      if (!hasMember(group, inviteeUserId)) {
        val dateMillis = System.currentTimeMillis()

        persist(GroupEvents.UserInvited(inviteeUserId, inviterUserId, dateMillis)) { evt ⇒
          context become working(group.updated(evt))

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
    case ChangeTopic(_, clientUserId, clientAuthId, topic, randomId) ⇒
      updateTopic(group, clientUserId, clientAuthId, topic, randomId)
    case ChangeAbout(_, clientUserId, clientAuthId, about, randomId) ⇒
      updateAbout(group, clientUserId, clientAuthId, about, randomId)
    case MakeUserAdmin(_, clientUserId, clientAuthId, candidateId) ⇒
      makeUserAdmin(group, clientUserId, clientAuthId, candidateId)
    case StopOffice     ⇒ context stop self
    case ReceiveTimeout ⇒ context.parent ! ShardRegion.Passivate(stopMessage = StopOffice)
  }

  private[this] var groupStateMaybe: Option[Group] = None

  override def receiveRecover = {
    case evt: GroupEvents.Created ⇒
      groupStateMaybe = Some(initState(evt))
    case evt: GroupEvent ⇒
      groupStateMaybe = groupStateMaybe map (_.updated(evt))
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
      about = None,
      creatorUserId = evt.creatorUserId,
      createdAt = evt.createdAt,
      members = Map(evt.creatorUserId → Member(evt.creatorUserId, evt.creatorUserId, evt.createdAt, isAdmin = true)),
      isPublic = false,
      lastSenderId = None,
      lastReceivedDate = None,
      lastReadDate = None,
      bot = None,
      invitedUserIds = Set.empty,
      avatar = None,
      topic = None
    )
  }

  override protected def workWith(e: GroupEvent, group: Group): Unit = context become working(group.updated(e))

  protected def hasMember(group: Group, userId: Int): Boolean = group.members.keySet.contains(userId)

  protected def isBot(group: Group, userId: Int): Boolean = group.bot exists (_.userId == userId)
}