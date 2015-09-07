package im.actor.server.group

import akka.actor._
import akka.contrib.pattern.ShardRegion
import akka.pattern.pipe
import akka.persistence.{ RecoveryCompleted, RecoveryFailure }
import akka.util.Timeout
import im.actor.api.rpc.misc.Extension
import im.actor.server.commons.KeyValueMappings
import im.actor.server.commons.serialization.ActorSerializer
import im.actor.server.db.DbExtension
import im.actor.server.event.TSEvent
import im.actor.server.file.{ FileStorageAdapter, S3StorageExtension, Avatar }
import im.actor.server.office.{ PeerProcessor, ProcessorState, StopOffice }
import im.actor.server.dialog.group.GroupDialogExtension
import im.actor.server.dialog.group.GroupDialogRegion
import im.actor.server.sequence.SeqUpdatesExtension
import im.actor.server.user.{ UserExtension, UserProcessorRegion, UserViewRegion }
import org.joda.time.DateTime
import shardakka.{ IntCodec, ShardakkaExtension }
import slick.driver.PostgresDriver.api._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

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
  id:              Int,
  typ:             GroupType,
  accessHash:      Long,
  creatorUserId:   Int,
  createdAt:       DateTime,
  members:         Map[Int, Member],
  invitedUserIds:  Set[Int],
  title:           String,
  about:           Option[String],
  bot:             Option[Bot],
  avatar:          Option[Avatar],
  topic:           Option[String],
  isHidden:        Boolean,
  isHistoryShared: Boolean,
  extensions:      Seq[Extension]
) extends ProcessorState

trait GroupCommand {
  val groupId: Int
}

trait GroupEvent

trait GroupQuery {
  val groupId: Int
}

object GroupProcessor {

  def register(): Unit =
    ActorSerializer.register(
      20001 → classOf[GroupCommands.Create],
      20002 → classOf[GroupCommands.CreateAck],
      20003 → classOf[GroupCommands.Invite],
      20004 → classOf[GroupCommands.Join],
      20005 → classOf[GroupCommands.Kick],
      20006 → classOf[GroupCommands.Leave],
      20010 → classOf[GroupCommands.UpdateAvatar],
      20011 → classOf[GroupCommands.MakePublic],
      20012 → classOf[GroupCommands.MakePublicAck],
      20013 → classOf[GroupCommands.UpdateTitle],
      20015 → classOf[GroupCommands.ChangeTopic],
      20016 → classOf[GroupCommands.ChangeAbout],
      20017 → classOf[GroupCommands.MakeUserAdmin],
      20018 → classOf[GroupCommands.RevokeIntegrationToken],
      20020 → classOf[GroupCommands.RevokeIntegrationTokenAck],
      20023 → classOf[GroupCommands.JoinAfterFirstRead],
      20024 → classOf[GroupCommands.CreateInternal],
      20025 → classOf[GroupCommands.CreateInternalAck],

      21001 → classOf[GroupQueries.GetIntegrationToken],
      21002 → classOf[GroupQueries.GetIntegrationTokenResponse],
      21003 → classOf[GroupQueries.CheckAccessHash],
      21004 → classOf[GroupQueries.CheckAccessHashResponse],
      21005 → classOf[GroupQueries.GetMembers],
      21006 → classOf[GroupQueries.GetMembersResponse],
      21007 → classOf[GroupQueries.GetApiStruct],
      21008 → classOf[GroupQueries.GetApiStructResponse],
      21009 → classOf[GroupQueries.IsPublic],
      21010 → classOf[GroupQueries.IsPublicResponse],
      21011 → classOf[GroupQueries.GetIntegrationTokenInternal],
      21012 → classOf[GroupQueries.GetAccessHash],
      21013 → classOf[GroupQueries.GetAccessHashResponse],
      21014 → classOf[GroupQueries.IsHistoryShared],
      21015 → classOf[GroupQueries.IsHistorySharedResponse],

      22003 → classOf[GroupEvents.UserInvited],
      22004 → classOf[GroupEvents.UserJoined],
      22005 → classOf[GroupEvents.Created],
      22006 → classOf[GroupEvents.BotAdded],
      22007 → classOf[GroupEvents.UserKicked],
      22008 → classOf[GroupEvents.UserLeft],
      22009 → classOf[GroupEvents.AvatarUpdated],
      22010 → classOf[GroupEvents.BecamePublic],
      22011 → classOf[GroupEvents.AboutUpdated],
      22012 → classOf[GroupEvents.TitleUpdated],
      22013 → classOf[GroupEvents.TopicUpdated],
      22015 → classOf[GroupEvents.UserBecameAdmin],
      22016 → classOf[GroupEvents.IntegrationTokenRevoked]
    )

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

  protected implicit val system: ActorSystem = context.system
  protected implicit val ec: ExecutionContext = context.dispatcher

  protected implicit val timeout: Timeout = Timeout(10.seconds)

  protected implicit val db: Database = DbExtension(system).db
  protected implicit val seqUpdatesExt: SeqUpdatesExtension = SeqUpdatesExtension(system)
  protected implicit val groupViewRegion: GroupViewRegion = GroupViewRegion(context.parent)
  protected implicit val userProcessorRegion: UserProcessorRegion = UserExtension(context.system).processorRegion
  protected implicit val userViewRegion: UserViewRegion = UserExtension(context.system).viewRegion
  protected implicit val fileStorageAdapter: FileStorageAdapter = S3StorageExtension(context.system).s3StorageAdapter

  protected val integrationTokensKv = ShardakkaExtension(system).simpleKeyValue[Int](KeyValueMappings.IntegrationTokens, IntCodec)

  //Declared lazy because of cyclic dependency between GroupDialogRegion and GroupProcessorRegion.
  //It lead to problems with initialization of extensions.
  //Such bugs are hard to catch. One should avoid such behaviour
  lazy protected implicit val groupDialogRegion: GroupDialogRegion = GroupDialogExtension(system).region

  protected val groupId = self.path.name.toInt

  override def persistenceId = GroupOffice.persistenceIdFor(groupId)

  context.setReceiveTimeout(5.hours)

  def updatedState(evt: TSEvent, state: Group): Group = {
    evt match {
      case TSEvent(_, GroupEvents.BotAdded(userId, token)) ⇒
        state.copy(bot = Some(Bot(userId, token)))
      case TSEvent(ts, GroupEvents.UserInvited(userId, inviterUserId)) ⇒
        state.copy(
          members = state.members + (userId → Member(userId, inviterUserId, ts, isAdmin = userId == state.creatorUserId)),
          invitedUserIds = state.invitedUserIds + userId
        )
      case TSEvent(ts, GroupEvents.UserJoined(userId, inviterUserId)) ⇒
        state.copy(
          members = state.members + (userId → Member(userId, inviterUserId, ts, isAdmin = userId == state.creatorUserId)),
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
        state.copy(typ = GroupType.Public, isHistoryShared = true)
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
    case GroupQueries.GetIntegrationTokenInternal(_) ⇒ getIntegrationToken(state)
    case GroupQueries.GetApiStruct(_, userId)        ⇒ getApiStruct(state, userId)
    case GroupQueries.CheckAccessHash(_, accessHash) ⇒ checkAccessHash(state, accessHash)
    case GroupQueries.GetMembers(_)                  ⇒ getMembers(state)
    case GroupQueries.IsPublic(_)                    ⇒ isPublic(state)
    case GroupQueries.GetAccessHash(_)               ⇒ getAccessHash(state)
    case GroupQueries.IsHistoryShared(_)             ⇒ isHistoryShared(state)
  }

  override def handleInitCommand: Receive = {
    case Create(_, typ, creatorUserId, creatorAuthId, title, randomId, userIds) ⇒
      create(groupId, typ, creatorUserId, creatorAuthId, title, randomId, userIds.toSet)
    case CreateInternal(_, typ, creatorUserId, title, userIds, isHidden, isHistoryShared, extensions) ⇒
      createInternal(typ, creatorUserId, title, userIds, isHidden, isHistoryShared, extensions)
  }

  override def handleCommand(state: Group): Receive = {
    case Invite(_, inviteeUserId, inviterUserId, inviterAuthId, randomId) ⇒
      if (!hasMember(state, inviteeUserId)) {
        persist(TSEvent(now(), GroupEvents.UserInvited(inviteeUserId, inviterUserId))) { evt ⇒
          context become working(updatedState(evt, state))

          val replyTo = sender()

          invite(state, inviteeUserId, inviterUserId, inviterAuthId, randomId, evt.ts) pipeTo replyTo onFailure {
            case e ⇒ replyTo ! Status.Failure(e)
          }
        }
      } else {
        sender() ! Status.Failure(GroupErrors.UserAlreadyInvited)
      }
    case JoinAfterFirstRead(_, joiningUserId, joiningUserAuthId) ⇒
      val invitingUserId = state.members.get(joiningUserId).map(_.inviterUserId) getOrElse state.creatorUserId
      setJoined(state, joiningUserId, joiningUserAuthId, invitingUserId)
    case Join(_, joiningUserId, joiningUserAuthId, invitingUserId) ⇒
      setJoined(state, joiningUserId, joiningUserAuthId, invitingUserId)
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
      typ = evt.typ.getOrElse(GroupType.General),
      accessHash = evt.accessHash,
      title = evt.title,
      about = None,
      creatorUserId = evt.creatorUserId,
      createdAt = ts,
      members = (evt.userIds map (userId ⇒ (userId → Member(userId, evt.creatorUserId, ts, isAdmin = (userId == evt.creatorUserId))))).toMap,
      bot = None,
      invitedUserIds = evt.userIds.filterNot(_ == evt.creatorUserId).toSet,
      avatar = None,
      topic = None,
      isHidden = evt.isHidden.getOrElse(false),
      isHistoryShared = evt.isHistoryShared.getOrElse(false),
      extensions = evt.extensions
    )
  }

  protected def hasMember(group: Group, userId: Int): Boolean = group.members.keySet.contains(userId)

  protected def isInvited(group: Group, userId: Int): Boolean = group.invitedUserIds.contains(userId)

  protected def isBot(group: Group, userId: Int): Boolean = userId == 0 || (group.bot exists (_.userId == userId))

  protected def isAdmin(group: Group, userId: Int): Boolean = group.members.get(userId) exists (_.isAdmin)
}