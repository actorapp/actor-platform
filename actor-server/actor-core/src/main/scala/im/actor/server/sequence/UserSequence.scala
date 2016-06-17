package im.actor.server.sequence

import akka.actor._
import akka.http.scaladsl.util.FastFuture
import akka.pattern.pipe
import com.github.benmanes.caffeine.cache.{ Cache, Caffeine }
import com.google.protobuf.ByteString
import com.google.protobuf.wrappers.{ Int32Value, StringValue }
import im.actor.api.rpc.sequence.UpdateEmptyUpdate
import im.actor.server.db.DbExtension
import im.actor.server.model.{ AuthId, SeqUpdate, UpdateMapping }
import im.actor.server.persist.AuthIdRepo
import im.actor.server.persist.sequence.UserSequenceRepo
import im.actor.server.pubsub.PubSubExtension

import scala.concurrent.Future
import scala.language.postfixOps
import scala.util.{ Failure, Success }

object UserSequence {
  def topic(authId: Long): String = s"sequence.$authId"

  private final case class Initialized(commonSeq: Int, seqs: Map[Long, Int])

  private[sequence] def props =
    Props(new UserSequence)
}

private trait SeqControl {
  private var _commonSeq: Int = 0

  protected def commonSeq = this._commonSeq
  protected def commonSeq_=(seq: Int) = this._commonSeq = seq

  protected def nextCommonSeq(): Int = {
    val nseq = this._commonSeq + 1
    this._commonSeq = nseq
    nseq
  }

  private var _seqMap: Map[Long, Int] = Map.empty

  protected def seqMap = this._seqMap
  protected def seqMap_=(map: Map[Long, Int]) = this._seqMap = map

  protected def getSeq(authId: Long) = seqMap getOrElse (authId, 0)

  protected def nextSeq(authId: Long) = {
    val nextSeq = _seqMap.getOrElse(authId, 0) + 1
    this._seqMap += (authId → nextSeq)
    nextSeq
  }
}

private[sequence] final class UserSequence
  extends Actor
  with ActorLogging
  with Stash
  with SeqControl {

  import UserSequence._
  import UserSequenceCommands._
  import akka.cluster.pubsub.DistributedPubSubMediator._
  import context.dispatcher

  private val db = DbExtension(context.system).db
  private val connector = DbExtension(context.system).connector
  private val seqUpdExt = SeqUpdatesExtension(context.system)
  private val pubSubExt = PubSubExtension(context.system)

  private var authIdsOptFu = Map.empty[Long, Optimization.Func]

  val userId = self.path.name.toInt

  private val deliveryCache: Cache[String, SeqState] =
    Caffeine
      .newBuilder()
      .maximumSize(500)
      .executor(context.dispatcher)
      .build[String, SeqState]()

  private lazy val vendorPush = context.actorOf(VendorPush.props(userId), "vendor-push")

  init()

  def receive = {
    case Initialized(commonSeq, seqs) ⇒
      this.commonSeq = commonSeq
      this.seqMap = seqs
      unstashAll()
      context become initialized
    case Status.Failure(e) ⇒
      log.error(e, "Failed to initialize UserSequence")
      init()
    case msg ⇒ stash()
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    log.error(reason, "Failure while processing {}", message)
    authIdsOptFu.keys foreach { authId ⇒
      val seq = getSeq(authId)
      if (seq != 0) {
        log.warning("Persisting current seq: {} for authId: {}", seq, authId)
        connector.run(SeqStorage.putSeq(authId, seq))
      }
    }
    super.preRestart(reason, message)
  }

  private def initialized: Receive = {
    case cmd: VendorPushCommand ⇒ vendorPush forward cmd
    case DeliverUpdate(authId, mappingOpt, pushRules, reduceKey, deliveryId) ⇒
      mappingOpt match {
        case Some(mapping) ⇒ deliver(authId, mapping, pushRules, reduceKey, deliveryId)
        case None          ⇒ log.error("Empty mapping")
      }
    case GetSeqState(authId) ⇒
      sender() ! SeqState(getSeq(authId), commonState())
    case RegisterAuthId(authId) ⇒
      addAuthId(authId)
    case UnregisterAuthId(authId) ⇒
      if (this.seqMap.contains(authId)) {
        removeAuthId(authId)
      }
      sender() ! UnregisterAuthIdAck()
    case AddOptimizations(authId, optimizations) ⇒
      this.authIdsOptFu += authId → Optimization(optimizations)
  }

  private def init(): Unit =
    (for {
      authIds ← db.run(AuthIdRepo.findByUserId(userId))
      _ = this.authIdsOptFu = authIds.map(_.id → Optimization.EmptyFunc).toMap
      authIdsSeqs ← Future.traverse(authIds) {
        case AuthId(id, _, _) ⇒
          connector.run(SeqStorage.getSeq(id)) map (v ⇒ id → (v map (Int32Value.parseFrom(_).value) getOrElse 0))
      }
      commonSeq ← db.run(UserSequenceRepo.fetchSeq(userId)) map (_ getOrElse 0)
    } yield Initialized(commonSeq, authIdsSeqs.toMap)) pipeTo self

  private def deliver(
    authId:     Long,
    mapping:    UpdateMapping,
    pushRules:  Option[PushRules],
    reduceKey:  Option[StringValue],
    deliveryId: String
  ): Unit = {
    cached(authId, deliveryId) {
      nextCommonSeq()

      val optimizedMapping = applyOptimizations(mapping)

      val seqUpdate = SeqUpdate(
        userId = userId,
        commonSeq = commonSeq,
        timestamp = System.currentTimeMillis(),
        reduceKey = reduceKey,
        mapping = Some(optimizedMapping)
      )

      writeToDb(seqUpdate) map { _ ⇒
        val state = commonState()

        authIdsOptFu.keys foreach { authId ⇒
          val update = optimizedMapping.custom.getOrElse(authId, optimizedMapping.getDefault)
          // empty update indicates that we don't need to push it.
          // this update won't affect sequence, and will not appear in difference.
          if (update.header != UpdateEmptyUpdate.header) {
            val seq = nextSeq(authId)
            // it it ok to persist this way?
            if (seq % 50 == 0) {
              connector.run(SeqStorage.putSeq(authId, seq))
            }
            pubSubExt.publish(
              Publish(
                topic(authId),
                UserSequenceEvents.NewUpdate(Some(update), seq, state, pushRules, None)
              )
            )
            vendorPush ! DeliverPush(authId, seq, pushRules)
          }
        }
        SeqState(getSeq(authId), state)
      }
    }
  }

  private def applyOptimizations(mapping: UpdateMapping): UpdateMapping = {
    val default = mapping.getDefault
    val customOptimized = authIdsOptFu flatMap {
      case (authId, optFunc) ⇒
        val optimized = optFunc(default)
        if (optimized == default) None else Some(authId → optimized)
    }
    mapping.copy(custom = mapping.custom ++ customOptimized)
  }

  private def addAuthId(authId: Long) = {
    if (!this.authIdsOptFu.contains(authId)) {
      this.authIdsOptFu += authId → Optimization.EmptyFunc
    }
    if (!this.seqMap.contains(authId)) {
      for {
        optSeq ← connector.run(SeqStorage.getSeq(authId))
        seq = optSeq map (v ⇒ Int32Value.parseFrom(v).value) getOrElse 0
        _ = this.seqMap += authId → seq
      } yield ()
    }
  }

  private def removeAuthId(authId: Long) = {
    this.authIdsOptFu -= authId
    this.seqMap -= authId
  }

  private def commonState(): ByteString =
    ByteString.copyFrom(seqUpdExt.commonState(commonSeq).toByteArray)

  private def cached(authId: Long, deliveryId: String)(f: ⇒ Future[SeqState]): Unit = {
    val deliveryKey = s"${authId}_${deliveryId}"
    (if (deliveryId.nonEmpty)
      Option(deliveryCache.getIfPresent(deliveryKey)) match {
      case Some(seqState) ⇒ FastFuture.successful(seqState)
      case None           ⇒ f
    }
    else f) pipeTo sender() onComplete {
      case Success(s) ⇒ deliveryCache.put(deliveryKey, s)
      case Failure(e) ⇒ log.error(e, "Failed to deliver")
    }
  }

  private def writeToDb(seqUpdate: SeqUpdate): Future[Unit] = seqUpdExt.persistUpdate(seqUpdate)

}
