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
  private[push] case class PushUpdate(upd: api.Update) extends Message

  @SerialVersionUID(1L)
  private[push] case class PushUpdateGetSeq(upd: api.Update) extends Message

  @SerialVersionUID(1L)
  private[push] case class Envelope(authId: Long, payload: Message)

  @SerialVersionUID(1L)
  private[push] case class Seq(value: Int)

  @SerialVersionUID(1L)
  private[push] case object Stop

  private[push] sealed trait PersistentEvent

  @SerialVersionUID(1L)
  private[push] case class SeqChanged(value: Int) extends PersistentEvent

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

  val PushGetSeqTimeout = Timeout(5.seconds) // TODO: configurable

  def startRegion()(implicit system: ActorSystem): ActorRef = startRegion(Some(Props[SeqUpdatesManager]))

  def startRegionProxy()(implicit system: ActorSystem): ActorRef = startRegion(None)

  def pushUpdate(region: ActorRef, authId: Long, upd: api.Update): Unit = {
    region ! Envelope(authId, PushUpdate(upd))
  }

  def pushUpdateGetSeq(region: ActorRef, authId: Long, upd: api.Update): Future[Seq] = {
    region.ask(Envelope(authId, PushUpdateGetSeq(upd)))(PushGetSeqTimeout).mapTo[Seq]
  }

  def sendClientUpdate(region: ActorRef, update: api.Update)(
    implicit clientData: api.ClientData, ec: ExecutionContext
  ): DBIO[(Int, Array[Byte])] = {
    val header = update.header
    val serializedData = update.toByteArray
    val seqUpdate = models.sequence.SeqUpdate(clientData.authId, header, serializedData)

    for {
      _ <- persist.sequence.SeqUpdate.create(seqUpdate)
      seq <- DBIO.from(pushUpdateGetSeq(region, clientData.authId, update).map(_.value))
    } yield (seq, seqUpdate.ref.toByteArray)
  }

  def broadcastUserUpdate(
    region: ActorRef,
    userId: Int,
    update: api.Update
  )(implicit ec: ExecutionContext): DBIO[Unit] = {
    val header = update.header
    val serializedData = update.toByteArray

    for {
      authIds <- persist.AuthId.findByUserId(userId)
      _ <- DBIO.sequence(authIds.map( authId =>
        persist.sequence.SeqUpdate.create(models.sequence.SeqUpdate(authId.id, header, serializedData))))
    } yield {
      authIds foreach (a => pushUpdate(region, a.id, update))
    }
  }

  def broadcastClientUpdate(region: ActorRef, clientUserId: Int, update: api.Update)(
    implicit
    clientData: api.ClientData, ec: ExecutionContext
  ): DBIO[(Int, Array[Byte])] = {
    val header = update.header
    val serializedData = update.toByteArray

    for {
      otherAuthIds <- persist.AuthId.findByUserId(clientUserId).map(_.view.filter(_.id != clientData.authId))
      _ <- DBIO.sequence(
        otherAuthIds.map { authId =>
          persist.sequence.SeqUpdate.create(models.sequence.SeqUpdate(authId.id, header, serializedData))
        }
      )
      ownUpdate = models.sequence.SeqUpdate(clientData.authId, header, serializedData)
      _ <- persist.sequence.SeqUpdate.create(ownUpdate)
      ownSeq <- DBIO.from(pushUpdateGetSeq(region, clientData.authId, update).map(_.value))
    } yield {
      otherAuthIds foreach (authId => pushUpdate(region, authId.id, update))

      (ownSeq, ownUpdate.ref.toByteArray)
    }
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
  require(IncrementOnStart > 1) // it is needed to prevent divizion by zero in pushUpdate

  private[this] var seq: Int = 0

  override def receiveCommand: Receive = {
    case Envelope(authId: Long, PushUpdate(upd)) =>
      pushUpdate(upd)
    case Envelope(authId: Long, PushUpdateGetSeq(upd)) =>
      val replyTo = sender()

      pushUpdate(upd, { newSeq =>
        replyTo ! Seq(newSeq)
      })
    case ReceiveTimeout => context.parent ! Passivate(stopMessage = Stop)
    case Stop           => context.stop(self)
  }

  override def receiveRecover: Receive = {
    case SeqChanged(value) =>
      println(s"SeqChanged $value")
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
