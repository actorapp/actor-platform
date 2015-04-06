package im.actor.server.push

import java.util.concurrent.TimeUnit

import scala.concurrent._, duration._

import akka.actor._
import akka.contrib.pattern.ClusterSharding
import akka.contrib.pattern.ShardRegion
import akka.pattern.ask
import akka.persistence._
import akka.util.Timeout
import slick.dbio.DBIO

import im.actor.api.{ rpc => api }
import im.actor.server.models
import im.actor.server.persist

object SeqUpdatesManager {

  sealed trait Message

  @SerialVersionUID(1L)
  private[push] case object GetSeq extends Message

  @SerialVersionUID(1L)
  private[push] case class PushUpdate(upd: api.Update) extends Message

  @SerialVersionUID(1L)
  private[push] case class PushUpdateGetSeq(upd: api.Update) extends Message

  @SerialVersionUID(1L)
  private[push] case class Envelope(authId: Long, payload: Message)

  @SerialVersionUID(1L)
  private[push] case object Stop

  type SeqVal = Int
  type SeqValState = (Int, Array[Byte])

  private[push] sealed trait PersistentEvent

  @SerialVersionUID(1L)
  private[push] case class SeqChanged(value: Int) extends PersistentEvent

  private[push] val noop1: Any => Unit = _ => ()

  private[this] val idExtractor: ShardRegion.IdExtractor = {
    case env@Envelope(authId, payload) => (authId.toString, env)
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
  val GetSeqTimeout = Timeout(5.seconds)
  val PushGetSeqTimeout = Timeout(5.seconds)

  def startRegion()(implicit system: ActorSystem): ActorRef = startRegion(Some(Props[SeqUpdatesManager]))

  def startRegionProxy()(implicit system: ActorSystem): ActorRef = startRegion(None)

  def getSeqState(region: ActorRef, authId: Long)(implicit ec: ExecutionContext): DBIO[(SeqVal, Array[Byte])] = {
    for {
      seq <- DBIO.from(region.ask(Envelope(authId, GetSeq))(GetSeqTimeout).mapTo[SeqVal])
      state <- persist.sequence.SeqUpdate.find(authId).headOption.map(optU => optU.map(_.ref.toByteArray).getOrElse(Array.empty[Byte]))
    } yield (seq, state)
  }

  def pushUpdate(region: ActorRef, authId: Long, update: api.Update): Unit = {
    region ! Envelope(authId, PushUpdate(update))
  }

  def persistAndPushUpdate(region: ActorRef, authId: Long, update: api.Update)(implicit ec: ExecutionContext): DBIO[SeqValState] = {
    val header = update.header
    val serializedData = update.toByteArray

    DBIO.from(pushUpdateGetSeq(region, authId, update)).flatMap(persistAndPushUpdate(region, authId, _, header, serializedData))
  }

  def persistAndPushUpdate(region: ActorRef, authId: Long, seq: Int, header: Int, serializedData: Array[Byte])(implicit ec: ExecutionContext): DBIO[SeqValState] = {
    val seqUpdate = models.sequence.SeqUpdate(authId, seq, header, serializedData)

    for {
      _ <- persist.sequence.SeqUpdate.create(seqUpdate)
    } yield (seq, seqUpdate.ref.toByteArray)
  }

  def broadcastUserUpdate(region: ActorRef,
                          userId: Int,
                          update: api.Update)(implicit ec: ExecutionContext): DBIO[Seq[SeqValState]] = {
    val header = update.header
    val serializedData = update.toByteArray

    for {
      authIds <- persist.AuthId.findIdByUserId(userId)
      seqstates <- DBIO.sequence(authIds map (persistAndPushUpdate(region, _, update)))
    } yield seqstates
  }

  def broadcastClientUpdate(region: ActorRef, update: api.Update)(
    implicit
    client: api.AuthorizedClientData, ec: ExecutionContext
    ): DBIO[SeqValState] = {
    val header = update.header
    val serializedData = update.toByteArray

    for {
      otherAuthIds <- persist.AuthId.findIdByUserId(client.userId).map(_.view.filter(_ != client.authId))
      _ <- DBIO.sequence(otherAuthIds map (authId => persistAndPushUpdate(region, authId, update)))
      ownseqstate <- persistAndPushUpdate(region, client.authId, update)
    } yield ownseqstate
  }

  private def pushUpdateGetSeq(region: ActorRef, authId: Long, update: api.Update): Future[SeqVal] = {
    region.ask(Envelope(authId, PushUpdateGetSeq(update)))(PushGetSeqTimeout).mapTo[SeqVal]
  }
}

class SeqUpdatesManager extends PersistentActor {

  import SeqUpdatesManager._
  import ShardRegion.Passivate

  context.setReceiveTimeout(
    context.system.settings.config.getDuration("push.seq-updates-manager.receive-timeout", TimeUnit.SECONDS).seconds
  )

  override def persistenceId: String = self.path.parent.name + "-" + self.path.name

  private[this] val IncrementOnStart: Int = 1000
  require(IncrementOnStart > 1)
  // it is needed to prevent divizion by zero in pushUpdate

  private[this] var seq: Int = 0

  override def receiveCommand: Receive = {
    case Envelope(_, GetSeq) =>
      sender() ! seq
    case Envelope(_, PushUpdate(upd)) =>
      pushUpdate(upd)
    case Envelope(_, PushUpdateGetSeq(upd)) =>
      val replyTo = sender()

      pushUpdate(upd, { newSeq =>
        replyTo ! newSeq
      })
    case ReceiveTimeout => context.parent ! Passivate(stopMessage = Stop)
    case Stop => context.stop(self)
  }

  override def receiveRecover: Receive = {
    case SeqChanged(value) =>
      seq = value
    case RecoveryCompleted =>
      recoveryCompleted()
  }

  private def recoveryCompleted(): Unit = {
    seq += IncrementOnStart

    persist(SeqChanged(seq))(noop1)
  }

  private def pushUpdate(upd: api.Update): Unit = {
    pushUpdate(upd, noop1)
  }

  private def pushUpdate(upd: api.Update, cb: Int => Unit): Unit = {
    def push(): Unit = {
      // TODO: push it
    }

    seq += 1

    if (seq % (IncrementOnStart / 2) == 0) {
      persist(SeqChanged(seq)) {
        case SeqChanged(newSeq) =>
          push()
          cb(newSeq)
      }
    } else {
      push()
      cb(seq)
    }
  }
}
