package im.actor.server.push

import java.util.concurrent.TimeUnit

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

import akka.actor._
import akka.contrib.pattern.ShardRegion
import akka.pattern.pipe
import com.google.protobuf.ByteString

import im.actor.api.rpc.UpdateBox
import im.actor.api.rpc.groups.Group
import im.actor.api.rpc.messaging.{ UpdateMessage, UpdateMessageSent }
import im.actor.api.rpc.peers.Peer
import im.actor.api.rpc.sequence.{ FatSeqUpdate, SeqUpdate }
import im.actor.api.rpc.users.User
import im.actor.server.db.ActorPostgresDriver.api._
import im.actor.server.db.DbExtension
import im.actor.server.persist.HistoryMessage
import im.actor.server.sequence.SeqState
import im.actor.server.{ models, persist ⇒ p }

object SeqUpdatesManagerMessages {
  @SerialVersionUID(1L)
  case class Envelope(authId: Long, payload: Message)

  sealed trait Message

  @SerialVersionUID(1L)
  case object GetSequenceState extends Message

  @SerialVersionUID(1L)
  case class FatMetaData(userIds: Seq[Int], groupIds: Seq[Int])

  @SerialVersionUID(1L)
  case class FatData(users: Seq[User], groups: Seq[Group])

  @SerialVersionUID(1L)
  case class PushUpdate(
    header:         Int,
    serializedData: Array[Byte],
    pushText:       Option[String],
    originPeer:     Option[Peer],
    fatData:        Option[FatData]
  ) extends Message

  @SerialVersionUID(1L)
  private[push] case class PushUpdateGetSequenceState(
    header:         Int,
    serializedData: Array[Byte],
    pushText:       Option[String],
    originPeer:     Option[Peer],
    fatData:        Option[FatData]
  ) extends Message

  @SerialVersionUID(1L)
  case class Subscribe(consumer: ActorRef) extends Message

  @SerialVersionUID(1L)
  case class SubscribeAck(consumer: ActorRef) extends Message

  @SerialVersionUID(1L)
  case class PushCredentialsUpdated(credsOpt: Option[models.push.PushCredentials]) extends Message

  @SerialVersionUID(1L)
  case class DeletePushCredentials(credsOpt: Option[models.push.PushCredentials]) extends Message

  @SerialVersionUID(1L)
  case class UpdateReceived(update: UpdateBox)
}

private[push] object SeqUpdatesManagerActor {
  @SerialVersionUID(1L)
  private case class Initialized(
    seq:            Int,
    timestamp:      Long,
    googleCredsOpt: Option[models.push.GooglePushCredentials],
    appleCredsOpt:  Option[models.push.ApplePushCredentials]
  )

  def props(
    implicit
    googlePushManager: GooglePushManager,
    applePushManager:  ApplePushManager
  ) = Props(classOf[SeqUpdatesManagerActor], googlePushManager, applePushManager)
}

private final class SeqUpdatesManagerActor(
  googlePushManager: GooglePushManager,
  applePushManager:  ApplePushManager
) extends Actor with Stash with ActorLogging with VendorPush {

  import ShardRegion.Passivate

  import SeqUpdatesManagerActor._
  import SeqUpdatesManagerMessages._

  private implicit val system: ActorSystem = context.system
  private implicit val ec: ExecutionContext = context.dispatcher
  private implicit val db: Database = DbExtension(context.system).db

  private val authId: Long = self.path.name.toLong

  // FIXME: move to props
  private val receiveTimeout = context.system.settings.config.getDuration("push.seq-updates-manager.receive-timeout", TimeUnit.SECONDS).seconds
  context.setReceiveTimeout(receiveTimeout)

  private[this] val IncrementOnStart: Int = 1000
  require(IncrementOnStart > 1)
  // it is needed to prevent division by zero in pushUpdate

  private[this] var seq: Int = -1
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
      sender() ! sequenceState(seq, SeqUpdatesManager.timestampToBytes(lastTimestamp))
    case PushUpdate(header, updBytes, pushText, originPeer, fatData) ⇒
      pushUpdate(authId, header, updBytes, pushText, originPeer, fatData)
    case PushUpdateGetSequenceState(header, serializedData, pushText, originPeer, fatData) ⇒
      val replyTo = sender()

      pushUpdate(authId, header, serializedData, pushText, originPeer, fatData, { seqstate: SeqState ⇒
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
    case DeletePushCredentials(credsOpt) ⇒
      credsOpt match {
        case c @ Some(_) if c == appleCredsOpt ⇒
          log.warning("Deleting apple push creds")
          appleCredsOpt = None
          db.run(deletePushCredentials(authId))
        case c @ Some(_) if c == googleCredsOpt ⇒
          log.warning("Deleting google push creds")
          googleCredsOpt = None
          db.run(deletePushCredentials(authId))
        case None ⇒
        // ignoring, already deleted
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
    case Initialized(seq, timestamp, googleCredsOpt, appleCredsOpt) ⇒
      this.seq = seq + IncrementOnStart
      this.lastTimestamp = timestamp
      this.googleCredsOpt = googleCredsOpt
      this.appleCredsOpt = appleCredsOpt

      unstashAll()
      context.become(receiveInitialized)
    case msg ⇒ stash()
  }

  def receive: Receive = stashing

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
      seqUpdOpt.map(_.seq).getOrElse(-1),
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
    pushText:       Option[String],
    originPeer:     Option[Peer],
    fatData:        Option[FatData]
  ): Unit = {
    pushUpdate(authId, header, serializedData, pushText, originPeer, fatData, _ ⇒ ())
  }

  private def pushUpdate(
    authId:         Long,
    header:         Int,
    serializedData: Array[Byte],
    pushText:       Option[String],
    originPeer:     Option[Peer],
    fatData:        Option[FatData],
    cb:             SeqState ⇒ Unit
  ): Unit = {
    // TODO: #perf pinned dispatcher?
    implicit val ec = context.dispatcher

    def push(seq: Int, timestamp: Long): Future[Int] = {
      val (userIds, groupIds) = fatData map (d ⇒ (d.users.map(_.id) → d.groups.map(_.id))) getOrElse (Seq.empty → Seq.empty)

      val seqUpdate = models.sequence.SeqUpdate(authId, timestamp, seq, header, serializedData, userIds.toSet, groupIds.toSet)

      db.run(p.sequence.SeqUpdate.create(seqUpdate))
        .map(_ ⇒ seq)
        .andThen {
          case Success(_) ⇒
            if (header != UpdateMessageSent.header) {
              val updateStruct = fatData match {
                case Some(FatData(users, groups)) ⇒
                  FatSeqUpdate(
                    seqUpdate.seq,
                    SeqUpdatesManager.timestampToBytes(seqUpdate.timestamp),
                    seqUpdate.header,
                    seqUpdate.serializedData,
                    users.toVector,
                    groups.toVector
                  )
                case _ ⇒
                  SeqUpdate(
                    seqUpdate.seq,
                    SeqUpdatesManager.timestampToBytes(seqUpdate.timestamp),
                    seqUpdate.header,
                    seqUpdate.serializedData
                  )
              }

              consumers foreach { consumer ⇒
                consumer ! UpdateReceived(updateStruct)
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
                      HistoryMessage.getUnreadTotal(userId)
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

    push(seq, timestamp) foreach (s ⇒ cb(sequenceState(s, SeqUpdatesManager.timestampToBytes(timestamp))))
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

  private def sequenceState(sequence: Int, timestamp: Long): SeqState = sequenceState(sequence, SeqUpdatesManager.timestampToBytes(timestamp))

  private def sequenceState(sequence: Int, state: Array[Byte]): SeqState = SeqState(sequence, ByteString.copyFrom(state))

}