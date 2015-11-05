package im.actor.server.sequence

import akka.actor._
import akka.cluster.pubsub.DistributedPubSub
import akka.pattern.pipe
import com.github.benmanes.caffeine.cache.Caffeine
import com.google.protobuf.ByteString
import im.actor.server.db.DbExtension
import im.actor.server.model.{ UpdateMapping, SeqUpdate }
import im.actor.server.persist.sequence.UserSequenceRepo

import scala.concurrent.Future
import scala.language.postfixOps

object UserSequence {
  def topic(userId: Int): String = s"sequence.$userId"

  private final case class Initialized(seq: Int)

  private[sequence] def props = Props(new UserSequence)
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

private[sequence] final class UserSequence extends Actor with ActorLogging with Stash with SeqControl {

  import UserSequence._
  import UserSequenceCommands._
  import akka.cluster.pubsub.DistributedPubSubMediator._
  import context.dispatcher

  private val db = DbExtension(context.system).db

  val userId = self.path.name.toInt

  private val mediator = DistributedPubSub(context.system).mediator

  val deliveryCache = Caffeine.newBuilder().maximumSize(100).executor(context.dispatcher).build[String, SeqState]()

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
    case DeliverUpdate(mappingOpt, pushRules, deliveryId) ⇒
      mappingOpt match {
        case Some(mapping) ⇒ deliver(mapping, pushRules, deliveryId)
        case None ⇒
          log.error("Empty mapping")
      }
    case GetSeqState() ⇒
      sender() ! SeqState(getSeq)
  }

  private def becomeStashing(f: ActorRef ⇒ Receive): Unit =
    context.become(f(sender()) orElse stashing, discardOld = false)

  private def stashing: Receive = {
    case msg ⇒ stash()
  }

  private def init(): Unit =
    db.run(UserSequenceRepo.fetchSeq(userId)) map (seqOpt ⇒ Initialized(seqOpt.getOrElse(0))) pipeTo self

  private def deliver(mapping: UpdateMapping, pushRules: Option[PushRules], deliveryId: String): Unit = {
    cached(deliveryId) {
      val seq = nextSeq()

      val seqUpdate = SeqUpdate(
        userId,
        seq,
        System.currentTimeMillis(),
        Some(mapping)
      )

      becomeStashing(replyTo ⇒ {
        case s: SeqState ⇒
          unstashAll()
          context.unbecome()

          mediator ! Publish(topic(userId), UserSequenceEvents.NewUpdate(Some(seqUpdate), pushRules, ByteString.EMPTY))
        case s @ Status.Failure(e) ⇒
          log.error(e, "Failed to write seq update: {}", seqUpdate)
          replyTo ! s

          unstashAll()
          context.unbecome()
      })

      writeToDb(seqUpdate) map (_ ⇒ SeqState(seq)) pipeTo self
    }
  }

  private def cached(deliveryId: String)(f: ⇒ Future[SeqState]): Unit = {
    (if (deliveryId.nonEmpty) {
      Option(deliveryCache.getIfPresent(deliveryId)) match {
        case Some(seqstate) ⇒ Future.successful(seqstate)
        case None           ⇒ f
      }
    } else f) pipeTo sender() onSuccess { case s ⇒ deliveryCache.put(deliveryId, s) }
  }

  private def writeToDb(seqUpdate: SeqUpdate): Future[Unit] = db.run(UserSequenceRepo.create(seqUpdate)) map (_ ⇒ ())

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    super.preRestart(reason, message)
    log.error(reason, "Failure while processing {}", message)
  }
}