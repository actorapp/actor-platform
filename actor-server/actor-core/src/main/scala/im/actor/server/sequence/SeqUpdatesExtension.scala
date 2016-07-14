package im.actor.server.sequence

import akka.actor._
import akka.cluster.pubsub.{ DistributedPubSub, DistributedPubSubMediator }
import akka.event.Logging
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.google.protobuf.wrappers.Int32Value
import im.actor.server.db.DbExtension
import im.actor.server.migrations.v2.{ MigrationNameList, MigrationTsActions }
import im.actor.server.model._
import im.actor.server.sequence.operations.{ DeliveryOperations, DifferenceOperations, PushOperations }
import im.actor.storage.SimpleStorage
import im.actor.storage.api.{ GetAction, UpsertAction }

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future, Promise }

final case class Difference(
  updates:     IndexedSeq[SerializedUpdate],
  clientSeq:   Int,
  commonState: Array[Byte],
  needMore:    Boolean
)

/**
 * Stores mapping `authId` -> `seq` for
 * authId: Long
 * seq: Int
 */
object SequenceStorage extends SimpleStorage("sequence") {
  def getSeq(authId: Long): GetAction = {
    get(authId.toString)
  }
  def upsertSeq(authId: Long, seq: Int): UpsertAction = {
    upsert(authId.toString, Int32Value(seq).toByteArray)
  }
}

final class SeqUpdatesExtension(_system: ActorSystem)
  extends Extension
  with DeliveryOperations
  with DifferenceOperations
  with PushOperations {

  import UserSequenceCommands._

  protected val log = Logging(_system, getClass)
  protected implicit val OperationTimeout: Timeout = Timeout(20.seconds)
  private implicit val system: ActorSystem = _system
  protected implicit val ec: ExecutionContext = system.dispatcher
  protected lazy val (db, conn) = {
    val ext = DbExtension(system)
    (ext.db, ext.connector)
  }
  lazy val region: SeqUpdatesManagerRegion = SeqUpdatesManagerRegion.start()(system)
  private val writer = system.actorOf(BatchUpdatesWriter.props, "batch-updates-writer")
  private val mediator = DistributedPubSub(system).mediator

  val MultiSequenceMigrationTs: Long = {
    val optTs = MigrationTsActions.getTimestamp(MigrationNameList.MultiSequence)(conn)
    optTs.getOrElse(throw new RuntimeException(s"No Migration timestamp found for ${MigrationNameList.MultiSequence}"))
  }

  def msgDeliveryId(peer: Peer, randomId: Long) =
    s"msg_${peer.`type`.value}_${peer.id}_${randomId}"

  def getSeqState(userId: Int, authId: Long): Future[SeqState] =
    (region.ref ? Envelope(userId).withGetSeqState(GetSeqState(authId))).mapTo[SeqState]

  def addOptimizations(userId: Int, authId: Long, opts: Seq[Int]): Unit =
    region.ref ! Envelope(userId).withAddOptimizations(AddOptimizations(authId, opts))

  def registerAuthId(userId: Int, authId: Long): Unit =
    region.ref ! Envelope(userId).withRegisterAuthId(RegisterAuthId(authId))

  def reloadSettings(userId: Int): Unit =
    region.ref ! Envelope(userId).withReloadSettings(ReloadSettings())

  def subscribe(authId: Long, ref: ActorRef): Future[Unit] =
    (mediator ? DistributedPubSubMediator.Subscribe(UserSequence.topic(authId), ref)) map (_ ⇒ ())

  def buildCommonState(commonSeq: Int): CommonState = CommonState(CommonStateVersion.V1, commonSeq)

  def persistUpdate(update: SeqUpdate): Future[Unit] = {
    val promise = Promise[Unit]()
    writer ! BatchUpdatesWriter.Enqueue(update, promise)
    promise.future
  }
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
