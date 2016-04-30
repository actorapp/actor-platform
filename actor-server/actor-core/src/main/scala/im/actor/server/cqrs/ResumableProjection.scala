package im.actor.server.cqrs

import akka.actor.{ ActorRef, ActorRefFactory, PoisonPill, Props }
import akka.pattern.ask
import akka.http.scaladsl.util.FastFuture
import akka.persistence.SnapshotMetadata
import akka.util.Timeout
import im.actor.config.ActorConfig
import im.actor.serialization.ActorSerializer

import scala.concurrent.Future

object ResumableProjection {
  ActorSerializer.register(
    110001 → classOf[ResumableProjectionEvents.OffsetWritten],
    110002 → classOf[ResumableProjectionState]
  )

  def apply(id: String)(implicit factory: ActorRefFactory) = new ResumableProjection(id)

  private[cqrs] val maxHits = 20
}

private[cqrs] trait ResumableProjectionStateBase extends ProcessorState[ResumableProjectionState] {
  this: ResumableProjectionState ⇒

  import ResumableProjectionEvents._

  override def updated(e: Event): ResumableProjectionState = e match {
    case ow: OffsetWritten ⇒
      copy(offset = ow.offset, hits = hits + 1)
  }

  override def withSnapshot(metadata: SnapshotMetadata, snapshot: Any): ResumableProjectionState = snapshot match {
    case s: ResumableProjectionState ⇒ s
  }
}

final class ResumableProjection(id: String)(implicit factory: ActorRefFactory) {
  import factory.dispatcher

  private case class SaveOffset(offset: Long)
  private object SaveOffsetAck

  private object GetOffset
  private case class GetOffsetResponse(offset: Long)

  private implicit val timeout = Timeout(ActorConfig.defaultTimeout)

  private var isStopped = false

  private val _actor = factory.actorOf(Props(new Processor[ResumableProjectionState] {
    import ResumableProjectionEvents._

    override def persistenceId: String = s"RProj_$id"

    override protected def getInitialState: ResumableProjectionState = ResumableProjectionState(0L, 0)

    override protected def handleQuery: PartialFunction[Any, Future[Any]] = {
      case GetOffset ⇒ FastFuture.successful(GetOffsetResponse(state.offset))
    }

    override protected def handleCommand: Receive = {
      case SaveOffset(offset) ⇒ persist(OffsetWritten(offset)) { e ⇒
        commit(e)

        if (state.hits > 10) {
          saveSnapshot(state)
          setState(state.copy(hits = 0))
        }

        sender() ! SaveOffsetAck
      }
    }
  }))

  private def actor: ActorRef =
    if (isStopped) throw new IllegalStateException("Projection is stopped")
    else _actor

  def latestOffset: Future[Long] = (actor ? GetOffset).mapTo[GetOffsetResponse] map (_.offset)

  def saveOffset(offset: Long): Future[Unit] = (actor ? SaveOffset(offset)) map (_ ⇒ ())

  def stop(): Unit = {
    actor ! PoisonPill
    isStopped = true
  }
}