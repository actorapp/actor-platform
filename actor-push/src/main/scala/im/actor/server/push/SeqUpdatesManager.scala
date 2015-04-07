package im.actor.server.push

import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit

import scala.concurrent._
import scala.concurrent.duration._
import scala.util.{ Failure, Success }

import akka.actor._
import akka.contrib.pattern.{ ClusterSharding, ShardRegion }
import akka.pattern.{ ask, pipe }
import akka.persistence._
import akka.util.Timeout
import slick.dbio.DBIO
import slick.driver.PostgresDriver.api._

import im.actor.api.{ rpc => api }
import im.actor.server.{ models, persist => p }

object SeqUpdatesManager {

  @SerialVersionUID(1L)
  private[push] case class Envelope(authId: Long, payload: Message)

  sealed trait Message

  @SerialVersionUID(1L)
  private[push] case object GetSequenceState extends Message

  @SerialVersionUID(1L)
  private[push] case class PushUpdate(header: Int, serializedData: Array[Byte]) extends Message

  @SerialVersionUID(1L)
  private[push] case class PushUpdateGetSequenceState(header: Int, serializedData: Array[Byte]) extends Message

  type Sequence = Int
  type SequenceState = (Int, Array[Byte])

  private[push] sealed trait PersistentEvent

  @SerialVersionUID(1L)
  private[push] case class SeqChanged(sequence: Int) extends PersistentEvent

  private[push] val noop1: Any => Unit = _ => ()

  private[this] val idExtractor: ShardRegion.IdExtractor = {
    case env @ Envelope(authId, payload) => (authId.toString, env)
  }

  private[this] val shardResolver: ShardRegion.ShardResolver = msg => msg match {
    case Envelope(authId, _) => (authId % 32).toString // TODO: configurable
  }

  private[this] def startRegion(props: Option[Props])(implicit system: ActorSystem): ActorRef = ClusterSharding(system).start(
    typeName = "SeqUpdatesManager",
    entryProps = props,
    idExtractor = idExtractor,
    shardResolver = shardResolver
  )

  // TODO: configurable
  private val OperationTimeout = Timeout(5.seconds)

  def startRegion()(implicit system: ActorSystem, db: Database): ActorRef = startRegion(Some(Props(classOf[SeqUpdatesManager], db)))

  def startRegionProxy()(implicit system: ActorSystem): ActorRef = startRegion(None)

  def getSeqState(region: ActorRef, authId: Long)(implicit ec: ExecutionContext): DBIO[(Sequence, Array[Byte])] = {
    for {
      seqstate <- DBIO.from(region.ask(Envelope(authId, GetSequenceState))(OperationTimeout).mapTo[SequenceState])
    } yield seqstate
  }

  def persistAndPushUpdate(region: ActorRef, authId: Long, header: Int, serializedData: Array[Byte])(implicit ec: ExecutionContext): DBIO[SequenceState] = {
    DBIO.from(pushUpdateGetSeqState(region, authId, header, serializedData))
  }

  def broadcastUserUpdate(region: ActorRef,
                          userId: Int,
                          update: api.Update)(implicit ec: ExecutionContext): DBIO[Seq[SequenceState]] = {
    val header = update.header
    val serializedData = update.toByteArray

    for {
      authIds <- p.AuthId.findIdByUserId(userId)
      seqstates <- DBIO.sequence(authIds map (persistAndPushUpdate(region, _, header, serializedData)))
    } yield seqstates
  }

  def broadcastClientUpdate(region: ActorRef, update: api.Update)(
    implicit
    client: api.AuthorizedClientData, ec: ExecutionContext
    ): DBIO[SequenceState] = {
    val header = update.header
    val serializedData = update.toByteArray

    for {
      otherAuthIds <- p.AuthId.findIdByUserId(client.userId).map(_.view.filter(_ != client.authId))
      _ <- DBIO.sequence(otherAuthIds map (authId => persistAndPushUpdate(region, authId, header, serializedData)))
      ownseqstate <- persistAndPushUpdate(region, client.authId, header, serializedData)
    } yield ownseqstate
  }

  private def pushUpdateGetSeqState(region: ActorRef, authId: Long, header: Int, serializedData: Array[Byte]): Future[SequenceState] = {
    region.ask(Envelope(authId, PushUpdateGetSequenceState(header, serializedData)))(OperationTimeout).mapTo[SequenceState]
  }

  private def pushUpdate(region: ActorRef, authId: Long, header: Int, serializedData: Array[Byte]): Unit = {
    region ! Envelope(authId, PushUpdate(header, serializedData))
  }
}

class SeqUpdatesManager(db: Database) extends PersistentActor with Stash with ActorLogging {

  import ShardRegion.Passivate

  import SeqUpdatesManager._

  @SerialVersionUID(1L)
  private case class Initiated(authId: Long, timestamp: Long)

  // FIXME: move to props
  val receiveTimeout = context.system.settings.config.getDuration("push.seq-updates-manager.receive-timeout", TimeUnit.SECONDS).seconds

  context.setReceiveTimeout(receiveTimeout)

  override def persistenceId: String = self.path.parent.name + "-" + self.path.name

  private[this] val IncrementOnStart: Int = 1000
  require(IncrementOnStart > 1)
  // it is needed to prevent divizion by zero in pushUpdate

  private[this] var seq: Int = 0
  private[this] var lastTimestamp: Long = 0 // TODO: feed this value from db on actor startup

  def receiveInitiated: Receive = {
    case Envelope(_, GetSequenceState) =>
      sender() ! sequenceState(seq, timestampToBytes(lastTimestamp))
    case Envelope(authId, PushUpdate(header, updBytes)) =>
      pushUpdate(authId, header, updBytes)
    case Envelope(authId, PushUpdateGetSequenceState(header, serializedData)) =>
      val replyTo = sender()

      pushUpdate(authId, header, serializedData, { seqstate: SequenceState =>
        replyTo ! seqstate
      })
    case ReceiveTimeout => context.parent ! Passivate(stopMessage = PoisonPill)
  }

  def stashing: Receive = {
    case Initiated(authId, timestamp) =>
      lastTimestamp = timestamp

      unstashAll()
      context.become(receiveInitiated)
    case msg => stash()
  }

  def waitingForEnvelope: Receive = {
    case env @ Envelope(authId, _) =>
      stash()
      context.become(stashing)

      // TODO: pinned diepstcher?
      implicit val ec = context.dispatcher

      val timestampFuture: Future[Long] = for {
        seqUpdOpt <- db.run(p.sequence.SeqUpdate.find(authId).headOption)
      } yield {
          seqUpdOpt.map(_.timestamp).getOrElse(0)
        }

      timestampFuture.onFailure {
        case e =>
          log.error(e, "Failed loading last update")
          context.parent ! Passivate(stopMessage = PoisonPill)
      }

      timestampFuture.map(Initiated(authId, _)).pipeTo(self)
    case msg => stash()
  }

  override def receiveCommand: Receive = waitingForEnvelope

  override def receiveRecover: Receive = {
    case SeqChanged(value) =>
      seq = value
    case RecoveryCompleted =>
      seq += IncrementOnStart - 1
  }

  private def pushUpdate(authId: Long, header: Int, serializedData: Array[Byte]): Unit = {
    pushUpdate(authId, header, serializedData, noop1)
  }

  private def pushUpdate(authId: Long, header: Int, serializedData: Array[Byte], cb: SequenceState => Unit): Unit = {
    // TODO: pinned dispatcher?
    implicit val ec = context.dispatcher

    def push(seq: Int, timestamp: Long): Future[Unit] = {
      // TODO: push it

      db.run(p.sequence.SeqUpdate.create(models.sequence.SeqUpdate(authId, timestamp, seq, header, serializedData)))
        .map(_ => ())
        .andThen {
        case Success(_) => log.debug("Pushed update seq: {}", seq)
        case Failure(err) => log.error(err, "Failed to push update") // TODO: throw exception?
      }
    }

    seq += 1
    val timestamp = newTimestamp()

    log.debug("new timestamp {}", timestamp)

    // TODO: DRY this
    if (seq % (IncrementOnStart / 2) == 0) {
      persist(SeqChanged(seq)) { _ =>
        push(seq, timestamp) foreach (_ => cb(sequenceState(seq, timestampToBytes(timestamp))))
      }
    } else {
      push(seq, timestamp) foreach (_ => cb(sequenceState(seq, timestampToBytes(timestamp))))
    }
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

  private def timestampToBytes(timestamp: Long): Array[Byte] = {
    ByteBuffer.allocate(java.lang.Long.BYTES).putLong(timestamp).array()
  }

  private def sequenceState(sequence: Int, timestamp: Long): SequenceState =
    sequenceState(sequence, timestampToBytes(timestamp))

  private def sequenceState(sequence: Int, state: Array[Byte]): SequenceState =
    (sequence, state)
}
