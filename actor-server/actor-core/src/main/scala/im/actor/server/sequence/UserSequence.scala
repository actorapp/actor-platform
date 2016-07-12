package im.actor.server.sequence

import akka.actor._
import akka.http.scaladsl.util.FastFuture
import akka.pattern.pipe
import com.github.benmanes.caffeine.cache.{ Cache, Caffeine }
import com.google.protobuf.ByteString
import com.google.protobuf.wrappers.Int32Value
import im.actor.api.rpc.sequence.UpdateEmptyUpdate
import im.actor.server.db.DbExtension
import im.actor.server.model.{ SeqUpdate, UpdateMapping }
import im.actor.server.persist.{ AuthIdRepo, AuthSessionRepo }
import im.actor.server.persist.sequence.UserSequenceRepo
import im.actor.server.pubsub.PubSubExtension

import scala.collection.immutable
import scala.concurrent.Future
import scala.language.postfixOps
import scala.util.{ Failure, Success }

private trait SeqControl {
  import UserSequence._
  private var _commonSeq: Int = Const.UserSeqStart

  protected def commonSeq = this._commonSeq
  protected def commonSeq_=(seq: Int) = this._commonSeq = seq

  protected def nextCommonSeq(): Int = {
    val nseq = this._commonSeq + 1
    this._commonSeq = nseq
    nseq
  }

  private var _seqMap: immutable.Map[Long, Int] = Map.empty

  protected def seqMap = this._seqMap
  protected def seqMap_=(map: Map[Long, Int]) = this._seqMap = map

  protected def getSeq(authId: Long) = this._seqMap getOrElse (authId, Const.SeqStart)

  protected def nextSeq(authId: Long) = {
    val nextSeq = _seqMap.getOrElse(authId, Const.SeqStart) + 1
    this._seqMap += (authId → nextSeq)
    nextSeq
  }
}

object UserSequence {
  object Const {
    val UserSeqStart: Int = 0
    val SeqStart: Int = 1
    val SeqIncrement: Int = 1000
    val FlushInterval: Int = 500 // TODO: make it 250
  }

  def topic(authId: Long): String = s"sequence.$authId"

  private final case class Initialized(userSeq: Int, seqs: Map[Long, Int])

  private[sequence] def props =
    Props(new UserSequence)
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

  private val (db, conn) = {
    val ext = DbExtension(context.system)
    (ext.db, ext.connector)
  }
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
    case Initialized(userSeq, seqs) ⇒
      this.commonSeq = userSeq
      this.seqMap = seqs
      unstashAll()
      context become initialized
    case Status.Failure(e) ⇒
      log.error(e, "Failed to initialize UserSequence")
      init()
    case msg ⇒ stash()
  }

  /**
   * On actor failure we are trying to persist
   * all non-empty `seq`s for every authId.
   * This method may never been called.
   */
  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    log.error(reason, "Failure while processing {}", message)
    persistNonEmptySeqs()
    super.preRestart(reason, message)
  }

  //  TODO: figure out is it okay to access actor's state in postStop.
  //  /**
  //   * On actor stop we are trying to persist
  //   * all non-empty `seq`s for every authId.
  //   * This method may never been called.
  //   */
  //  override def postStop(): Unit = {
  //    log.warning("Stopping user sequence,trying to persist seqs")
  //    persistNonEmptySeqs()
  //    super.postStop()
  //  }

  private def initialized: Receive = {
    case cmd: VendorPushCommand ⇒ vendorPush forward cmd
    case DeliverUpdate(authId, mappingOpt, pushRules, reduceKey, deliveryId, deliveryTag) ⇒
      mappingOpt match {
        case Some(mapping) ⇒ deliver(authId, mapping, pushRules, reduceKey, deliveryId, deliveryTag)
        case None          ⇒ log.error("Empty mapping")
      }
    case GetSeqState(authId) ⇒
      sender() ! SeqState(getSeq(authId), buildCommonState())
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

  /**
   * • Fetch `userSeq`, or get `Const.UserSeqStart` if sequence isn't initialized yet.
   * • Associate every `authId` with empty optimization function and put in `authIdsOptFu` map
   * • Initialize sequence for every authId
   */
  private def init(): Unit =
    (for {
      userSeq ← db.run(UserSequenceRepo.fetchSeq(userId)) map (_ getOrElse Const.UserSeqStart)
      authIdsModels ← db.run(AuthIdRepo.findByUserId(userId))
      authIds = authIdsModels map (_.id)
      _ = this.authIdsOptFu = authIds.map(_ → Optimization.Default).toMap
      authIdsSeqs ← initializeSeqs(userSeq, authIds)
    } yield Initialized(userSeq, authIdsSeqs.toMap)) pipeTo self

  private def deliver(
    authId:      Long,
    mapping:     UpdateMapping,
    pushRules:   Option[PushRules],
    reduceKey:   Option[String],
    deliveryId:  String,
    deliveryTag: Option[String]
  ): Unit = {
    cached(authId, deliveryId) {
      nextCommonSeq()

      val optimizedMapping = applyOptimizations(deliveryTag, mapping)

      val seqUpdate = SeqUpdate(
        userId = userId,
        commonSeq = commonSeq,
        timestamp = System.currentTimeMillis(),
        reduceKey = reduceKey,
        mapping = Some(optimizedMapping)
      )

      writeToDb(seqUpdate) map { _ ⇒
        val state = buildCommonState()

        authIdsOptFu.keys foreach { authId ⇒
          val update = optimizedMapping.custom.getOrElse(authId, optimizedMapping.getDefault)
          // UpdateEmptyUpdate indicates that we don't need to push it.
          // This update won't affect sequence, and will not appear in difference.
          if (update.header != UpdateEmptyUpdate.header) {
            val seq = nextSeq(authId)
            flushSeq(authId, seq)
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

  /**
   * Initialize `seq` for every `authId`.
   * We keep compatibility with old clients(pre-multi-sequence) in following way:
   * 1. If `seq` for given `authId` exists in `SequenceStorage` - it is initialized already, sequence is: `seq` + `Const.SeqIncrement`
   * 2. If no `seq` found for given `authId`, we find `AuthSession` associated with given `authId`, and:
   *   2.1 If `AuthSession` found and `authTime` is after migration date - this is new client, sequence is: `Const.SeqStart`
   *   2.2 If `AuthSession` found, but `authTime` is before migration date - this is old client, sequence is: `commonSeq` + Const.SeqIncrement
   *   2.3 If `AuthSession` not found(what could be an error), we ASSUME this is old client, sequence is: `commonSeq` + Const.SeqIncrement
   *
   *  NOTE: After we get `seq` from storage, or create new one - we should persist it,
   *  to avoid cases, when client's seq won't be persisted until next interval point.
   */
  private def initializeSeqs(userSeq: Int, authIds: Seq[Long]): Future[Seq[(Long, Int)]] =
    Future.traverse(authIds) { authId ⇒
      val seqFu = conn.run(SequenceStorage.getSeq(authId)) flatMap {
        case Some(seqBytes) ⇒
          FastFuture.successful(Int32Value.parseFrom(seqBytes).value + Const.SeqIncrement) // 1.
        case None ⇒
          for {
            optSession ← db.run(AuthSessionRepo.findByAuthId(authId))
            seq = optSession match {
              case Some(session) ⇒
                if (session.authTime.getMillis > seqUpdExt.MultiSequenceMigrationTs)
                  Const.SeqStart // 2.1
                else
                  userSeq + Const.SeqIncrement // 2.2
              case None ⇒
                log.warning("Didn't find auth session for authId: {}, assuming it an old client!", authId)
                userSeq + Const.SeqIncrement // 2.3
            }
          } yield seq
      }

      for {
        seq ← seqFu
        _ ← conn.run(SequenceStorage.upsertSeq(authId, seq))
      } yield authId → seq
    }

  // TODO: move to SequenceStorageActions
  private def persistSeq(authId: Long, seq: Int): Future[Int] =
    conn.run(SequenceStorage.upsertSeq(authId, seq))

  /**
   * Flush `seq` for given `authId` with `Const.FlushInterval` interval.
   */
  private def flushSeq(authId: Long, seq: Int): Unit = {
    if (seq % Const.FlushInterval == 0) {
      persistSeq(authId, seq)
    }
  }

  /**
   * Persist non-empty seqs for every user's `authId`.
   */
  private def persistNonEmptySeqs(): Unit = {
    authIdsOptFu.keys foreach { authId ⇒
      val seq = getSeq(authId)
      if (seq > Const.SeqStart) {
        log.warning("Persisting current seq: {} for authId: {}", seq, authId)
        persistSeq(authId, seq)
      }
    }
  }

  private def applyOptimizations(deliveryTag: Option[String], mapping: UpdateMapping): UpdateMapping = {
    val default = mapping.getDefault
    val customOptimized = authIdsOptFu flatMap {
      case (authId, optFunc) ⇒
        val optimized = optFunc(deliveryTag.getOrElse(""))(default)
        if (optimized == default) None else Some(authId → optimized)
    }
    mapping.copy(custom = mapping.custom ++ customOptimized)
  }

  private def addAuthId(authId: Long) = {
    if (!this.authIdsOptFu.contains(authId)) {
      this.authIdsOptFu += authId → Optimization.Default
    }
    if (!this.seqMap.contains(authId)) {
      for {
        optSeq ← conn.run(SequenceStorage.getSeq(authId))
        seq = optSeq map (v ⇒ Int32Value.parseFrom(v).value) getOrElse Const.SeqStart
        _ = this.seqMap += authId → seq
        _ ← persistSeq(authId, seq)
      } yield ()
    }
  }

  private def removeAuthId(authId: Long) = {
    this.authIdsOptFu -= authId
    this.seqMap -= authId
  }

  private def buildCommonState(): ByteString =
    ByteString.copyFrom(seqUpdExt.buildCommonState(commonSeq).toByteArray)

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
