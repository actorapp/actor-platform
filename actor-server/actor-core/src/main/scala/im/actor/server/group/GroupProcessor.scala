package im.actor.server.group

import java.time.Instant

import akka.actor._
import akka.cluster.sharding.ShardRegion
import akka.pattern.pipe
import akka.persistence.RecoveryCompleted
import akka.stream.{ ActorMaterializer, ActorMaterializerSettings, Materializer }
import akka.util.Timeout
import im.actor.api.rpc.collections.ApiMapValue
import im.actor.api.rpc.groups.ApiMember
import im.actor.serialization.ActorSerializer
import im.actor.server.KeyValueMappings
import im.actor.server.cqrs.TaggedEvent
import im.actor.server.db.DbExtension
import im.actor.server.dialog.{ DialogEnvelope, DialogExtension, DirectDialogCommand }
import im.actor.server.file.{ Avatar, FileStorageAdapter, FileStorageExtension }
import im.actor.server.model.Group
import im.actor.server.office.{ PeerProcessor, ProcessorState, StopOffice }
import im.actor.server.sequence.SeqUpdatesExtension
import im.actor.server.user.UserExtension
import shardakka.{ IntCodec, ShardakkaExtension }
import slick.driver.PostgresDriver.api._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

private[group] case class Member(
  userId:        Int,
  inviterUserId: Int,
  invitedAt:     Instant,
  isAdmin:       Boolean
)

private[group] case class Bot(
  userId: Int,
  token:  String
)

private[group] case class GroupState(
  id:              Int,
  typ:             GroupType,
  accessHash:      Long,
  creatorUserId:   Int,
  ownerUserId:     Int,
  createdAt:       Instant,
  members:         Map[Int, Member],
  invitedUserIds:  Set[Int],
  title:           String,
  about:           Option[String],
  bot:             Option[Bot],
  avatar:          Option[Avatar],
  topic:           Option[String],
  isHidden:        Boolean,
  isHistoryShared: Boolean,
  extensions:      Seq[ApiMapValue]
) extends ProcessorState

trait GroupCommand {
  val groupId: Int
}

trait GroupEvent extends TaggedEvent {
  val ts: Instant

  override def tags: Set[String] = Set("group")
}

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
      22016 → classOf[GroupEvents.IntegrationTokenRevoked],
      22017 → classOf[GroupEvents.OwnerChanged]
    )

  def props: Props = Props(classOf[GroupProcessor])
}

private[group] final class GroupProcessor
  extends PeerProcessor[GroupState, GroupEvent]
  with GroupCommandHandlers
  with GroupQueryHandlers
  with ActorLogging
  with Stash
  with GroupsImplicits {

  import GroupCommands._

  protected implicit val system: ActorSystem = context.system
  protected implicit val ec: ExecutionContext = context.dispatcher
  protected implicit val mat: Materializer = ActorMaterializer(ActorMaterializerSettings(system))

  protected implicit val timeout: Timeout = Timeout(10.seconds)

  protected val db: Database = DbExtension(system).db
  protected val userExt = UserExtension(system)
  protected lazy val dialogExt = DialogExtension(system)
  protected implicit val fileStorageAdapter: FileStorageAdapter = FileStorageExtension(context.system).fsAdapter
  protected val seqUpdExt = SeqUpdatesExtension(system)

  protected val integrationTokensKv = ShardakkaExtension(system).simpleKeyValue[Int](KeyValueMappings.IntegrationTokens, IntCodec)

  protected val groupId = self.path.name.toInt
  override protected val notFoundError = GroupErrors.GroupNotFound(groupId)

  override def persistenceId = GroupOffice.persistenceIdFor(groupId)

  context.setReceiveTimeout(5.hours)

  def updatedState(evt: GroupEvent, state: GroupState): GroupState = {
    evt match {
      case GroupEvents.BotAdded(_, userId, token) ⇒
        state.copy(bot = Some(Bot(userId, token)))
      case GroupEvents.UserInvited(ts, userId, inviterUserId) ⇒
        state.copy(
          members = state.members + (userId → Member(userId, inviterUserId, ts, isAdmin = userId == state.creatorUserId)),
          invitedUserIds = state.invitedUserIds + userId
        )
      case GroupEvents.UserJoined(ts, userId, inviterUserId) ⇒
        state.copy(
          members = state.members + (userId → Member(userId, inviterUserId, ts, isAdmin = userId == state.creatorUserId)),
          invitedUserIds = state.invitedUserIds - userId
        )
      case GroupEvents.UserKicked(_, userId, kickerUserId) ⇒
        state.copy(members = state.members - userId)
      case GroupEvents.UserLeft(_, userId) ⇒
        state.copy(members = state.members - userId)
      case GroupEvents.AvatarUpdated(_, avatar) ⇒
        state.copy(avatar = avatar)
      case GroupEvents.TitleUpdated(_, title) ⇒
        state.copy(title = title)
      case GroupEvents.BecamePublic(_) ⇒
        state.copy(typ = GroupType.Public, isHistoryShared = true)
      case GroupEvents.AboutUpdated(_, about) ⇒
        state.copy(about = about)
      case GroupEvents.TopicUpdated(_, topic) ⇒
        state.copy(topic = topic)
      case GroupEvents.UserBecameAdmin(_, userId, _) ⇒
        state.copy(members = state.members.updated(userId, state.members(userId).copy(isAdmin = true)))
      case GroupEvents.IntegrationTokenRevoked(_, token) ⇒
        state.copy(bot = state.bot.map(_.copy(token = token)))
      case GroupEvents.OwnerChanged(_, userId) ⇒
        state.copy(ownerUserId = userId)
    }
  }

  override def handleQuery(state: GroupState): Receive = {
    case GroupQueries.GetIntegrationToken(_, userId)              ⇒ getIntegrationToken(state, userId)
    case GroupQueries.GetIntegrationTokenInternal(_)              ⇒ getIntegrationToken(state)
    case GroupQueries.GetApiStruct(_, userId)                     ⇒ getApiStruct(state, userId)
    case GroupQueries.GetApiFullStruct(_, userId)                 ⇒ getApiFullStruct(state, userId)
    case GroupQueries.CheckAccessHash(_, accessHash)              ⇒ checkAccessHash(state, accessHash)
    case GroupQueries.GetMembers(_)                               ⇒ getMembers(state)
    case GroupQueries.IsPublic(_)                                 ⇒ isPublic(state)
    case GroupQueries.GetAccessHash(_)                            ⇒ getAccessHash(state)
    case GroupQueries.IsHistoryShared(_)                          ⇒ isHistoryShared(state)
    case GroupQueries.GetTitle(_)                                 ⇒ getTitle(state)
    case GroupQueries.LoadMembers(_, clientUserId, limit, offset) ⇒ loadMembers(state, clientUserId, limit, offset)
  }

  override def handleInitCommand: Receive = {
    case Create(_, typ, creatorUserId, creatorAuthSid, title, randomId, userIds) ⇒
      create(groupId, typ, creatorUserId, creatorAuthSid, title, randomId, userIds.toSet)
    case CreateInternal(_, typ, creatorUserId, title, userIds, isHidden, isHistoryShared, extensions) ⇒
      createInternal(typ, creatorUserId, title, userIds, isHidden, isHistoryShared, extensions)
  }

  override def handleCommand(state: GroupState): Receive = {
    case Invite(_, inviteeUserId, inviterUserId, randomId) ⇒
      if (!hasMember(state, inviteeUserId)) {
        persist(GroupEvents.UserInvited(now(), inviteeUserId, inviterUserId)) { evt ⇒
          context become working(updatedState(evt, state))

          val replyTo = sender()

          invite(state, inviteeUserId, inviterUserId, randomId, evt.ts) pipeTo replyTo
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
    case UpdateAvatar(_, clientUserId, avatarOpt, randomId) ⇒
      updateAvatar(state, clientUserId, avatarOpt, randomId)
    case UpdateTitle(_, clientUserId, title, randomId) ⇒
      updateTitle(state, clientUserId, title, randomId)
    case MakePublic(_, description) ⇒
      makePublic(state, description.getOrElse(""))
    case ChangeTopic(_, clientUserId, topic, randomId) ⇒
      updateTopic(state, clientUserId, topic, randomId)
    case ChangeAbout(_, clientUserId, about, randomId) ⇒
      updateAbout(state, clientUserId, about, randomId)
    case MakeUserAdmin(_, clientUserId, candidateId) ⇒
      makeUserAdmin(state, clientUserId, candidateId)
    case RevokeIntegrationToken(_, userId) ⇒
      revokeIntegrationToken(state, userId)
    case TransferOwnership(_, clientUserId, clientAuthSid, userId) ⇒
      transferOwnership(state, clientUserId, clientAuthSid, userId)
    case StopOffice     ⇒ context stop self
    case ReceiveTimeout ⇒ context.parent ! ShardRegion.Passivate(stopMessage = StopOffice)
    case de: DialogEnvelope ⇒
      groupPeer forward de.getAllFields.values.head
  }

  private[this] var groupStateMaybe: Option[GroupState] = None

  override def receiveRecover = {
    case created: GroupEvents.Created ⇒
      groupStateMaybe = Some(initState(created).copy(isHistoryShared = created.typ.exists(t ⇒ t.isChannel || t.isPublic)))
    case evt: GroupEvent ⇒
      groupStateMaybe = groupStateMaybe map (updatedState(evt, _))
    case RecoveryCompleted ⇒
      groupStateMaybe match {
        case Some(group) ⇒ context become working(group)
        case None        ⇒ context become initializing
      }
    case unmatched ⇒
      log.error("Unmatched recovery event {}", unmatched)
  }

  protected def initState(evt: GroupEvents.Created): GroupState = {
    GroupState(
      id = groupId,
      typ = evt.typ.getOrElse(GroupType.General),
      accessHash = evt.accessHash,
      title = evt.title,
      about = None,
      creatorUserId = evt.creatorUserId,
      ownerUserId = evt.creatorUserId,
      createdAt = evt.ts,
      members = (evt.userIds map (userId ⇒ userId → Member(userId, evt.creatorUserId, evt.ts, isAdmin = userId == evt.creatorUserId))).toMap,
      bot = None,
      invitedUserIds = evt.userIds.filterNot(_ == evt.creatorUserId).toSet,
      avatar = None,
      topic = None,
      isHidden = evt.isHidden.getOrElse(false),
      isHistoryShared = evt.isHistoryShared.getOrElse(false),
      extensions = Vector.empty
    )
  }

  private def groupPeer: ActorRef = {
    val groupPeer = "GroupPeer"
    context.child(groupPeer).getOrElse(context.actorOf(GroupPeer.props(groupId), groupPeer))
  }

  protected def hasMember(group: GroupState, userId: Int): Boolean = group.members.keySet.contains(userId)

  protected def isInvited(group: GroupState, userId: Int): Boolean = group.invitedUserIds.contains(userId)

  protected def isBot(group: GroupState, userId: Int): Boolean = userId == 0 || (group.bot exists (_.userId == userId))

  protected def isAdmin(group: GroupState, userId: Int): Boolean = group.members.get(userId) exists (_.isAdmin)

  protected def canInvitePeople(group: GroupState, clientUserId: Int) =
    isMember(group, clientUserId)

  protected def isMember(group: GroupState, clientUserId: Int) = hasMember(group, clientUserId)

  protected def canViewMembers(group: GroupState, clientUserId: Int) =
    (group.typ.isGeneral || group.typ.isPublic) && isMember(group, clientUserId)
}