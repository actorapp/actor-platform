package im.actor.server.group

import java.time.Instant
import java.util.concurrent.TimeUnit

import akka.actor.{ ActorRef, ActorSystem, Props, ReceiveTimeout, Status }
import akka.cluster.sharding.ShardRegion
import akka.http.scaladsl.util.FastFuture
import com.github.benmanes.caffeine.cache.{ Cache, Caffeine }
import im.actor.api.rpc.collections.{ ApiInt32Value, ApiMapValue, ApiMapValueItem, ApiStringValue }
import im.actor.api.rpc.peers.{ ApiPeer, ApiPeerType }
import im.actor.concurrent.ActorFutures
import im.actor.serialization.ActorSerializer
import im.actor.server.cqrs.{ Event, Processor, TaggedEvent }
import im.actor.server.db.DbExtension
import im.actor.server.dialog._
import im.actor.server.group.GroupErrors._
import im.actor.server.group.GroupCommands._
import im.actor.server.group.GroupExt.Value.{ BoolValue, StringValue }
import im.actor.server.group.GroupQueries._
import im.actor.server.names.GlobalNamesStorageKeyValueStorage
import im.actor.server.sequence.SeqUpdatesExtension
import im.actor.server.user.UserExtension

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

//TODO: maybe add dateMillis
trait GroupEvent extends TaggedEvent {
  val ts: Instant

  override def tags: Set[String] = Set("group")
}

case object StopProcessor

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
      20013 → classOf[GroupCommands.UpdateTitle],
      20015 → classOf[GroupCommands.UpdateTopic],
      20016 → classOf[GroupCommands.UpdateAbout],
      20017 → classOf[GroupCommands.MakeUserAdmin],
      20018 → classOf[GroupCommands.RevokeIntegrationToken],
      20020 → classOf[GroupCommands.RevokeIntegrationTokenAck],
      20021 → classOf[GroupCommands.TransferOwnership],
      20022 → classOf[GroupCommands.UpdateShortName],
      20023 → classOf[GroupCommands.DismissUserAdmin],
      20024 → classOf[GroupCommands.UpdateAdminSettings],
      20025 → classOf[GroupCommands.MakeHistoryShared],
      20026 → classOf[GroupCommands.DeleteGroup],
      20027 → classOf[GroupCommands.AddExt],
      20028 → classOf[GroupCommands.RemoveExt],

      21001 → classOf[GroupQueries.GetIntegrationToken],
      21002 → classOf[GroupQueries.GetIntegrationTokenResponse],
      21003 → classOf[GroupQueries.CheckAccessHash],
      21004 → classOf[GroupQueries.CheckAccessHashResponse],
      21005 → classOf[GroupQueries.GetMembers],
      21006 → classOf[GroupQueries.GetMembersResponse],
      21007 → classOf[GroupQueries.GetApiStruct],
      21008 → classOf[GroupQueries.GetApiStructResponse],
      21012 → classOf[GroupQueries.GetAccessHash],
      21013 → classOf[GroupQueries.GetAccessHashResponse],
      21014 → classOf[GroupQueries.IsHistoryShared],
      21015 → classOf[GroupQueries.IsHistorySharedResponse],
      21016 → classOf[GroupQueries.GetTitle],
      21017 → classOf[GroupQueries.LoadMembers],
      21018 → classOf[GroupQueries.GetApiFullStruct],
      21019 → classOf[GroupQueries.CanSendMessage],
      21020 → classOf[GroupQueries.CanSendMessageResponse],
      21021 → classOf[GroupQueries.LoadAdminSettings],
      21022 → classOf[GroupQueries.LoadAdminSettingsResponse],
      21023 → classOf[GroupQueries.IsChannel],
      21024 → classOf[GroupQueries.IsChannelResponse],

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
      22017 → classOf[GroupEvents.OwnerChanged],
      22018 → classOf[GroupEvents.ShortNameUpdated],
      22019 → classOf[GroupEvents.AdminSettingsUpdated],
      22020 → classOf[GroupEvents.AdminStatusChanged],
      22021 → classOf[GroupEvents.HistoryBecameShared],
      22022 → classOf[GroupEvents.GroupDeleted],
      22023 → classOf[GroupEvents.MembersBecameAsync],
      22024 → classOf[GroupEvents.ExtAdded],
      22025 → classOf[GroupEvents.ExtRemoved]
    )

  def persistenceIdFor(groupId: Int): String = s"Group-${groupId}"

  private[group] def props: Props = Props(classOf[GroupProcessor])
}

//FIXME: snapshots!!!
private[group] final class GroupProcessor
  extends Processor[GroupState]
  with ActorFutures
  with GroupCommandHandlers
  with GroupQueryHandlers {

  protected implicit val system: ActorSystem = context.system
  protected implicit val ec: ExecutionContext = system.dispatcher
  protected val db = DbExtension(system).db
  protected val dialogExt = DialogExtension(system)
  protected val seqUpdExt = SeqUpdatesExtension(system)
  protected val userExt = UserExtension(system)

  protected var integrationStorage: IntegrationTokensWriteOps = _
  protected val globalNamesStorage = new GlobalNamesStorageKeyValueStorage

  // short living cache to store member's names when user loads group members
  protected implicit val memberNamesCache: Cache[java.lang.Integer, Future[String]] =
    Caffeine.newBuilder()
      .expireAfterAccess(3, TimeUnit.MINUTES)
      .maximumSize(Long.MaxValue)
      .build[java.lang.Integer, Future[String]]

  protected val groupId = self.path.name.toInt
  protected val apiGroupPeer = ApiPeer(ApiPeerType.Group, groupId)

  context.setReceiveTimeout(5.hours)

  protected def handleCommand: Receive = {
    // creation actions
    case c: Create if state.isNotCreated       ⇒ create(c)
    case _: Create                             ⇒ sender() ! Status.Failure(GroupIdAlreadyExists(groupId))
    case _: GroupCommand if state.isNotCreated ⇒ sender() ! Status.Failure(GroupNotFound(groupId))
    case _: GroupCommand if state.isDeleted    ⇒ sender() ! Status.Failure(GroupAlreadyDeleted(groupId))

    // members actions
    case i: Invite                             ⇒ invite(i)
    case j: Join                               ⇒ join(j)
    case l: Leave                              ⇒ leave(l)
    case k: Kick                               ⇒ kick(k)

    // group info actions
    case u: UpdateAvatar                       ⇒ updateAvatar(u)
    case u: UpdateTitle                        ⇒ updateTitle(u)
    case u: UpdateTopic                        ⇒ updateTopic(u)
    case u: UpdateAbout                        ⇒ updateAbout(u)
    case u: UpdateShortName                    ⇒ updateShortName(u)
    case a: AddExt                             ⇒ addExt(a)
    case r: RemoveExt                          ⇒ removeExt(r)

    // admin actions
    case r: RevokeIntegrationToken             ⇒ revokeIntegrationToken(r)
    case m: MakeUserAdmin                      ⇒ makeUserAdmin(m)
    case d: DismissUserAdmin                   ⇒ dismissUserAdmin(d)
    case t: TransferOwnership                  ⇒ transferOwnership(t)
    case s: UpdateAdminSettings                ⇒ updateAdminSettings(s)
    case m: MakeHistoryShared                  ⇒ makeHistoryShared(m)
    case d: DeleteGroup                        ⇒ deleteGroup(d)

    // dialogs envelopes coming through group.
    case de: DialogEnvelope ⇒
      groupPeerActor forward de.getAllFields.values.head

    // actor's lifecycle
    case StopProcessor  ⇒ context stop self
    case ReceiveTimeout ⇒ context.parent ! ShardRegion.Passivate(stopMessage = StopProcessor)
  }

  // TODO: add backoff
  private def groupPeerActor: ActorRef = {
    val groupPeer = "GroupPeer"
    context.child(groupPeer).getOrElse(context.actorOf(GroupPeer.props(groupId), groupPeer))
  }

  protected def handleQuery: PartialFunction[Any, Future[Any]] = {
    case _: GroupQuery if state.isNotCreated          ⇒ FastFuture.failed(GroupNotFound(groupId))
    //    case _: GroupQuery if state.isDeleted         ⇒ FastFuture.failed(GroupAlreadyDeleted(groupId)) // TODO: figure out how to propperly handle group deletion
    case GetAccessHash()                              ⇒ getAccessHash
    case GetTitle()                                   ⇒ getTitle
    case GetIntegrationToken(optClient)               ⇒ getIntegrationToken(optClient)
    case GetMembers()                                 ⇒ getMembers
    case LoadMembers(clientUserId, limit, offset)     ⇒ loadMembers(clientUserId, limit, offset)
    case IsChannel()                                  ⇒ isChannel
    case IsHistoryShared()                            ⇒ isHistoryShared
    case GetApiStruct(clientUserId, loadGroupMembers) ⇒ getApiStruct(clientUserId, loadGroupMembers)
    case GetApiFullStruct(clientUserId)               ⇒ getApiFullStruct(clientUserId)
    case CheckAccessHash(accessHash)                  ⇒ checkAccessHash(accessHash)
    case CanSendMessage(clientUserId)                 ⇒ canSendMessage(clientUserId)
    case LoadAdminSettings(clientUserId)              ⇒ loadAdminSettings(clientUserId)
  }

  protected def extToApi(exts: Seq[GroupExt]): ApiMapValue = {
    ApiMapValue(
      exts.toVector map {
        case GroupExt(key, BoolValue(b))   ⇒ ApiMapValueItem(key, ApiInt32Value(if (b) 1 else 0))
        case GroupExt(key, StringValue(s)) ⇒ ApiMapValueItem(key, ApiStringValue(s))
      }
    )
  }

  override def afterCommit(e: Event) = {
    super.afterCommit(e)
    if (recoveryFinished) {
      // can't make calls in group with more than 25 members
      if (state.membersCount == 26) {
        updateCanCall(state)
      }
      // from 50+ members we make group with async members
      if (!state.isAsyncMembers && state.membersCount >= 50) {
        makeMembersAsync()
      }
    }
  }

  def persistenceId: String = GroupProcessor.persistenceIdFor(groupId)

  protected def getInitialState: GroupState = GroupState.empty

  override protected def onRecoveryCompleted(): Unit = {
    super.onRecoveryCompleted()
    // set integrationStorage only for created group
    if (state.isCreated) {
      integrationStorage = new IntegrationTokensWriteCompat(state.createdAt.get.toEpochMilli)
    }
  }
}
