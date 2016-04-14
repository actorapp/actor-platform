package im.actor.server.sequence

import akka.actor._
import akka.cluster.pubsub.{ DistributedPubSub, DistributedPubSubMediator }
import akka.event.Logging
import akka.http.scaladsl.util.FastFuture
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.google.protobuf.ByteString
import com.google.protobuf.wrappers.StringValue
import im.actor.api.rpc.Update
import im.actor.api.rpc.messaging.UpdateMessage
import im.actor.server.db.DbExtension
import im.actor.server.model._
import im.actor.server.model.push.{ PushCredentials, ActorPushCredentials ⇒ ActorPushCredentialsModel, ApplePushCredentials ⇒ ApplePushCredentialsModel, GooglePushCredentials ⇒ GooglePushCredentialsModel }
import im.actor.server.persist.AuthSessionRepo
import im.actor.server.persist.push.{ ActorPushCredentialsRepo, ApplePushCredentialsRepo, GooglePushCredentialsRepo }
import im.actor.server.persist.sequence.UserSequenceRepo
import scodec.bits.BitVector
import slick.dbio.DBIO

import scala.annotation.tailrec
import scala.collection.immutable
import scala.concurrent.duration._
import scala.concurrent.{ Future, Promise }

final class SeqUpdatesExtension(_system: ActorSystem) extends Extension {

  import UserSequenceCommands._
  import system.dispatcher

  private val log = Logging(_system, getClass)
  private implicit val OperationTimeout = Timeout(20.seconds)
  private implicit val system: ActorSystem = _system
  private implicit lazy val db = DbExtension(system).db
  lazy val region: SeqUpdatesManagerRegion = SeqUpdatesManagerRegion.start()(system)
  private val writer = system.actorOf(BatchUpdatesWriter.props, "batch-updates-writer")
  private val mediator = DistributedPubSub(system).mediator

  def getSeqState(userId: Int): Future[SeqState] =
    (region.ref ? Envelope(userId).withGetSeqState(GetSeqState())).mapTo[SeqState]

  def deliverUpdate(
    userId:  Int,
    deliver: DeliverUpdate
  ): Future[SeqState] =
    (region.ref ? Envelope(userId).withDeliverUpdate(deliver)).mapTo[SeqState]

  def deliverUpdate(
    userId:     Int,
    mapping:    UpdateMapping,
    pushRules:  PushRules      = PushRules(),
    reduceKey:  Option[String] = None,
    deliveryId: String         = ""
  ): Future[SeqState] =
    deliverUpdate(userId, buildDeliver(mapping, pushRules, reduceKey, deliveryId))

  def deliverSingleUpdate(
    userId:     Int,
    update:     Update,
    pushRules:  PushRules      = PushRules(),
    reduceKey:  Option[String] = None,
    deliveryId: String         = ""
  ): Future[SeqState] =
    deliverUpdate(
      userId,
      UpdateMapping(default = Some(serializedUpdate(update))),
      pushRules,
      reduceKey,
      deliveryId
    )

  def broadcastSingleUpdate(
    userIds:    Set[Int],
    update:     Update,
    pushRules:  PushRules      = PushRules(),
    reduceKey:  Option[String] = None,
    deliveryId: String         = ""
  ): Future[Seq[SeqState]] = {
    val mapping = UpdateMapping(default = Some(serializedUpdate(update)))
    val deliver = buildDeliver(mapping, pushRules, reduceKey, deliveryId)
    broadcastSingleUpdate(userIds, deliver)
  }

  def broadcastSingleUpdate(
    userIds: Set[Int],
    deliver: DeliverUpdate
  ): Future[Seq[SeqState]] =
    Future.sequence(userIds.toSeq map (deliverUpdate(_, deliver)))

  def broadcastOwnSingleUpdate(
    userId:       Int,
    bcastUserIds: Set[Int],
    update:       Update,
    pushRules:    PushRules      = PushRules(),
    reduceKey:    Option[String] = None,
    deliveryId:   String         = ""
  ): Future[(SeqState, Seq[SeqState])] = {
    val mapping = UpdateMapping(default = Some(serializedUpdate(update)))
    val deliver = buildDeliver(mapping, pushRules, reduceKey, deliveryId)
    for {
      seqstate ← deliverUpdate(userId, deliver)
      seqstates ← broadcastSingleUpdate(bcastUserIds, deliver)
    } yield (seqstate, seqstates)
  }

  def deliverMappedUpdate(
    userId:     Int,
    default:    Option[Update],
    custom:     Map[Int, Update],
    pushRules:  PushRules        = PushRules(),
    deliveryId: String           = ""
  ): Future[SeqState] = deliverUpdate(
    userId,
    UpdateMapping(
      default = default map serializedUpdate,
      custom = custom mapValues serializedUpdate
    ),
    pushRules = pushRules,
    deliveryId = deliveryId
  )

  def deliverAuthIdMappedUpdate(
    userId:     Int,
    default:    Option[Update],
    custom:     Map[Long, Update],
    pushRules:  PushRules         = PushRules(),
    deliveryId: String            = ""
  ): Future[SeqState] =
    for {
      map ← db.run(AuthSessionRepo.findIdsByAuthIds(custom.keySet)) map (_.toMap)
      res ← deliverMappedUpdate(
        userId,
        default,
        custom map { case (authId, u) ⇒ map.getOrElse(authId, throw new RuntimeException("AuthId not found")) → u },
        pushRules,
        deliveryId
      )
    } yield res

  val DiffStep = 100L

  private type ReduceKey = String
  private case class DiffAcc(
    generic: immutable.SortedMap[Int, SeqUpdate] = immutable.TreeMap.empty,
    reduced: Map[ReduceKey, (Int, SeqUpdate)]    = Map.empty
  ) {
    def nonEmpty = generic.nonEmpty || reduced.nonEmpty

    def isEmpty = generic.isEmpty && reduced.isEmpty

    lazy val toVector = (generic ++ reduced.values).values.toVector
  }

  def getDifference(userId: Int, seq: Int, authSid: Int, maxSizeInBytes: Long): Future[(IndexedSeq[SeqUpdate], Boolean)] = {
    def run(seq: Int, acc: DiffAcc, currentSize: Long): DBIO[(DiffAcc, Boolean)] = {
      UserSequenceRepo.fetchAfterSeq(userId, seq, DiffStep).flatMap { updates ⇒
        if (updates.isEmpty) {
          DBIO.successful(acc → false)
        } else {
          val (newAcc, newSize, allFit) = append(updates.toList, currentSize, maxSizeInBytes, acc, authSid)
          if (allFit) {
            newAcc.toVector.lastOption match {
              case Some(u) ⇒ run(u.seq, newAcc, newSize)
              case None    ⇒ DBIO.successful(acc → false)
            }
          } else {
            DBIO.successful(newAcc → true)
          }
        }
      }
    }
    for {
      (acc, needMore) ← db.run(run(seq, DiffAcc(), 0L))
    } yield (acc.toVector, needMore)
  }

  private def append(
    updates:        List[SeqUpdate],
    currentSize:    Long,
    maxSizeInBytes: Long,
    updateAcc:      DiffAcc,
    authSid:        Int
  ): (DiffAcc, Long, Boolean) = {
    @tailrec
    def run(updLeft: List[SeqUpdate], acc: DiffAcc, currSize: Long): (DiffAcc, Long, Boolean) = {
      updLeft match {
        case h :: t ⇒
          val upd = h.getMapping.custom.getOrElse(authSid, h.getMapping.getDefault)
          val newSize = currSize + upd.body.size()
          if (newSize > maxSizeInBytes && acc.nonEmpty) {
            (acc, currSize, false)
          } else {
            val reduceKeyOpt = h.reduceKey map (_.value)
            val newAcc = reduceKeyOpt match {
              case None            ⇒ acc.copy(generic = acc.generic + (h.seq → h))
              case Some(reduceKey) ⇒ acc.copy(reduced = acc.reduced + (reduceKey → (h.seq → h)))
            }

            run(t, newAcc, newSize)
          }
        case Nil ⇒ (acc, currSize, true)
      }
    }
    run(updates, updateAcc, currentSize)
  }

  private def buildDeliver(mapping: UpdateMapping, pushRules: PushRules, reduceKey: Option[String], deliveryId: String): DeliverUpdate =
    DeliverUpdate(
      mapping = Some(mapping),
      pushRules = Some(pushRules),
      reduceKey map (StringValue(_)),
      deliveryId = deliveryId
    )

  def registerGooglePushCredentials(creds: GooglePushCredentialsModel) =
    registerPushCredentials(creds.authId, RegisterPushCredentials().withGoogle(creds))

  def registerApplePushCredentials(creds: ApplePushCredentialsModel) =
    registerPushCredentials(creds.authId, RegisterPushCredentials().withApple(creds))

  def registerActorPushCredentials(creds: ActorPushCredentialsModel) =
    registerPushCredentials(creds.authId, RegisterPushCredentials().withActor(creds))

  // TODO: real future
  private def registerPushCredentials(authId: Long, register: RegisterPushCredentials) =
    withAuthSession(authId) { session ⇒
      region.ref ! Envelope(session.userId).withRegisterPushCredentials(register)
      Future.successful(())
    }

  def unregisterAllPushCredentials(authId: Long): Future[Unit] =
    findAllPushCredentials(authId) map { creds ⇒
      creds map (c ⇒ unregisterPushCredentials(c.authId, makeUnregister(c)))
    }

  def unregisterActorPushCredentials(endpoint: String): Future[Unit] =
    db.run(ActorPushCredentialsRepo.findByTopic(endpoint).headOption) flatMap {
      case Some(creds) ⇒
        unregisterPushCredentials(creds.authId, UnregisterPushCredentials().withActor(creds))
      case None ⇒
        log.warning("Actor push credentials not found for endpoint: {}", endpoint)
        FastFuture.successful(())
    }

  def unregisterApplePushCredentials(token: Array[Byte]): Future[Unit] =
    db.run(ApplePushCredentialsRepo.findByToken(token).headOption) flatMap {
      case Some(creds) ⇒
        unregisterPushCredentials(creds.authId, UnregisterPushCredentials().withApple(creds))
      case None ⇒
        log.warning("Apple push credentials not found for token: {}", BitVector(token).toHex)
        FastFuture.successful(())
    }

  def unregisterGooglePushCredentials(token: String): Future[Unit] =
    db.run(GooglePushCredentialsRepo.findByToken(token)) flatMap {
      case Some(creds) ⇒
        unregisterPushCredentials(creds.authId, UnregisterPushCredentials().withGoogle(creds))
      case None ⇒
        log.warning("Google push credentials not found for token: {}", token)
        FastFuture.successful(())
    }

  private def unregisterPushCredentials(authId: Long, unregister: UnregisterPushCredentials): Future[Unit] =
    withAuthSession(authId) { session ⇒
      (region.ref ? Envelope(session.userId).withUnregisterPushCredentials(unregister)).mapTo[UnregisterPushCredentialsAck] map (_ ⇒ ())
    }

  private def makeUnregister: PartialFunction[PushCredentials, UnregisterPushCredentials] = {
    case actor: ActorPushCredentialsModel   ⇒ UnregisterPushCredentials().withActor(actor)
    case apple: ApplePushCredentialsModel   ⇒ UnregisterPushCredentials().withApple(apple)
    case google: GooglePushCredentialsModel ⇒ UnregisterPushCredentials().withGoogle(google)
  }

  private def findAllPushCredentials(authId: Long): Future[Seq[PushCredentials]] =
    db.run(for {
      google ← GooglePushCredentialsRepo.find(authId)
      apple ← ApplePushCredentialsRepo.find(authId)
      actor ← ActorPushCredentialsRepo.find(authId)
    } yield Seq(google, apple, actor).flatten)

  private def withAuthSession[A](authId: Long)(f: AuthSession ⇒ Future[A]): Future[A] = {
    db.run(AuthSessionRepo.findByAuthId(authId)) flatMap {
      case Some(session) ⇒ f(session)
      case None ⇒
        val err = new RuntimeException("AuthSession not found")
        log.error(err, err.getMessage)
        throw err
    }
  }

  def reloadSettings(userId: Int): Unit =
    region.ref ! Envelope(userId).withReloadSettings(ReloadSettings())

  def persistUpdate(update: SeqUpdate): Future[Unit] = {
    val promise = Promise[Unit]()
    writer ! BatchUpdatesWriter.Enqueue(update, promise)
    promise.future
  }

  def getOriginPeer(update: Update): Option[Peer] =
    update match {
      case u: UpdateMessage ⇒ Some(Peer(PeerType.fromValue(u.peer.`type`.id), u.peer.id))
      case _                ⇒ None
    }

  def subscribe(userId: Int, ref: ActorRef): Future[Unit] =
    (mediator ? DistributedPubSubMediator.Subscribe(UserSequence.topic(userId), ref)) map (_ ⇒ ())

  private def serializedUpdate(u: Update): SerializedUpdate = SerializedUpdate(u.header, ByteString.copyFrom(u.toByteArray), u._relatedUserIds, u._relatedGroupIds)
}

object SeqUpdatesExtension extends ExtensionId[SeqUpdatesExtension] with ExtensionIdProvider {
  override def lookup = SeqUpdatesExtension

  override def createExtension(system: ExtendedActorSystem) = {
    implicit val _system = system
    implicit val mat = ActorMaterializer()
    val log = Logging(system, getClass)
    new SeqUpdatesExtension(system)
  }
}