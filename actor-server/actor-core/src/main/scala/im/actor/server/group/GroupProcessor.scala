package im.actor.server.group

import java.time.Instant

import akka.actor.{ ActorRef, ActorSystem, Props, ReceiveTimeout, Status }
import akka.cluster.sharding.ShardRegion
import akka.http.scaladsl.util.FastFuture
import im.actor.api.rpc.peers.{ ApiPeer, ApiPeerType }
import im.actor.concurrent.ActorFutures
import im.actor.serialization.ActorSerializer
import im.actor.server.cqrs.{ Processor, TaggedEvent }
import im.actor.server.db.DbExtension
import im.actor.server.dialog.{ DialogEnvelope, DialogExtension }
import im.actor.server.group.GroupErrors.{ GroupIdAlreadyExists, GroupNotFound }
import im.actor.server.group.GroupCommands._
import im.actor.server.group.GroupQueries._
import im.actor.server.names.GlobalNamesStorageKeyValueStorage
import im.actor.server.sequence.SeqUpdatesExtension
import im.actor.server.user.UserExtension

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

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
      21012 → classOf[GroupQueries.GetAccessHash],
      21013 → classOf[GroupQueries.GetAccessHashResponse],
      21014 → classOf[GroupQueries.IsHistoryShared],
      21015 → classOf[GroupQueries.IsHistorySharedResponse],
      21016 → classOf[GroupQueries.GetTitle],
      21017 → classOf[GroupQueries.LoadMembers],
      21018 → classOf[GroupQueries.GetApiFullStruct],
      21019 → classOf[GroupQueries.CanSendMessage],
      21020 → classOf[GroupQueries.CanSendMessageResponse],

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
      22017 → classOf[GroupEvents.ShortNameUpdated]
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

  protected val groupId = self.path.name.toInt
  protected val apiGroupPeer = ApiPeer(ApiPeerType.Group, groupId)

  context.setReceiveTimeout(5.hours)

  protected def handleCommand: Receive = {
    // creation actions
    case c: Create if state.isNotCreated       ⇒ create(c)
    case _: Create                             ⇒ sender() ! Status.Failure(GroupIdAlreadyExists(groupId))
    case _: GroupCommand if state.isNotCreated ⇒ sender() ! Status.Failure(GroupNotFound(groupId))

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

    // admin actions
    case r: RevokeIntegrationToken             ⇒ revokeIntegrationToken(r)
    case m: MakeUserAdmin                      ⇒ makeUserAdmin(m)
    case t: TransferOwnership                  ⇒ transferOwnership(t)

    // termination actions
    case StopProcessor                         ⇒ context stop self
    case ReceiveTimeout                        ⇒ context.parent ! ShardRegion.Passivate(stopMessage = StopProcessor)

    // dialogs envelopes coming through group.
    case de: DialogEnvelope ⇒
      groupPeerActor forward de.getAllFields.values.head

  }

  // TODO: add backoff
  private def groupPeerActor: ActorRef = {
    val groupPeer = "GroupPeer"
    context.child(groupPeer).getOrElse(context.actorOf(GroupPeer.props(groupId), groupPeer))
  }

  protected def handleQuery: PartialFunction[Any, Future[Any]] = {
    case _: GroupQuery if state.isNotCreated      ⇒ FastFuture.failed(GroupNotFound(groupId))
    case GetAccessHash()                          ⇒ getAccessHash
    case GetTitle()                               ⇒ getTitle
    case GetIntegrationToken(optClient)           ⇒ getIntegrationToken(optClient)
    case GetMembers()                             ⇒ getMembers
    case LoadMembers(clientUserId, limit, offset) ⇒ loadMembers(clientUserId, limit, offset)
    case IsPublic()                               ⇒ isPublic
    case IsHistoryShared()                        ⇒ isHistoryShared
    case GetApiStruct(clientUserId)               ⇒ getApiStruct(clientUserId)
    case GetApiFullStruct(clientUserId)           ⇒ getApiFullStruct(clientUserId)
    case CheckAccessHash(accessHash)              ⇒ checkAccessHash(accessHash)
    case CanSendMessage(clientUserId)             ⇒ canSendMessage(clientUserId)
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
