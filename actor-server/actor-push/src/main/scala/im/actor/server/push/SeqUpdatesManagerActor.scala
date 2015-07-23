package im.actor.server.push

import java.util.concurrent.TimeUnit

import scala.concurrent.{ Future, ExecutionContext }
import scala.concurrent.duration._
import scala.util.{ Failure, Success }

import akka.actor._
import akka.contrib.pattern.ShardRegion
import akka.contrib.pattern.ShardRegion.Passivate
import akka.pattern.pipe
import com.google.protobuf.ByteString
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc.messaging.{ UpdateMessage, UpdateMessageSent }
import im.actor.api.rpc.peers.Peer
import im.actor.api.rpc.sequence.{ SeqUpdate, FatSeqUpdate }
import im.actor.server.sequence.SeqState
import im.actor.server.util.{ GroupUtils, UserUtils }
import im.actor.server.{ persist ⇒ p, models }

object SeqUpdatesManagerActor {
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
    applePushManager:  ApplePushManager,
    db:                Database
  ) = Props(classOf[SeqUpdatesManagerActor], googlePushManager, applePushManager, db)
}

class SeqUpdatesManagerActor(
  googlePushManager: GooglePushManager,
  applePushManager:  ApplePushManager,
  db:                Database
) extends Actor with Stash with ActorLogging with VendorPush {

  import ShardRegion.Passivate

  import SeqUpdatesManager._
  import SeqUpdatesManagerActor._

  implicit private val system: ActorSystem = context.system
  implicit private val ec: ExecutionContext = context.dispatcher

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
      sender() ! sequenceState(seq, timestampToBytes(lastTimestamp))
    case PushUpdate(header, updBytes, userIds, groupIds, pushText, originPeer, isFat) ⇒
      pushUpdate(authId, header, updBytes, userIds, groupIds, pushText, originPeer, isFat)
    case PushUpdateGetSequenceState(header, serializedData, userIds, groupIds, pushText, originPeer, isFat) ⇒
      val replyTo = sender()

      pushUpdate(authId, header, serializedData, userIds, groupIds, pushText, originPeer, isFat, { seqstate: SeqState ⇒
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
    userIds:        Set[Int],
    groupIds:       Set[Int],
    pushText:       Option[String],
    originPeer:     Option[Peer],
    isFat:          Boolean
  ): Unit = {
    pushUpdate(authId, header, serializedData, userIds, groupIds, pushText, originPeer, isFat, _ ⇒ ())
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
    cb:             SeqState ⇒ Unit
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

    push(seq, timestamp) foreach (_ ⇒ cb(sequenceState(seq, timestampToBytes(timestamp))))
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

  private def sequenceState(sequence: Int, timestamp: Long): SeqState = sequenceState(sequence, timestampToBytes(timestamp))

  private def sequenceState(sequence: Int, state: Array[Byte]): SeqState = SeqState(sequence, ByteString.copyFrom(state))

  private def unreadTotal(userId: Int): DBIO[Int] = {
    val query = (for {
      d ← p.Dialog.dialogs.filter(d ⇒ d.userId === userId)
      m ← p.HistoryMessage.notDeletedMessages.filter(_.senderUserId =!= userId)
      if m.userId === d.userId && m.peerType === d.peerType && m.peerId === d.peerId && m.date > d.ownerLastReadAt
    } yield m.date).length
    query.result
  }
}