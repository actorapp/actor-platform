package im.actor.server.sequence

import java.util.concurrent.TimeUnit

import akka.serialization.SerializationExtension
import com.github.benmanes.caffeine.cache.Caffeine

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

import akka.actor._
import akka.contrib.pattern.ShardRegion
import akka.pattern.pipe
import com.google.protobuf.ByteString

import im.actor.api.rpc.messaging.{ UpdateMessage, UpdateMessageSent }
import im.actor.api.rpc.peers.ApiPeer
import im.actor.api.rpc.sequence.SeqUpdate
import im.actor.server.db.ActorPostgresDriver.api._
import im.actor.server.db.DbExtension
import im.actor.server.persist.HistoryMessage
import im.actor.server.{ models, persist ⇒ p }

trait SeqUpdatesManagerMessage {
  val authId: Long
}

@SerialVersionUID(1L)
case class FatMetaData(userIds: Seq[Int], groupIds: Seq[Int])

private[sequence] object SeqUpdatesManagerActor {
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

  private[this] val IncrementOnStart = 1000
  private[this] val MaxDeliveryCacheSize = 100L
  require(IncrementOnStart > 1)
  // it is needed to prevent division by zero in pushUpdate

  private val ext = SeqUpdatesExtension(context.system)

  private val deliveryCache = Caffeine.newBuilder().maximumSize(MaxDeliveryCacheSize).build[String, SeqState]()

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
    case GetSeqState(_) ⇒
      sender() ! sequenceState(seq, SeqUpdatesManager.timestampToBytes(lastTimestamp))
    case PushUpdate(_, deliveryId, header, serializedData, refs, isFat, pushText, originPeer) ⇒
      val replyTo = sender()

      deliveryId.flatMap(id ⇒ Option(deliveryCache.getIfPresent(id))) match {
        case Some(seqstate) ⇒ replyTo ! seqstate
        case None ⇒
          pushUpdate(header, serializedData, refs, isFat, pushText, originPeer, { seqstate: SeqState ⇒
            deliveryId foreach { id ⇒
              deliveryCache.put(id, seqstate)
            }

            replyTo ! seqstate
          })
      }
    case msg @ Subscribe(_, consumerStr) ⇒
      val consumer = SerializationExtension(context.system).system.provider.resolveActorRef(consumerStr)

      if (!consumers.contains(consumer)) {
        context.watch(consumer)
      }

      consumers += consumer

      log.debug("Consumer subscribed {}", consumer)

      sender() ! SubscribeAck(msg)
    case p @ PushCredentialsUpdated(_, creds) ⇒
      creds match {
        case PushCredentialsUpdated.Credentials.Apple(ApplePushCredentials(apnsKey, token)) ⇒
          val model = models.push.ApplePushCredentials(authId, apnsKey, token.toByteArray)
          appleCredsOpt = Some(model)
          db.run(setPushCredentials(model))
        case PushCredentialsUpdated.Credentials.Google(GooglePushCredentials(projectId, regId)) ⇒
          val model = models.push.GooglePushCredentials(authId, projectId, regId)
          googleCredsOpt = Some(model)
          db.run(setPushCredentials(model))
        case _ ⇒ // TODO: delete?
      }
    case PushCredentialsDeleted(_) ⇒
      if (appleCredsOpt.isDefined) {
        log.warning("Deleting apple push creds")
        appleCredsOpt = None
        db.run(deletePushCredentials(authId))
      } else if (googleCredsOpt.isDefined) {
        log.warning("Deleting google push creds")
        googleCredsOpt = None
        db.run(deletePushCredentials(authId))
      } else {
        log.warning("Ignoring, my {} {}", appleCredsOpt, googleCredsOpt)
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
    val initiatedFuture: Future[Initialized] = db.run(for {
      seqUpdOpt ← p.sequence.SeqUpdate.findLast(authId)
      googleCredsOpt ← p.push.GooglePushCredentials.find(authId)
      appleCredsOpt ← p.push.ApplePushCredentials.find(authId)
    } yield Initialized(
      seqUpdOpt.map(_.seq).getOrElse(-1),
      seqUpdOpt.map(_.timestamp).getOrElse(0),
      googleCredsOpt,
      appleCredsOpt
    ))

    initiatedFuture.onFailure {
      case e ⇒
        log.error(e, "Failed initiating SeqUpdatesManager")
        context.parent ! Passivate(stopMessage = PoisonPill)
    }

    initiatedFuture pipeTo self
  }

  private def pushUpdate(
    header:         Int,
    serializedData: ByteString,
    refs:           UpdateRefs,
    isFat:          Boolean,
    pushText:       Option[String],
    originPeer:     Option[ApiPeer],
    cb:             SeqState ⇒ Unit
  ): Unit = {
    // TODO: #perf pinned dispatcher?
    implicit val ec = context.dispatcher

    def push(seq: Int, timestamp: Long): Future[Int] = {
      val UpdateRefs(userIds, groupIds) = refs

      val seqUpdate = models.sequence.SeqUpdate(authId, timestamp, seq, header, serializedData.toByteArray, userIds.toSet, groupIds.toSet)

      ext.persistUpdate(seqUpdate)
        .map(_ ⇒ seq)
        .andThen {
          case Success(_) ⇒
            if (header != UpdateMessageSent.header) {
              val updateStruct = SeqUpdate(
                seqUpdate.seq,
                SeqUpdatesManager.timestampToBytes(seqUpdate.timestamp),
                seqUpdate.header,
                seqUpdate.serializedData
              )

              val fatRefs =
                if (isFat)
                  Some(refs)
                else
                  None

              consumers foreach { consumer ⇒
                consumer ! UpdateReceived(updateStruct, fatRefs)
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