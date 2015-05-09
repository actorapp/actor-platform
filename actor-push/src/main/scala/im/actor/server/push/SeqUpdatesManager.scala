package im.actor.server.push

import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit

import scala.annotation.meta.field
import scala.concurrent._
import scala.concurrent.duration._
import scala.util.{ Failure, Success }

import akka.actor._
import akka.contrib.pattern.{ ClusterSharding, ShardRegion }
import akka.pattern.{ ask, pipe }
import akka.persistence._
import akka.util.Timeout
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.{ Tag ⇒ KryoTag }
import com.google.android.gcm.server.{ Message ⇒ GCMMessage, Sender ⇒ GCMSender }
import com.relayrides.pushy.apns.util.{ ApnsPayloadBuilder, SimpleApnsPushNotification }
import slick.dbio.DBIO
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc.messaging.{ UpdateMessage, UpdateMessageSent }
import im.actor.api.rpc.sequence.SeqUpdate
import im.actor.api.{ rpc ⇒ api }
import im.actor.server.commons.serialization.TaggedFieldSerializable
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
    pushText:       Option[String]
  ) extends Message

  @SerialVersionUID(1L)
  private[push] case class PushUpdateGetSequenceState(
    header:         Int,
    serializedData: Array[Byte],
    userIds:        Set[Int],
    groupIds:       Set[Int],
    pushText:       Option[String]
  ) extends Message

  @SerialVersionUID(1L)
  private[push] case class Subscribe(consumer: ActorRef) extends Message

  @SerialVersionUID(1L)
  private[push] case class SubscribeAck(consumer: ActorRef) extends Message

  @SerialVersionUID(1L)
  private[push] case class GooglePushCredentialsUpdated(credsOpt: Option[models.push.GooglePushCredentials]) extends Message

  @SerialVersionUID(1L)
  private[push] case class ApplePushCredentialsUpdated(credsOpt: Option[models.push.ApplePushCredentials]) extends Message

  @SerialVersionUID(1L)
  private case class Initialized(
    authId:         Long,
    timestamp:      Long,
    googleCredsOpt: Option[models.push.GooglePushCredentials],
    appleCredsOpt:  Option[models.push.ApplePushCredentials]
  )

  @SerialVersionUID(1L)
  case class UpdateReceived(update: SeqUpdate)

  type Sequence = Int
  type SequenceState = (Int, Array[Byte])

  private sealed trait PersistentEvent

  @SerialVersionUID(1L)
  private final case class SeqChanged(@(KryoTag @field)(0) sequence:Int) extends PersistentEvent with TaggedFieldSerializable

  private val noop1: Any ⇒ Unit = _ ⇒ ()

  private val idExtractor: ShardRegion.IdExtractor = {
    case env @ Envelope(authId, payload) ⇒ (authId.toString, env)
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
    system:           ActorSystem,
    gcmSender:        GCMSender,
    applePushManager: ApplePushManager,
    db:               Database
  ): SeqUpdatesManagerRegion =
    startRegion(Some(Props(classOf[SeqUpdatesManager], gcmSender, applePushManager, db)))

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
    pushText:       Option[String]
  )(implicit region: SeqUpdatesManagerRegion, ec: ExecutionContext): DBIO[SequenceState] = {
    DBIO.from(pushUpdateGetSeqState(authId, header, serializedData, userIds, groupIds, pushText))
  }

  def persistAndPushUpdate(authId: Long, update: api.Update, pushText: Option[String])(implicit region: SeqUpdatesManagerRegion, ec: ExecutionContext): DBIO[SequenceState] = {
    val header = update.header
    val serializedData = update.toByteArray

    val (userIds, groupIds) = updateRefs(update)

    persistAndPushUpdate(authId, header, serializedData, userIds, groupIds, pushText)
  }

  def persistAndPushUpdates(authIds: Set[Long], update: api.Update, pushText: Option[String])(implicit region: SeqUpdatesManagerRegion, ec: ExecutionContext): DBIO[Seq[SequenceState]] = {
    val header = update.header
    val serializedData = update.toByteArray

    val (userIds, groupIds) = updateRefs(update)

    DBIO.sequence(authIds.toSeq map (persistAndPushUpdate(_, header, serializedData, userIds, groupIds, pushText)))
  }

  def broadcastUpdateAll(
    userIds:  Set[Int],
    update:   api.Update,
    pushText: Option[String]
  )(implicit
    region: SeqUpdatesManagerRegion,
    ec:     ExecutionContext,
    client: api.AuthorizedClientData): DBIO[(SequenceState, Seq[SequenceState])] = {
    val header = update.header
    val serializedData = update.toByteArray
    val (refUserIds, refGroupIds) = updateRefs(update)

    for {
      authIds ← p.AuthId.findIdByUserIds(userIds + client.userId)
      seqstates ← DBIO.sequence(authIds.view.filterNot(_ == client.authId).map(persistAndPushUpdate(_, header, serializedData, refUserIds, refGroupIds, pushText)))
      seqstate ← persistAndPushUpdate(client.authId, header, serializedData, refUserIds, refGroupIds, pushText)
    } yield (seqstate, seqstates)
  }

  def broadcastUserUpdate(
    userId:   Int,
    update:   api.Update,
    pushText: Option[String]
  )(implicit
    region: SeqUpdatesManagerRegion,
    ec: ExecutionContext): DBIO[Seq[SequenceState]] = {
    val header = update.header
    val serializedData = update.toByteArray
    val (userIds, groupIds) = updateRefs(update)

    broadcastUserUpdate(userId, header, serializedData, userIds, groupIds, pushText)
  }

  def broadcastUserUpdate(
    userId:         Int,
    header:         Int,
    serializedData: Array[Byte],
    userIds:        Set[Int],
    groupIds:       Set[Int],
    pushText:       Option[String]
  )(implicit
    region: SeqUpdatesManagerRegion,
    ec: ExecutionContext): DBIO[Seq[SequenceState]] = {
    for {
      authIds ← p.AuthId.findIdByUserId(userId)
      seqstates ← DBIO.sequence(authIds map (persistAndPushUpdate(_, header, serializedData, userIds, groupIds, pushText)))
    } yield seqstates
  }

  def broadcastClientUpdate(update: api.Update, pushText: Option[String])(implicit
    region: SeqUpdatesManagerRegion,
                                                                          client: api.AuthorizedClientData,
                                                                          ec:     ExecutionContext): DBIO[SequenceState] = {
    val header = update.header
    val serializedData = update.toByteArray
    val (userIds, groupIds) = updateRefs(update)

    for {
      otherAuthIds ← p.AuthId.findIdByUserId(client.userId).map(_.view.filter(_ != client.authId))
      _ ← DBIO.sequence(otherAuthIds map (authId ⇒ persistAndPushUpdate(authId, header, serializedData, userIds, groupIds, pushText)))
      ownseqstate ← persistAndPushUpdate(client.authId, header, serializedData, userIds, groupIds, pushText)
    } yield ownseqstate
  }

  def notifyClientUpdate(update: api.Update, pushText: Option[String])(implicit
    region: SeqUpdatesManagerRegion,
                                                                       client: api.AuthorizedClientData,
                                                                       ec:     ExecutionContext): DBIO[Seq[SequenceState]] = {
    val header = update.header
    val serializedData = update.toByteArray
    val (userIds, groupIds) = updateRefs(update)

    notifyClientUpdate(header, serializedData, userIds, groupIds, pushText)
  }

  def notifyClientUpdate(
    header:         Int,
    serializedData: Array[Byte],
    userIds:        Set[Int],
    groupIds:       Set[Int],
    pushText:       Option[String]
  )(implicit
    region: SeqUpdatesManagerRegion,
    client: api.AuthorizedClientData,
    ec:     ExecutionContext) = {
    for {
      otherAuthIds ← p.AuthId.findIdByUserId(client.userId).map(_.view.filter(_ != client.authId))
      seqstates ← DBIO.sequence(otherAuthIds map (authId ⇒ persistAndPushUpdate(authId, header, serializedData, userIds, groupIds, pushText)))
    } yield seqstates
  }

  def setUpdatedGooglePushCredentials(authId: Long, credsOpt: Option[models.push.GooglePushCredentials])(implicit seqUpdManagerRegion: SeqUpdatesManagerRegion): Unit = {
    seqUpdManagerRegion.ref ! Envelope(authId, GooglePushCredentialsUpdated(credsOpt))
  }

  def setUpdatedApplePushCredentials(authId: Long, credsOpt: Option[models.push.ApplePushCredentials])(implicit seqUpdManagerRegion: SeqUpdatesManagerRegion): Unit = {
    seqUpdManagerRegion.ref ! Envelope(authId, ApplePushCredentialsUpdated(credsOpt))
  }

  def getDifference(authId: Long, state: Array[Byte])(implicit ec: ExecutionContext) = {
    val timestamp = bytesToTimestamp(state)
    for (updates ← p.sequence.SeqUpdate.findAfter(authId, timestamp, MaxDifferenceUpdates + 1)) yield {
      if (updates.length > MaxDifferenceUpdates) {
        (updates.take(updates.length - 1), true)
      } else {
        (updates, false)
      }
    }
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
      case api.messaging.UpdateMessageDelete(peer, _)                            ⇒ peerRefs(peer)
      case api.messaging.UpdateMessageRead(peer, _, _)                           ⇒ peerRefs(peer)
      case api.messaging.UpdateMessageReadByMe(peer, _)                          ⇒ peerRefs(peer)
      case api.messaging.UpdateMessageReceived(peer, _, _)                       ⇒ peerRefs(peer)
      case api.messaging.UpdateMessageSent(peer, _, _)                           ⇒ peerRefs(peer)
      case api.groups.UpdateGroupAvatarChanged(groupId, userId, _, _, _)         ⇒ (Set(userId), Set(groupId))
      case api.groups.UpdateGroupInvite(groupId, inviteUserId, _, _)             ⇒ (Set(inviteUserId), Set(groupId))
      case api.groups.UpdateGroupMembersUpdate(groupId, members)                 ⇒ (members.map(_.userId).toSet ++ members.map(_.inviterUserId).toSet, Set(groupId)) // TODO: #perf use foldLeft
      case api.groups.UpdateGroupTitleChanged(groupId, userId, _, _, _)          ⇒ (Set(userId), Set(groupId))
      case api.groups.UpdateGroupUserAdded(groupId, userId, inviterUserId, _, _) ⇒ (Set(userId, inviterUserId), Set(groupId))
      case api.groups.UpdateGroupUserKick(groupId, userId, kickerUserId, _, _)   ⇒ (Set(userId, kickerUserId), Set(groupId))
      case api.groups.UpdateGroupUserLeave(groupId, userId, _, _)                ⇒ (Set(userId), Set(groupId))
      case api.contacts.UpdateContactRegistered(userId, _, _)                    ⇒ singleUser(userId)
      case api.contacts.UpdateContactsAdded(userIds)                             ⇒ users(userIds)
      case api.contacts.UpdateContactsRemoved(userIds)                           ⇒ users(userIds)
      case api.users.UpdateEmailMoved(_, userId)                                 ⇒ singleUser(userId)
      case _: api.users.UpdateEmailTitleChanged                                  ⇒ empty
      case api.users.UpdatePhoneMoved(_, userId)                                 ⇒ singleUser(userId)
      case _: api.users.UpdatePhoneTitleChanged                                  ⇒ empty
      case api.users.UpdateUserAvatarChanged(userId, _)                          ⇒ singleUser(userId)
      case api.users.UpdateUserContactsChanged(userId, _, _)                     ⇒ singleUser(userId)
      case api.users.UpdateUserEmailAdded(userId, _)                             ⇒ singleUser(userId)
      case api.users.UpdateUserEmailRemoved(userId, _)                           ⇒ singleUser(userId)
      case api.users.UpdateUserLocalNameChanged(userId, _)                       ⇒ singleUser(userId)
      case api.users.UpdateUserNameChanged(userId, _)                            ⇒ singleUser(userId)
      case api.users.UpdateUserPhoneAdded(userId, _)                             ⇒ singleUser(userId)
      case api.users.UpdateUserPhoneRemoved(userId, _)                           ⇒ singleUser(userId)
      case api.users.UpdateUserStateChanged(userId, _)                           ⇒ singleUser(userId)
      case api.weak.UpdateGroupOnline(groupId, _)                                ⇒ singleGroup(groupId)
      case api.weak.UpdateTyping(peer, userId, _) ⇒
        val refs = peerRefs(peer)
        refs.copy(_1 = refs._1 + userId)
      case api.weak.UpdateUserLastSeen(userId, _) ⇒ singleUser(userId)
      case api.weak.UpdateUserOffline(userId)     ⇒ singleUser(userId)
      case api.weak.UpdateUserOnline(userId)      ⇒ singleUser(userId)
    }
  }

  private[push] def subscribe(authId: Long, consumer: ActorRef)(implicit region: SeqUpdatesManagerRegion, ec: ExecutionContext, timeout: Timeout): Future[Unit] = {
    region.ref.ask(Envelope(authId, Subscribe(consumer))).mapTo[SubscribeAck].map(_ ⇒ ())
  }

  private def bytesToTimestamp(bytes: Array[Byte]): Long = {
    if (bytes.isEmpty) {
      0L
    } else {
      ByteBuffer.wrap(bytes).getLong
    }
  }

  private def timestampToBytes(timestamp: Long): Array[Byte] = {
    ByteBuffer.allocate(java.lang.Long.BYTES).putLong(timestamp).array()
  }

  private def pushUpdateGetSeqState(
    authId:         Long,
    header:         Int,
    serializedData: Array[Byte],
    userIds:        Set[Int],
    groupIds:       Set[Int],
    pushText:       Option[String]
  )(implicit region: SeqUpdatesManagerRegion): Future[SequenceState] = {
    region.ref.ask(Envelope(authId, PushUpdateGetSequenceState(header, serializedData, userIds, groupIds, pushText)))(OperationTimeout).mapTo[SequenceState]
  }

  private def pushUpdate(
    authId:         Long,
    header:         Int,
    serializedData: Array[Byte],
    userIds:        Set[Int],
    groupIds:       Set[Int],
    pushText:       Option[String]
  )(implicit region: SeqUpdatesManagerRegion): Unit = {
    region.ref ! Envelope(authId, PushUpdate(header, serializedData, userIds, groupIds, pushText))
  }
}

class SeqUpdatesManager(
  gcmSender:        GCMSender,
  applePushManager: ApplePushManager,
  db:               Database
) extends PersistentActor with Stash with ActorLogging {

  import ShardRegion.Passivate

  import SeqUpdatesManager._

  override def persistenceId: String = self.path.parent.name + "-" + self.path.name

  // FIXME: move to props
  val receiveTimeout = context.system.settings.config.getDuration("push.seq-updates-manager.receive-timeout", TimeUnit.SECONDS).seconds
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

  def receiveInitialized: Receive = {
    case Envelope(_, GetSequenceState) ⇒
      sender() ! sequenceState(seq, timestampToBytes(lastTimestamp))
    case Envelope(authId, PushUpdate(header, updBytes, userIds, groupIds, pushText)) ⇒
      pushUpdate(authId, header, updBytes, userIds, groupIds, pushText)
    case Envelope(authId, PushUpdateGetSequenceState(header, serializedData, userIds, groupIds, pushText)) ⇒
      val replyTo = sender()

      pushUpdate(authId, header, serializedData, userIds, groupIds, pushText, { seqstate: SequenceState ⇒
        replyTo ! seqstate
      })
    case Envelope(authId, Subscribe(consumer: ActorRef)) ⇒
      if (!consumers.contains(consumer)) {
        context.watch(consumer)
      }

      consumers += consumer

      log.debug("Consumer subscribed {}", consumer)

      sender() ! SubscribeAck(consumer)
    case Envelope(_, GooglePushCredentialsUpdated(credsOpt)) ⇒
      this.googleCredsOpt = credsOpt
    case Envelope(_, ApplePushCredentialsUpdated(credsOpt)) ⇒
      this.appleCredsOpt = credsOpt
    case ReceiveTimeout ⇒
      if (consumers.isEmpty) {
        context.parent ! Passivate(stopMessage = PoisonPill)
      }
    case Terminated(consumer) ⇒
      log.debug("Consumer unsubscribed {}", consumer)
      consumers -= consumer
  }

  def stashing: Receive = {
    case Initialized(authId, timestamp, googleCredsOpt, appleCredsOpt) ⇒
      this.lastTimestamp = timestamp
      this.googleCredsOpt = googleCredsOpt
      this.appleCredsOpt = appleCredsOpt

      unstashAll()
      context.become(receiveInitialized)
    case msg ⇒ stash()
  }

  def waitingForEnvelope: Receive = {
    case env @ Envelope(authId, _) ⇒
      stash()
      context.become(stashing)

      // TODO: pinned dispatcher?
      implicit val ec = context.dispatcher

      val initiatedFuture: Future[Initialized] = for {
        seqUpdOpt ← db.run(p.sequence.SeqUpdate.find(authId).headOption)
        googleCredsOpt ← db.run(p.push.GooglePushCredentials.find(authId))
        appleCredsOpt ← db.run(p.push.ApplePushCredentials.find(authId))
      } yield Initialized(
        authId,
        seqUpdOpt.map(_.timestamp).getOrElse(0),
        googleCredsOpt,
        appleCredsOpt
      )

      initiatedFuture.onFailure {
        case e ⇒
          log.error(e, "Failed initiating SeqUpdatesManager")
          context.parent ! Passivate(stopMessage = PoisonPill)
      }

      initiatedFuture.pipeTo(self)
    case msg ⇒ stash()
  }

  override def receiveCommand: Receive = waitingForEnvelope

  override def receiveRecover: Receive = {
    case SeqChanged(value) ⇒
      log.debug("Recovery: SeqChanged {}", value)
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

  private def pushUpdate(
    authId:         Long,
    header:         Int,
    serializedData: Array[Byte],
    userIds:        Set[Int],
    groupIds:       Set[Int],
    pushText:       Option[String]
  ): Unit = {
    pushUpdate(authId, header, serializedData, userIds, groupIds, pushText, noop1)
  }

  private def pushUpdate(
    authId:         Long,
    header:         Int,
    serializedData: Array[Byte],
    userIds:        Set[Int],
    groupIds:       Set[Int],
    pushText:       Option[String],
    cb:             SequenceState ⇒ Unit
  ): Unit = {
    // TODO: #perf pinned dispatcher?
    implicit val ec = context.dispatcher

    def push(seq: Int, timestamp: Long): Future[Unit] = {
      val seqUpdate = models.sequence.SeqUpdate(authId, timestamp, seq, header, serializedData, userIds, groupIds)

      db.run(p.sequence.SeqUpdate.create(seqUpdate))
        .map(_ ⇒ ())
        .andThen {
          case Success(_) ⇒
            consumers foreach { consumer ⇒
              consumer ! UpdateReceived(
                SeqUpdate(
                  seqUpdate.seq,
                  timestampToBytes(seqUpdate.timestamp),
                  seqUpdate.header,
                  seqUpdate.serializedData
                )
              )
            }

            if (header != UpdateMessageSent.header) {
              googleCredsOpt foreach { creds ⇒
                if (header == UpdateMessage.header) {
                  deliverGooglePush(creds, authId, seqUpdate.seq)
                }
              }

              appleCredsOpt foreach { creds ⇒
                deliverApplePush(creds, authId, seqUpdate.seq, pushText)
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
      persist(SeqChanged(seq)) { _ ⇒
        push(seq, timestamp) foreach (_ ⇒ cb(sequenceState(seq, timestampToBytes(timestamp))))
      }
    } else {
      push(seq, timestamp) foreach (_ ⇒ cb(sequenceState(seq, timestampToBytes(timestamp))))
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

  private def deliverGooglePush(creds: models.push.GooglePushCredentials, authId: Long, seq: Int): Unit = {
    log.debug("Delivering google push, authId: {}, seq: {}", authId, seq)

    val message = (new GCMMessage.Builder)
      .collapseKey(authId.toString)
      .addData("seq", seq.toString)
      .build()

    // TODO: configurable retries
    // TODO: #perf pinned dispatcher
    implicit val ec = context.dispatcher

    val resultFuture = Future { blocking { gcmSender.send(message, creds.regId, 3) } }
    resultFuture map { result ⇒
      log.debug("Delivery result messageId: {}, error: {}", result.getMessageId, result.getErrorCodeName)
    }
  }

  private def deliverApplePush(creds: models.push.ApplePushCredentials, authId: Long, seq: Int, textOpt: Option[String]): Unit = {
    log.debug("Delivering apple push, authId: {}, seq: {}", authId, seq)

    val builder = new ApnsPayloadBuilder

    textOpt foreach (builder.setAlertBody)
    builder.addCustomProperty("seq", seq)
    builder.setContentAvailable(true)

    val payload = builder.buildWithDefaultMaximumLength()

    applePushManager.getInstance(creds.apnsKey) map { mgr ⇒
      mgr.getQueue.put(new SimpleApnsPushNotification(creds.token, payload))
    }
  }
}
