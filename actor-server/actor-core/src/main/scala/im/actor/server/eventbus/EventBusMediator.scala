package im.actor.server.eventbus

import akka.actor._
import akka.cluster.sharding.ShardRegion
import im.actor.api.rpc.Update
import im.actor.api.rpc.eventbus._
import im.actor.server.db.DbExtension
import im.actor.server.eventbus.EventBus._
import im.actor.server.sequence.WeakUpdatesExtension
import im.actor.types._

import scala.collection.mutable
import scala.concurrent.duration._
import scala.util.Random

abstract class EventBusError(message: String) extends RuntimeException(message)

object EventBusErrors {
  case object EventBusNotFound extends EventBusError("EventBus not found")
}

private[eventbus] trait EventBusMessage
private[eventbus] final case class EventBusEnvelope(id: String, message: EventBusMessage)

private[eventbus] object EventBusMessages {
  final case class Create(clientUserId: UserId, clientAuthId: AuthId, timeout: Option[Long], isOwned: Option[Boolean]) extends EventBusMessage
  final case class CreateAck(deviceId: Long)

  final case class Dispose(clientUserId: UserId) extends EventBusMessage
  case object DisposeAck

  final case class Post(
    clientUserId: UserId,
    clientAuthId: AuthId,
    destinations: Seq[ApiEventBusDestination],
    message:      Array[Byte]
  ) extends EventBusMessage
  case object PostAck

  final case class KeepAlive(clientAuthId: AuthId, timeout: Option[Long]) extends EventBusMessage
  case object KeepAliveAck

  final case class Join(clientUserId: UserId, clientAuthId: AuthId, timeout: Option[Long]) extends EventBusMessage
  final case class JoinAck(deviceId: DeviceId)

  final case class Subscribe(ref: ActorRef) extends EventBusMessage
  final case class SubscribeAck(subscribe: Subscribe)
}

object EventBusMediator {
  private[eventbus] def extractEntityId: ShardRegion.ExtractEntityId = {
    case EventBusEnvelope(id, msg) ⇒ (id, msg)
  }

  private[eventbus] def extractShardId: ShardRegion.ExtractShardId = {
    case EventBusEnvelope(id, _) ⇒ (id.hashCode % 100).toString
  }

  def props = Props(classOf[EventBusMediator])

  private case class ConsumerTimedOut(authId: Long)
}

final class EventBusMediator extends Actor with ActorLogging {
  import EventBus._
  import EventBusMessages._
  import EventBusMediator._

  import context.dispatcher

  val db = DbExtension(context.system).db
  val weakExt = WeakUpdatesExtension(context.system)

  val id = self.path.name

  var owner: Option[Int] = None
  val internalConsumers = mutable.Set.empty[ActorRef]

  object consumers {
    private val a2d = mutable.Map.empty[AuthId, (UserId, DeviceId)]
    private val d2a = mutable.Map.empty[DeviceId, (UserId, AuthId)]
    val ownerAuthIds = mutable.Set.empty[AuthId]
    private val a2timeouts = mutable.Map.empty[AuthId, Cancellable]

    def isOwnerConnected = ownerAuthIds.nonEmpty

    def isEmpty = a2d.isEmpty

    def put(userId: UserId, authId: AuthId, deviceId: DeviceId) = {
      a2d.put(authId, (userId, deviceId))
      d2a.put(deviceId, (userId, authId))
      if (owner.contains(userId)) ownerAuthIds += authId
    }

    def authIds = a2d.keySet
    def byAuthId(authId: AuthId) = a2d.get(authId)
    def byDeviceId(deviceId: DeviceId) = d2a.get(deviceId)
    def keepAlive(authId: AuthId, timeout: Long) = {
      a2timeouts.get(authId) foreach (_.cancel())
      val scheduled = context.system.scheduler.scheduleOnce(timeout.millis, self, ConsumerTimedOut(authId))
      a2timeouts.put(authId, scheduled)
    }

    def stopKeepAlive(authId: AuthId) = a2timeouts.remove(authId) foreach (_.cancel())

    def remove(authId: AuthId): Option[(UserId, DeviceId)] = {
      a2d.get(authId) map {
        case (userId, deviceId) ⇒
          a2timeouts.remove(authId) foreach (_.cancel)
          a2d.remove(authId)
          d2a.remove(deviceId)
          ownerAuthIds -= authId
          (userId, deviceId)
      }
    }
  }

  def receive = {
    case Create(clientUserId, clientAuthId, timeoutOpt, isOwned) ⇒
      if (isOwned.contains(true)) this.owner = Some(clientUserId)
      val deviceId = Random.nextLong()
      consumers.put(clientUserId, clientAuthId, deviceId)
      timeoutOpt foreach (consumers.keepAlive(clientAuthId, _))
      sender() ! CreateAck(deviceId)
      context become created
    case _ ⇒
      sender() ! Status.Failure(EventBusErrors.EventBusNotFound)
      context stop self
  }

  def created: Receive = {
    case Post(clientUserId, clientAuthId, dests, message) ⇒
      val update = UpdateEventBusMessage(
        id = id,
        senderId = Some(clientUserId),
        senderDeviceId = consumers.byAuthId(clientAuthId) map (_._2),
        message = message
      )

      dests foreach {
        case ApiEventBusDestination(destUserId, deviceIds) ⇒
          deviceIds foreach { deviceId ⇒
            consumers.byDeviceId(deviceId) foreach {
              case (userId, authId) ⇒
                weakExt.pushUpdate(authId, update, None, None)
            }
          }
      }

      val msg = EventBus.Message(id, clientUserId, message)
      this.internalConsumers foreach (_ ! msg)

      sender() ! PostAck
    case KeepAlive(clientAuthId, timeoutOpt) ⇒
      timeoutOpt match {
        case Some(timeout) ⇒ consumers.keepAlive(clientAuthId, timeout)
        case None          ⇒ consumers.stopKeepAlive(clientAuthId)
      }
      sender() ! KeepAliveAck
    case ConsumerTimedOut(authId) ⇒
      if ((owner.isDefined && consumers.ownerAuthIds == Set(authId)) || consumers.authIds == Set(authId)) {
        log.debug("Disposing as no more clients connected")
        dispose()
      } else {
        consumers.remove(authId) match {
          case Some((userId, deviceId)) ⇒
            broadcast(UpdateEventBusDeviceDisconnected(id, userId, deviceId))
          case None ⇒ log.error("Consumer timed out with unknown authId: {}", authId)
        }
      }
    case Join(clientUserId, clientAuthId, timeoutOpt) ⇒
      val deviceId = Random.nextLong()
      consumers.put(clientUserId, clientAuthId, deviceId)
      broadcast(UpdateEventBusDeviceConnected(id, clientUserId, deviceId))
      timeoutOpt foreach (consumers.keepAlive(clientAuthId, _))
      sender() ! JoinAck(deviceId)
    case Dispose(clientUserId) ⇒
      if (owner.contains(clientUserId)) {
        log.debug("Disposing by owner request")
        dispose()
      } else sender() ! Status.Failure(new RuntimeException("Attempt to dispose by not an owner"))
    case subscribe @ Subscribe(ref) ⇒
      this.internalConsumers.add(ref)
      context watch ref
      sender() ! SubscribeAck(subscribe)
    case Terminated(ref) ⇒
      this.internalConsumers.remove(ref)
  }

  private def dispose(): Unit = {
    broadcast(UpdateEventBusDisposed(id))
    internalConsumers foreach (_ ! EventBus.Disposed(id))
    context stop self
  }

  private def broadcast(update: Update): Unit =
    consumers.authIds foreach (weakExt.pushUpdate(_, update, None, None))

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    super.preRestart(reason, message)
    log.error(reason, "Failure while processing message: {}", message)
  }
}