package im.actor.server.sequence

import akka.actor._
import akka.pattern.pipe
import com.github.benmanes.caffeine.cache.Caffeine
import com.google.protobuf.ByteString
import com.google.protobuf.wrappers.StringValue
import im.actor.server.db.DbExtension
import im.actor.server.model.{ SeqUpdate, UpdateMapping }
import im.actor.server.persist.sequence.UserSequenceRepo
import im.actor.server.pubsub.PubSubExtension

import scala.concurrent.Future
import scala.language.postfixOps
import scala.util.{ Failure, Success }

object UserSequence {
  def topic(userId: Int): String = s"sequence.$userId"

  private final case class Initialized(seq: Int)

  private[sequence] def props(
    googlePushManager: GooglePushManager,
    applePushManager:  ApplePushManager
  ) =
    Props(new UserSequence(googlePushManager, applePushManager))
}

private trait SeqControl {
  private var seq: Int = 0

  protected def getSeq: Int = this.seq

  protected def nextSeq(): Int = {
    val nseq = this.seq + 1
    this.seq = nseq
    nseq
  }

  protected def setSeq(s: Int): Unit = this.seq = s
}

private[sequence] final class UserSequence(
  googlePushManager: GooglePushManager,
  applePushManager:  ApplePushManager
) extends Actor with ActorLogging with Stash with SeqControl {

  import UserSequence._
  import UserSequenceCommands._
  import akka.cluster.pubsub.DistributedPubSubMediator._
  import context.dispatcher

  private val db = DbExtension(context.system).db
  private val seqUpdExt = SeqUpdatesExtension(context.system)
  private val pubSubExt = PubSubExtension(context.system)

  val userId = self.path.name.toInt

  private val deliveryCache = Caffeine.newBuilder().maximumSize(100).executor(context.dispatcher).build[String, SeqState]()

  private lazy val vendorPush = context.actorOf(VendorPush.props(userId, googlePushManager, applePushManager), "vendor-push")

  init()

  def receive = {
    case Initialized(initSeq) ⇒
      setSeq(initSeq)
      unstashAll()
      context become initialized
    case Status.Failure(e) ⇒
      log.error(e, "Failed to initialize UserSequence")
      init()
    case msg ⇒ stash()
  }

  def initialized: Receive = {
    case cmd: VendorPushCommand ⇒ vendorPush forward cmd
    case DeliverUpdate(mappingOpt, pushRules, reduceKey, deliveryId) ⇒
      mappingOpt match {
        case Some(mapping) ⇒ deliver(mapping, pushRules, reduceKey, deliveryId)
        case None ⇒
          log.error("Empty mapping")
      }
    case GetSeqState() ⇒
      sender() ! SeqState(getSeq)
  }

  private def init(): Unit =
    db.run(for {
      seq ← UserSequenceRepo.fetchSeq(userId) map (_ getOrElse 0)
    } yield Initialized(seq)) pipeTo self

  private def deliver(mapping: UpdateMapping, pushRules: Option[PushRules], reduceKey: Option[StringValue], deliveryId: String): Unit = {
    cached(deliveryId) {
      val seq = nextSeq()

      val seqUpdate = SeqUpdate(
        userId,
        seq,
        System.currentTimeMillis(),
        reduceKey,
        Some(mapping)
      )

      writeToDb(seqUpdate) map (_ ⇒ SeqState(seq)) andThen {
        case Success(_) ⇒
          pubSubExt.publish(Publish(topic(userId), UserSequenceEvents.NewUpdate(Some(seqUpdate), pushRules, None, ByteString.EMPTY)))
          vendorPush ! DeliverPush(seq, pushRules)
      }
    }
  }

  private def cached(deliveryId: String)(f: ⇒ Future[SeqState]): Unit = {
    (if (deliveryId.nonEmpty) {
      Option(deliveryCache.getIfPresent(deliveryId)) match {
        case Some(seqstate) ⇒ Future.successful(seqstate)
        case None           ⇒ f
      }
    } else f) pipeTo sender() onComplete {
      case Success(s) ⇒ deliveryCache.put(deliveryId, s)
      case Failure(e) ⇒ log.error(e, "Failed to deliver")
    }
  }

  private def writeToDb(seqUpdate: SeqUpdate): Future[Unit] = seqUpdExt.persistUpdate(seqUpdate)

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    super.preRestart(reason, message)
    log.error(reason, "Failure while processing {}", message)
  }
}