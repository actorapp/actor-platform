package im.actor.server.push

import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit

import scala.annotation.meta.field
import scala.annotation.tailrec
import scala.concurrent._
import scala.concurrent.duration._
import scala.util.{ Failure, Success }
import akka.actor._
import akka.contrib.pattern.{ ClusterSharding, ShardRegion }
import akka.pattern.{ ask, pipe }
import akka.persistence._
import akka.util.Timeout
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.{ Tag ⇒ KryoTag }
import com.github.tototoshi.slick.PostgresJodaSupport._
import com.google.android.gcm.server.{ Sender ⇒ GCMSender }
import slick.dbio.DBIO
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc.UpdateBox
import im.actor.api.rpc.messaging.{ UpdateMessage, UpdateMessageSent }
import im.actor.api.rpc.peers.Peer
import im.actor.api.rpc.sequence.{ DifferenceUpdate, FatSeqUpdate, SeqUpdate }
import im.actor.api.{ rpc ⇒ api }
import im.actor.server.commons.serialization.KryoSerializable
import im.actor.server.models.sequence
import im.actor.server.util.{ GroupUtils, UserUtils }
import im.actor.server.{ models, persist ⇒ p }

case class SeqUpdatesManagerRegion(ref: ActorRef)

object SeqUpdatesManager {

  @SerialVersionUID(1L)
  private[push] case class Envelope(authId: Long, payload: Message)

  private[push] sealed trait Message

  @SerialVersionUID(1L)
  private[push] case object GetSequenceState extends Message

  @SerialVersionUID(1L)
  private[push] case class PushUpdate(
    header:         Int,
    serializedData: Array[Byte],
    userIds:        Set[Int],
    groupIds:       Set[Int],
    pushText:       Option[String],
    originPeer:     Option[Peer],
    isFat:          Boolean
  ) extends Message

  @SerialVersionUID(1L)
  private[push] case class PushUpdateGetSequenceState(
    header:         Int,
    serializedData: Array[Byte],
    userIds:        Set[Int],
    groupIds:       Set[Int],
    pushText:       Option[String],
    originPeer:     Option[Peer],
    isFat:          Boolean
  ) extends Message

  @SerialVersionUID(1L)
  private[push] case class Subscribe(consumer: ActorRef) extends Message

  @SerialVersionUID(1L)
  private[push] case class SubscribeAck(consumer: ActorRef) extends Message

  @SerialVersionUID(1L)
  private[push] case class PushCredentialsUpdated(credsOpt: Option[models.push.PushCredentials]) extends Message

  @SerialVersionUID(1L)
  private case class Initialized(
    timestamp:      Long,
    googleCredsOpt: Option[models.push.GooglePushCredentials],
    appleCredsOpt:  Option[models.push.ApplePushCredentials]
  )

  @SerialVersionUID(1L)
  case class UpdateReceived(update: UpdateBox)

  type Sequence = Int
  type SequenceState = (Int, Array[Byte])
  type SequenceStateDate = (SequenceState, Long)

  sealed trait PersistentEvent

  @SerialVersionUID(1L)
  final case class SeqChanged(@(KryoTag @field)(0) sequence:Int) extends PersistentEvent

  final case class SeqChangedKryo(
    @(KryoTag @field)(0) sequence:Int
  ) extends PersistentEvent with KryoSerializable

  private val noop1: Any ⇒ Unit = _ ⇒ ()

  private val idExtractor: ShardRegion.IdExtractor = {
    case env @ Envelope(authId, payload) ⇒ (authId.toString, payload)
  }

  private val shardResolver: ShardRegion.ShardResolver = msg ⇒ msg match {
    case Envelope(authId, _) ⇒ (authId % 32).toString // TODO: configurable
  }

  // TODO: configurable
  private val OperationTimeout = Timeout(5.seconds)
  private val MaxDifferenceUpdates = 100

  private def startRegion(props: Option[Props])(implicit system: ActorSystem): SeqUpdatesManagerRegion =
    SeqUpdatesManagerRegion(ClusterSharding(system).start(
      typeName = "SeqUpdatesManager",
      entryProps = props,
      idExtractor = idExtractor,
      shardResolver = shardResolver
    ))

  def startRegion()(
    implicit
    system:            ActorSystem,
    googlePushManager: GooglePushManager,
    applePushManager:  ApplePushManager,
    db:                Database
  ): SeqUpdatesManagerRegion =
    startRegion(Some(Props(classOf[SeqUpdatesManager], googlePushManager, applePushManager, db)))

  def startRegionProxy()(implicit system: ActorSystem): SeqUpdatesManagerRegion = startRegion(None)

  def getSeqState(authId: Long)(implicit region: SeqUpdatesManagerRegion, ec: ExecutionContext): DBIO[(Sequence, Array[Byte])] = {
    for {
      seqstate ← DBIO.from(region.ref.ask(Envelope(authId, GetSequenceState))(OperationTimeout).mapTo[SequenceState])
    } yield seqstate
  }

  def persistAndPushUpdate(
    authId:         Long,
    header:         Int,
    serializedData: Array[Byte],
    userIds:        Set[Int],
    groupIds:       Set[Int],
    pushText:       Option[String],
    originPeer:     Option[Peer],
    isFat:          Boolean
  )(implicit region: SeqUpdatesManagerRegion, ec: ExecutionContext): DBIO[SequenceState] = {
    DBIO.from(pushUpdateGetSeqState(authId, header, serializedData, userIds, groupIds, pushText, originPeer, isFat))
  }

  def persistAndPushUpdate(authId: Long, update: api.Update, pushText: Option[String], isFat: Boolean = false)(implicit region: SeqUpdatesManagerRegion, ec: ExecutionContext): DBIO[SequenceState] = {
    val header = update.header
    val serializedData = update.toByteArray

    val (userIds, groupIds) = updateRefs(update)

    persistAndPushUpdate(authId, header, serializedData, userIds, groupIds, pushText, getOriginPeer(update), isFat)
  }

  def persistAndPushUpdates(authIds: Set[Long], update: api.Update, pushText: Option[String], isFat: Boolean = false)(implicit region: SeqUpdatesManagerRegion, ec: ExecutionContext): DBIO[Seq[SequenceState]] = {
    val header = update.header
    val serializedData = update.toByteArray

    val (userIds, groupIds) = updateRefs(update)

    DBIO.sequence(authIds.toSeq map (persistAndPushUpdate(_, header, serializedData, userIds, groupIds, pushText, getOriginPeer(update), isFat)))
  }

  def broadcastClientAndUsersUpdate(
    userIds:  Set[Int],
    update:   api.Update,
    pushText: Option[String],
    isFat:    Boolean        = false
  )(implicit
    region: SeqUpdatesManagerRegion,
    ec:     ExecutionContext,
    client: api.AuthorizedClientData): DBIO[(SequenceState, Seq[SequenceState])] = {
    val header = update.header
    val serializedData = update.toByteArray
    val (refUserIds, refGroupIds) = updateRefs(update)

    val originPeer = getOriginPeer(update)

    for {
      authIds ← p.AuthId.findIdByUserIds(userIds + client.userId)
      seqstates ← DBIO.sequence(
        authIds.view
          .filterNot(_ == client.authId)
          .map(persistAndPushUpdate(_, header, serializedData, refUserIds, refGroupIds, pushText, originPeer, isFat))
      )
      seqstate ← persistAndPushUpdate(client.authId, header, serializedData, refUserIds, refGroupIds, pushText, originPeer, isFat)
    } yield (seqstate, seqstates)
  }

  def broadcastUsersUpdate(
    userIds:  Set[Int],
    update:   api.Update,
    pushText: Option[String],
    isFat:    Boolean        = false
  )(implicit
    region: SeqUpdatesManagerRegion,
    ec: ExecutionContext): DBIO[Seq[SequenceState]] = {
    val header = update.header
    val serializedData = update.toByteArray
    val (refUserIds, refGroupIds) = updateRefs(update)

    val originPeer = getOriginPeer(update)

    for {
      authIds ← p.AuthId.findIdByUserIds(userIds)
      seqstates ← DBIO.sequence(
        authIds.map(persistAndPushUpdate(_, header, serializedData, refUserIds, refGroupIds, pushText, originPeer, isFat))
      )
    } yield seqstates
  }

  def broadcastUserUpdate(
    userId:   Int,
    update:   api.Update,
    pushText: Option[String],
    isFat:    Boolean        = false
  )(implicit
    region: SeqUpdatesManagerRegion,
    ec: ExecutionContext): DBIO[Seq[SequenceState]] = {
    val header = update.header
    val serializedData = update.toByteArray
    val (userIds, groupIds) = updateRefs(update)

    broadcastUserUpdate(userId, header, serializedData, userIds, groupIds, pushText, getOriginPeer(update), isFat)
  }

  def broadcastUserUpdate(
    userId:         Int,
    header:         Int,
    serializedData: Array[Byte],
    userIds:        Set[Int],
    groupIds:       Set[Int],
    pushText:       Option[String],
    originPeer:     Option[Peer],
    isFat:          Boolean
  )(implicit
    region: SeqUpdatesManagerRegion,
    ec: ExecutionContext): DBIO[Seq[SequenceState]] = {
    for {
      authIds ← p.AuthId.findIdByUserId(userId)
      seqstates ← DBIO.sequence(authIds map (persistAndPushUpdate(_, header, serializedData, userIds, groupIds, pushText, originPeer, isFat)))
    } yield seqstates
  }

  def broadcastClientUpdate(update: api.Update, pushText: Option[String], isFat: Boolean = false)(
    implicit
    region: SeqUpdatesManagerRegion,
    client: api.AuthorizedClientData,
    ec:     ExecutionContext
  ): DBIO[SequenceState] = {
    val header = update.header
    val serializedData = update.toByteArray
    val (userIds, groupIds) = updateRefs(update)

    val originPeer = getOriginPeer(update)

    for {
      otherAuthIds ← p.AuthId.findIdByUserId(client.userId).map(_.view.filter(_ != client.authId))
      _ ← DBIO.sequence(otherAuthIds map (authId ⇒ persistAndPushUpdate(authId, header, serializedData, userIds, groupIds, pushText, originPeer, isFat)))
      ownseqstate ← persistAndPushUpdate(client.authId, header, serializedData, userIds, groupIds, pushText, originPeer, isFat)
    } yield ownseqstate
  }

  def broadcastOtherDevicesUpdate(userId: Int, currentAuthId: Long, update: api.Update, pushText: Option[String], isFat: Boolean = false)(
    implicit
    region: SeqUpdatesManagerRegion,
    ec:     ExecutionContext
  ): DBIO[SequenceState] = {
    val header = update.header
    val serializedData = update.toByteArray
    val (userIds, groupIds) = updateRefs(update)

    val originPeer = getOriginPeer(update)

    for {
      otherAuthIds ← p.AuthId.findIdByUserId(userId).map(_.view.filter(_ != currentAuthId))
      _ ← DBIO.sequence(otherAuthIds map (authId ⇒ persistAndPushUpdate(authId, header, serializedData, userIds, groupIds, pushText, originPeer, isFat)))
      ownseqstate ← persistAndPushUpdate(currentAuthId, header, serializedData, userIds, groupIds, pushText, originPeer, isFat)
    } yield ownseqstate
  }

  def notifyUserUpdate(userId: Int, exceptAuthId: Long, update: api.Update, pushText: Option[String], isFat: Boolean = false)(
    implicit
    region: SeqUpdatesManagerRegion,
    ec:     ExecutionContext
  ): DBIO[Seq[SequenceState]] = {
    val header = update.header
    val serializedData = update.toByteArray
    val (userIds, groupIds) = updateRefs(update)

    val originPeer = getOriginPeer(update)

    notifyUserUpdate(userId, exceptAuthId, header, serializedData, userIds, groupIds, pushText, originPeer, isFat)
  }

  def notifyUserUpdate(
    userId:         Int,
    exceptAuthId:   Long,
    header:         Int,
    serializedData: Array[Byte],
    userIds:        Set[Int],
    groupIds:       Set[Int],
    pushText:       Option[String],
    originPeer:     Option[Peer],
    isFat:          Boolean
  )(implicit
    region: SeqUpdatesManagerRegion,
    ec: ExecutionContext) = {
    for {
      otherAuthIds ← p.AuthId.findIdByUserId(userId).map(_.view.filter(_ != exceptAuthId))
      seqstates ← DBIO.sequence(otherAuthIds map (authId ⇒ persistAndPushUpdate(authId, header, serializedData, userIds, groupIds, pushText, originPeer, isFat)))
    } yield seqstates
  }

  def notifyClientUpdate(update: api.Update, pushText: Option[String], isFat: Boolean = false)(
    implicit
    region: SeqUpdatesManagerRegion,
    client: api.AuthorizedClientData,
    ec:     ExecutionContext
  ): DBIO[Seq[SequenceState]] = {
    val header = update.header
    val serializedData = update.toByteArray
    val (userIds, groupIds) = updateRefs(update)

    val originPeer = getOriginPeer(update)

    notifyClientUpdate(header, serializedData, userIds, groupIds, pushText, originPeer, isFat)
  }

  def notifyClientUpdate(
    header:         Int,
    serializedData: Array[Byte],
    userIds:        Set[Int],
    groupIds:       Set[Int],
    pushText:       Option[String],
    originPeer:     Option[Peer],
    isFat:          Boolean
  )(implicit
    region: SeqUpdatesManagerRegion,
    client: api.AuthorizedClientData,
    ec:     ExecutionContext) = {
    notifyUserUpdate(client.userId, client.authId, header, serializedData, userIds, groupIds, pushText, originPeer, isFat)
  }

  def setPushCredentials(authId: Long, creds: models.push.PushCredentials)(implicit region: SeqUpdatesManagerRegion): Unit = {
    region.ref ! Envelope(authId, PushCredentialsUpdated(Some(creds)))
  }

  def deletePushCredentials(authId: Long)(implicit region: SeqUpdatesManagerRegion): Unit = {
    region.ref ! Envelope(authId, PushCredentialsUpdated(None))
  }

  def getDifference(authId: Long, timestamp: Long, maxSizeInBytes: Long)(implicit ec: ExecutionContext): DBIO[(Vector[models.sequence.SeqUpdate], Boolean)] = {
    def run(state: Long, acc: Vector[models.sequence.SeqUpdate], currentSize: Long): DBIO[(Vector[models.sequence.SeqUpdate], Boolean)] = {
      p.sequence.SeqUpdate.findAfter(authId, state).flatMap { updates ⇒
        if (updates.isEmpty) {
          DBIO.successful(acc → false)
        } else {
          val (newAcc, newSize, allFit) = append(updates.toVector, currentSize, maxSizeInBytes, acc)
          if (allFit) {
            newAcc.lastOption match {
              case Some(u) ⇒ run(u.timestamp, newAcc, newSize)
              case None    ⇒ DBIO.successful(acc → false)
            }
          } else {
            DBIO.successful(newAcc → true)
          }
        }
      }
    }
    run(timestamp, Vector.empty[sequence.SeqUpdate], 0L)
  }

  private def append(updates: Vector[sequence.SeqUpdate], currentSize: Long, maxSizeInBytes: Long, updateAcc: Vector[sequence.SeqUpdate]): (Vector[sequence.SeqUpdate], Long, Boolean) = {
    @tailrec
    def run(updLeft: Vector[sequence.SeqUpdate], acc: Vector[sequence.SeqUpdate], currSize: Long): (Vector[sequence.SeqUpdate], Long, Boolean) = {
      updLeft match {
        case h +: t ⇒
          val newSize = currSize + h.serializedData.length
          if (newSize > maxSizeInBytes) {
            (acc, currSize, false)
          } else {
            run(t, acc :+ h, newSize)
          }
        case Vector() ⇒ (acc, currSize, true)
      }
    }
    run(updates, updateAcc, currentSize)
  }

  def updateRefs(update: api.Update): (Set[Int], Set[Int]) = {
    def peerRefs(peer: api.peers.Peer): (Set[Int], Set[Int]) = {
      if (peer.`type` == api.peers.PeerType.Private) {
        (Set(peer.id), Set.empty)
      } else {
        (Set.empty, Set(peer.id))
      }
    }

    val empty = (Set.empty[Int], Set.empty[Int])
    def singleUser(userId: Int): (Set[Int], Set[Int]) = (Set(userId), Set.empty)
    def singleGroup(groupId: Int): (Set[Int], Set[Int]) = (Set.empty, Set(groupId))
    def users(userIds: Seq[Int]): (Set[Int], Set[Int]) = (userIds.toSet, Set.empty)

    update match {
      case _: api.misc.UpdateConfig              ⇒ empty
      case _: api.configs.UpdateParameterChanged ⇒ empty
      case api.messaging.UpdateChatClear(peer)   ⇒ (Set.empty, Set(peer.id))
      case api.messaging.UpdateChatDelete(peer)  ⇒ (Set.empty, Set(peer.id))
      case api.messaging.UpdateMessage(peer, senderUserId, _, _, _) ⇒
        val refs = peerRefs(peer)
        refs.copy(_1 = refs._1 + senderUserId)
      case api.messaging.UpdateMessageDelete(peer, _)                              ⇒ peerRefs(peer)
      case api.messaging.UpdateMessageRead(peer, _, _)                             ⇒ peerRefs(peer)
      case api.messaging.UpdateMessageReadByMe(peer, _)                            ⇒ peerRefs(peer)
      case api.messaging.UpdateMessageReceived(peer, _, _)                         ⇒ peerRefs(peer)
      case api.messaging.UpdateMessageSent(peer, _, _)                             ⇒ peerRefs(peer)
      case api.messaging.UpdateMessageContentChanged(peer, _, _)                   ⇒ peerRefs(peer)
      case api.messaging.UpdateMessageDateChanged(peer, _, _)                      ⇒ peerRefs(peer)
      case api.groups.UpdateGroupAvatarChanged(groupId, userId, _, _, _)           ⇒ (Set(userId), Set(groupId))
      case api.groups.UpdateGroupInvite(groupId, inviteUserId, _, _)               ⇒ (Set(inviteUserId), Set(groupId))
      case api.groups.UpdateGroupMembersUpdate(groupId, members)                   ⇒ (members.map(_.userId).toSet ++ members.map(_.inviterUserId).toSet, Set(groupId)) // TODO: #perf use foldLeft
      case api.groups.UpdateGroupTitleChanged(groupId, userId, _, _, _)            ⇒ (Set(userId), Set(groupId))
      case api.groups.UpdateGroupUserInvited(groupId, userId, inviterUserId, _, _) ⇒ (Set(userId, inviterUserId), Set(groupId))
      case api.groups.UpdateGroupUserKick(groupId, userId, kickerUserId, _, _)     ⇒ (Set(userId, kickerUserId), Set(groupId))
      case api.groups.UpdateGroupUserLeave(groupId, userId, _, _)                  ⇒ (Set(userId), Set(groupId))
      case api.contacts.UpdateContactRegistered(userId, _, _, _)                   ⇒ singleUser(userId)
      case api.contacts.UpdateContactsAdded(userIds)                               ⇒ users(userIds)
      case api.contacts.UpdateContactsRemoved(userIds)                             ⇒ users(userIds)
      case api.users.UpdateUserAvatarChanged(userId, _)                            ⇒ singleUser(userId)
      case api.users.UpdateUserContactsChanged(userId, _)                          ⇒ singleUser(userId)
      case api.users.UpdateUserLocalNameChanged(userId, _)                         ⇒ singleUser(userId)
      case api.users.UpdateUserNameChanged(userId, _)                              ⇒ singleUser(userId)
      case api.weak.UpdateGroupOnline(groupId, _)                                  ⇒ singleGroup(groupId)
      case api.weak.UpdateTyping(peer, userId, _) ⇒
        val refs = peerRefs(peer)
        refs.copy(_1 = refs._1 + userId)
      case api.weak.UpdateUserLastSeen(userId, _) ⇒ singleUser(userId)
      case api.weak.UpdateUserOffline(userId)     ⇒ singleUser(userId)
      case api.weak.UpdateUserOnline(userId)      ⇒ singleUser(userId)
      case api.calls.UpdateCallRing(user, _)      ⇒ singleUser(user.id)
      case api.calls.UpdateCallEnd(_)             ⇒ empty
    }
  }

  def bytesToTimestamp(bytes: Array[Byte]): Long = {
    if (bytes.isEmpty) {
      0L
    } else {
      ByteBuffer.wrap(bytes).getLong
    }
  }

  def timestampToBytes(timestamp: Long): Array[Byte] = {
    ByteBuffer.allocate(java.lang.Long.BYTES).putLong(timestamp).array()
  }

  private[push] def subscribe(authId: Long, consumer: ActorRef)(implicit region: SeqUpdatesManagerRegion, ec: ExecutionContext, timeout: Timeout): Future[Unit] = {
    region.ref.ask(Envelope(authId, Subscribe(consumer))).mapTo[SubscribeAck].map(_ ⇒ ())
  }

  private def pushUpdateGetSeqState(
    authId:         Long,
    header:         Int,
    serializedData: Array[Byte],
    userIds:        Set[Int],
    groupIds:       Set[Int],
    pushText:       Option[String],
    originPeer:     Option[Peer],
    isFat:          Boolean
  )(implicit region: SeqUpdatesManagerRegion): Future[SequenceState] = {
    region.ref.ask(Envelope(authId, PushUpdateGetSequenceState(header, serializedData, userIds, groupIds, pushText, originPeer, isFat)))(OperationTimeout).mapTo[SequenceState]
  }

  private def pushUpdate(
    authId:         Long,
    header:         Int,
    serializedData: Array[Byte],
    userIds:        Set[Int],
    groupIds:       Set[Int],
    pushText:       Option[String],
    originPeer:     Option[Peer],
    isFat:          Boolean
  )(implicit region: SeqUpdatesManagerRegion): Unit = {
    region.ref ! Envelope(authId, PushUpdate(header, serializedData, userIds, groupIds, pushText, originPeer, isFat))
  }

  private def getOriginPeer(update: api.Update): Option[Peer] = {
    update match {
      case u: UpdateMessage ⇒ Some(u.peer)
      case _                ⇒ None
    }
  }
}

class SeqUpdatesManager(
  googlePushManager: GooglePushManager,
  applePushManager:  ApplePushManager,
  db:                Database
) extends PersistentActor with Stash with ActorLogging with VendorPush {

  import ShardRegion.Passivate

  import SeqUpdatesManager._

  override def persistenceId: String = self.path.parent.name + "-" + self.path.name

  implicit private val system: ActorSystem = context.system
  implicit private val ec: ExecutionContext = context.dispatcher

  private val authId: Long = self.path.name.toLong

  // FIXME: move to props
  private val receiveTimeout = context.system.settings.config.getDuration("push.seq-updates-manager.receive-timeout", TimeUnit.SECONDS).seconds
  context.setReceiveTimeout(receiveTimeout)

  private[this] val IncrementOnStart: Int = 1000
  require(IncrementOnStart > 1)
  // it is needed to prevent divizion by zero in pushUpdate

  private[this] var seq: Int = 0
  private[this] var lastTimestamp: Long = 0
  // TODO: feed this value from db on actor startup
  private[this] var consumers: Set[ActorRef] = Set.empty
  private[this] var googleCredsOpt: Option[models.push.GooglePushCredentials] = None
  private[this] var appleCredsOpt: Option[models.push.ApplePushCredentials] = None

  private[this] val applePusher = new ApplePusher(applePushManager, db)
  private[this] val googlePusher = new GooglePusher(googlePushManager, db)

  initialize()

  def receiveInitialized: Receive = {
    case GetSequenceState ⇒
      sender() ! sequenceState(seq, timestampToBytes(lastTimestamp))
    case PushUpdate(header, updBytes, userIds, groupIds, pushText, originPeer, isFat) ⇒
      pushUpdate(authId, header, updBytes, userIds, groupIds, pushText, originPeer, isFat)
    case PushUpdateGetSequenceState(header, serializedData, userIds, groupIds, pushText, originPeer, isFat) ⇒
      val replyTo = sender()

      pushUpdate(authId, header, serializedData, userIds, groupIds, pushText, originPeer, isFat, { seqstate: SequenceState ⇒
        replyTo ! seqstate
      })
    case Subscribe(consumer: ActorRef) ⇒
      if (!consumers.contains(consumer)) {
        context.watch(consumer)
      }

      consumers += consumer

      log.debug("Consumer subscribed {}", consumer)

      sender() ! SubscribeAck(consumer)
    case PushCredentialsUpdated(credsOpt) ⇒
      credsOpt match {
        case Some(c: models.push.GooglePushCredentials) ⇒
          googleCredsOpt = Some(c)
          db.run(setPushCredentials(c))
        case Some(c: models.push.ApplePushCredentials) ⇒
          appleCredsOpt = Some(c)
          db.run(setPushCredentials(c))
        case None ⇒
          googleCredsOpt = None
          appleCredsOpt = None
          db.run(deletePushCredentials(authId))
      }
    case ReceiveTimeout ⇒
      if (consumers.isEmpty) {
        context.parent ! Passivate(stopMessage = PoisonPill)
      }
    case Terminated(consumer) ⇒
      log.debug("Consumer unsubscribed {}", consumer)
      consumers -= consumer
  }

  def stashing: Receive = {
    case Initialized(timestamp, googleCredsOpt, appleCredsOpt) ⇒
      this.lastTimestamp = timestamp
      this.googleCredsOpt = googleCredsOpt
      this.appleCredsOpt = appleCredsOpt

      unstashAll()
      context.become(receiveInitialized)
    case msg ⇒ stash()
  }

  override def receiveCommand: Receive = stashing

  override def receiveRecover: Receive = {
    case SeqChangedKryo(value) ⇒
      log.debug("Recovery: SeqChangedKryo {}", value)
      seq = value
    case SnapshotOffer(_, SeqChangedKryo(value)) ⇒
      log.debug("Recovery(snapshot): SeqChangedKryo {}", value)
      seq = value
    case SeqChanged(value) ⇒
      log.debug("Recovery: SeqChanged {}", value)
      seq = value
    case SnapshotOffer(_, SeqChanged(value)) ⇒
      log.debug("Recovery(snapshot): SeqChanged {}", value)
      seq = value
    case RecoveryFailure(cause) ⇒
      log.error(cause, "Failed to recover")
      context.stop(self)
    case RecoveryCompleted ⇒
      log.debug("Recovery: Completed, seq: {}", seq)
      seq += IncrementOnStart - 1
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    super.preRestart(reason, message)

    log.error(reason, "SeqUpdatesManager exception, message option: {}", message)
  }

  private def initialize(): Unit = {
    val initiatedFuture: Future[Initialized] = for {
      seqUpdOpt ← db.run(p.sequence.SeqUpdate.findLast(authId))
      googleCredsOpt ← db.run(p.push.GooglePushCredentials.find(authId))
      appleCredsOpt ← db.run(p.push.ApplePushCredentials.find(authId))
    } yield Initialized(
      seqUpdOpt.map(_.timestamp).getOrElse(0),
      googleCredsOpt,
      appleCredsOpt
    )

    initiatedFuture.onFailure {
      case e ⇒
        log.error(e, "Failed initiating SeqUpdatesManager")
        context.parent ! Passivate(stopMessage = PoisonPill)
    }

    initiatedFuture pipeTo self
  }

  private def pushUpdate(
    authId:         Long,
    header:         Int,
    serializedData: Array[Byte],
    userIds:        Set[Int],
    groupIds:       Set[Int],
    pushText:       Option[String],
    originPeer:     Option[Peer],
    isFat:          Boolean
  ): Unit = {
    pushUpdate(authId, header, serializedData, userIds, groupIds, pushText, originPeer, isFat, noop1)
  }

  private def pushUpdate(
    authId:         Long,
    header:         Int,
    serializedData: Array[Byte],
    userIds:        Set[Int],
    groupIds:       Set[Int],
    pushText:       Option[String],
    originPeer:     Option[Peer],
    isFat:          Boolean,
    cb:             SequenceState ⇒ Unit
  ): Unit = {
    // TODO: #perf pinned dispatcher?
    implicit val ec = context.dispatcher

    def push(seq: Int, timestamp: Long): Future[Int] = {
      val seqUpdate = models.sequence.SeqUpdate(authId, timestamp, seq, header, serializedData, userIds, groupIds)

      db.run(p.sequence.SeqUpdate.create(seqUpdate))
        .map(_ ⇒ seq)
        .andThen {
          case Success(_) ⇒
            if (header != UpdateMessageSent.header) {
              consumers foreach { consumer ⇒
                val updateStructFuture = if (isFat) {

                  db.run(
                    p.AuthId.findUserId(authId) flatMap {
                      case Some(userId) ⇒
                        for {
                          users ← UserUtils.getUserStructs(userIds, userId, authId)
                          groups ← GroupUtils.getGroupStructs(groupIds, userId)
                        } yield {
                          FatSeqUpdate(
                            seqUpdate.seq,
                            timestampToBytes(seqUpdate.timestamp),
                            seqUpdate.header,
                            seqUpdate.serializedData,
                            users.toVector,
                            groups.toVector
                          )
                        }
                      case None ⇒
                        throw new Exception(s"Failed to get userId from authId ${authId}")
                    }
                  )
                } else {
                  Future.successful(SeqUpdate(
                    seqUpdate.seq,
                    timestampToBytes(seqUpdate.timestamp),
                    seqUpdate.header,
                    seqUpdate.serializedData
                  ))
                }

                updateStructFuture foreach (s ⇒ consumer ! UpdateReceived(s))
              }

              googleCredsOpt foreach { creds ⇒
                if (header == UpdateMessage.header) {
                  googlePusher.deliverGooglePush(creds, authId, seqUpdate.seq, pushText, originPeer)
                }
              }

              appleCredsOpt foreach { creds ⇒
                db.run {
                  for {
                    optUserId ← p.AuthId.findUserId(authId)
                    unread ← optUserId.map { userId ⇒
                      unreadTotal(userId)
                    } getOrElse DBIO.successful(0)
                    _ = applePusher.deliverApplePush(creds, authId, seqUpdate.seq, pushText, originPeer, unread)
                  } yield ()
                }
              }
            }

            log.debug("Pushed update seq: {}", seq)
          case Failure(err) ⇒
            log.error(err, "Failed to push update") // TODO: throw exception?
        }
    }

    seq += 1
    val timestamp = newTimestamp()

    log.debug("new timestamp {}", timestamp)

    // TODO: DRY this
    if (seq % (IncrementOnStart / 2) == 0) {
      persist(SeqChangedKryo(seq)) { s ⇒
        push(s.sequence, timestamp) foreach (_ ⇒ cb(sequenceState(s.sequence, timestampToBytes(timestamp))))
        saveSnapshot(SeqChangedKryo(s.sequence))
      }
    } else {
      push(seq, timestamp) foreach (updSeq ⇒ cb(sequenceState(updSeq, timestampToBytes(timestamp))))
    }
  }

  private def newTimestamp(): Long = {
    val timestamp = System.currentTimeMillis()

    if (timestamp > lastTimestamp) {
      lastTimestamp = timestamp
      lastTimestamp
    } else {
      lastTimestamp = lastTimestamp + 1
      lastTimestamp
    }
  }

  private def sequenceState(sequence: Int, timestamp: Long): SequenceState =
    sequenceState(sequence, timestampToBytes(timestamp))

  private def sequenceState(sequence: Int, state: Array[Byte]): SequenceState =
    (sequence, state)

  private def unreadTotal(userId: Int): DBIO[Int] = {
    val query = (for {
      d ← p.Dialog.dialogs.filter(d ⇒ d.userId === userId)
      m ← p.HistoryMessage.notDeletedMessages.filter(_.senderUserId =!= userId)
      if m.userId === d.userId && m.peerType === d.peerType && m.peerId === d.peerId && m.date > d.ownerLastReadAt
    } yield m.date).length
    query.result
  }

}
