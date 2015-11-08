package im.actor.server.sequence

import akka.actor._
import akka.cluster.pubsub.{ DistributedPubSub, DistributedPubSubMediator }
import akka.pattern.ask
import akka.util.Timeout
import com.google.protobuf.ByteString
import im.actor.api.rpc.Update
import im.actor.api.rpc.messaging.UpdateMessage
import im.actor.server.db.DbExtension
import im.actor.server.model._
import im.actor.server.model.push.{ ApplePushCredentials ⇒ ApplePushCredentialsModel, GooglePushCredentials ⇒ GooglePushCredentialsModel }
import im.actor.server.persist.sequence.UserSequenceRepo
import slick.dbio.DBIO

import scala.annotation.tailrec
import scala.concurrent.duration._
import scala.concurrent.{ Future, Promise }
import scala.util.Try

final class SeqUpdatesExtension(
  _system: ActorSystem,
  gpm:     GooglePushManager,
  apm:     ApplePushManager
) extends Extension {

  import UserSequenceCommands._
  import system.dispatcher

  private implicit val OperationTimeout = Timeout(20.seconds)
  private implicit val system: ActorSystem = _system
  private implicit lazy val db = DbExtension(system).db

  lazy val region: SeqUpdatesManagerRegion = SeqUpdatesManagerRegion.start()(system, gpm, apm)

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
    pushRules:  PushRules     = PushRules(),
    deliveryId: String        = ""
  ): Future[SeqState] =
    deliverUpdate(userId, buildDeliver(mapping, pushRules, deliveryId))

  def deliverSingleUpdate(
    userId:     Int,
    update:     Update,
    pushRules:  PushRules = PushRules(),
    deliveryId: String    = ""
  ): Future[SeqState] =
    deliverUpdate(
      userId,
      UpdateMapping(default = Some(serializedUpdate(update))),
      pushRules,
      deliveryId
    )

  def broadcastSingleUpdate(
    userIds:    Set[Int],
    update:     Update,
    pushRules:  PushRules = PushRules(),
    deliveryId: String    = ""
  ): Future[Seq[SeqState]] = {
    val mapping = UpdateMapping(default = Some(serializedUpdate(update)))
    val deliver = buildDeliver(mapping, pushRules, deliveryId)
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
    pushRules:    PushRules = PushRules(),
    deliveryId:   String    = ""
  ): Future[(SeqState, Seq[SeqState])] = {
    val mapping = UpdateMapping(default = Some(serializedUpdate(update)))
    val deliver = buildDeliver(mapping, pushRules, deliveryId)
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

  val DiffStep = 100L

  def getDifference(userId: Int, seq: Int, authSid: Int, maxSizeInBytes: Long): Future[(IndexedSeq[SeqUpdate], Boolean)] = {
    def run(seq: Int, acc: IndexedSeq[SeqUpdate], currentSize: Long): DBIO[(IndexedSeq[SeqUpdate], Boolean)] = {
      UserSequenceRepo.fetchAfterSeq(userId, seq, DiffStep).flatMap { updates ⇒
        if (updates.isEmpty) {
          DBIO.successful(acc → false)
        } else {
          val (newAcc, newSize, allFit) = append(updates.toVector, currentSize, maxSizeInBytes, acc, authSid)
          if (allFit) {
            newAcc.lastOption match {
              case Some(u) ⇒ run(u.seq, newAcc, newSize)
              case None    ⇒ DBIO.successful(acc → false)
            }
          } else {
            DBIO.successful(newAcc → true)
          }
        }
      }
    }
    db.run(run(seq, Vector.empty[SeqUpdate], 0L))
  }

  private def append(updates: IndexedSeq[SeqUpdate], currentSize: Long, maxSizeInBytes: Long, updateAcc: IndexedSeq[SeqUpdate], authSid: Int): (IndexedSeq[SeqUpdate], Long, Boolean) = {
    @tailrec
    def run(updLeft: IndexedSeq[SeqUpdate], acc: IndexedSeq[SeqUpdate], currSize: Long): (IndexedSeq[SeqUpdate], Long, Boolean) = {
      updLeft match {
        case h +: t ⇒
          val upd = h.getMapping.custom.getOrElse(authSid, h.getMapping.getDefault)
          val newSize = currSize + upd.body.size()
          if (newSize > maxSizeInBytes && acc.nonEmpty) {
            (acc, currSize, false)
          } else {
            run(t, acc :+ h, newSize)
          }
        case Vector() ⇒ (acc, currSize, true)
      }
    }
    run(updates, updateAcc, currentSize)
  }

  private def buildDeliver(mapping: UpdateMapping, pushRules: PushRules, deliveryId: String): DeliverUpdate =
    DeliverUpdate(
      mapping = Some(mapping),
      pushRules = Some(pushRules),
      deliveryId = deliveryId
    )

  def registerGooglePushCredentials(authId: Long, creads: GooglePushCredentialsModel) = Future.successful(())

  def registerApplePushCredentials(authId: Long, creads: ApplePushCredentialsModel) = Future.successful(())

  def deletePushCredentials(userId: Int, authSid: Int): Future[Unit] = Future.successful(())

  def deletePushCredentials(authId: Long): Future[Unit] = Future.successful(())

  def deleteApplePushCredentials(token: Array[Byte]): Future[Unit] = Future.successful(())

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
    val applePushConfig = ApplePushManagerConfig.load(
      Try(system.settings.config.getConfig("services.apple.push"))
        .getOrElse(system.settings.config.getConfig("push.apple"))
    )
    val googlePushConfig = GooglePushManagerConfig.load(system.settings.config.getConfig("services.google.push")).get

    val gpm = new GooglePushManager(googlePushConfig)
    val apm = new ApplePushManager(applePushConfig, system)

    new SeqUpdatesExtension(system, gpm, apm)
  }
}