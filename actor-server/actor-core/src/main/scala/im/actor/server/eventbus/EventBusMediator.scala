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
  final case class Create(client: Client, timeout: Option[Long], isOwned: Option[Boolean]) extends EventBusMessage
  final case class CreateAck(deviceId: Long)

  final case class Dispose(client: Client) extends EventBusMessage
  case object DisposeAck

  final case class Post(
    client:       Client,
    destinations: Seq[Long],
    message:      Array[Byte]
  ) extends EventBusMessage
  case object PostAck

  final case class KeepAlive(client: Client, timeout: Option[Long]) extends EventBusMessage
  case object KeepAliveAck

  final case class Join(client: Client, timeout: Option[Long]) extends EventBusMessage
  final case class JoinAck(deviceId: DeviceId)

  case object FetchInfo extends EventBusMessage
  final case class FetchInfoAck(ownerDeviceId: Long)
}

object EventBusMediator {
  private[eventbus] def extractEntityId: ShardRegion.ExtractEntityId = {
    case EventBusEnvelope(id, msg) ⇒ (id, msg)
  }

  private[eventbus] def extractShardId: ShardRegion.ExtractShardId = {
    case EventBusEnvelope(id, _) ⇒ (id.hashCode % 100).toString
  }

  def props = Props(classOf[EventBusMediator])

  private case class ConsumerTimedOut(client: Client)
}

final class EventBusMediator extends Actor with ActorLogging {
  import EventBus._
  import EventBusMessages._
  import EventBusMediator._

  import context.dispatcher

  val db = DbExtension(context.system).db
  val weakExt = WeakUpdatesExtension(context.system)

  val id = self.path.name

  var owner: Option[Client] = None

  object consumers {
    private val a2c = mutable.Map.empty[AuthId, (UserId, DeviceId)]
    private val r2c = mutable.Map.empty[ActorRef, DeviceId]
    val devices = mutable.Map.empty[DeviceId, Client]
    val owners = mutable.Set.empty[Client]
    private val c2timeouts = mutable.Map.empty[Client, Cancellable]

    def isOwnerConnected = owners.nonEmpty

    def isEmpty = a2c.isEmpty

    def put(client: Client, deviceId: DeviceId) = {
      devices.put(deviceId, client)

      if (owner.contains(client)) owners += client

      client match {
        case ec @ ExternalClient(userId, authId) ⇒
          a2c.put(authId, userId → deviceId)
        case InternalClient(ref) ⇒
          r2c.put(ref, deviceId)
      }
    }

    def authIds = a2c.keySet
    def actorRefs = r2c.keySet
    def byAuthId(authId: AuthId) = a2c.get(authId)
    def byActorRef(ref: ActorRef) = r2c.get(ref)
    def byDeviceId(deviceId: DeviceId) = devices.get(deviceId)
    def keepAlive(client: Client, timeout: Long) = {
      c2timeouts.get(client) foreach (_.cancel())
      val scheduled = context.system.scheduler.scheduleOnce(timeout.millis, self, ConsumerTimedOut(client))
      c2timeouts.put(client, scheduled)
    }

    def stopKeepAlive(client: Client) = c2timeouts.remove(client) foreach (_.cancel())

    def remove(client: Client): Option[(DeviceId, Client)] =
      client match {
        case ExternalClient(userId, authId) ⇒
          a2c.get(authId) map {
            case (_, deviceId) ⇒
              val client = ExternalClient(userId, authId)
              c2timeouts.remove(client) foreach (_.cancel())
              a2c.remove(authId)
              devices.remove(deviceId)
              owners -= client
              deviceId → client
          }
        case InternalClient(ref) ⇒
          r2c.remove(ref) map { deviceId ⇒
            val client = InternalClient(ref)
            c2timeouts.remove(client) foreach (_.cancel())
            devices.remove(deviceId)
            owners -= client
            deviceId → client
          }
      }
  }

  def receive = {
    case Create(client, timeoutOpt, isOwned) ⇒
      if (isOwned.contains(true))
        this.owner = Some(client)
      val deviceId = Random.nextLong()
      consumers.put(client, deviceId)
      timeoutOpt foreach (consumers.keepAlive(client, _))
      sender() ! CreateAck(deviceId)
      client.internalActorRef foreach context.watch
      context become created(deviceId)
    case _ ⇒
      sender() ! Status.Failure(EventBusErrors.EventBusNotFound)
      context stop self
  }

  def created(ownerDeviceId: Long): Receive = {
    case FetchInfo ⇒ sender() ! FetchInfoAck(ownerDeviceId)
    case Post(client, dests, message) ⇒
      val (update, deviceId) =
        client match {
          case e: ExternalClient ⇒
            val deviceId = consumers.byAuthId(e.authId) map (_._2)

            (UpdateEventBusMessage(
              id = id,
              senderId = Some(e.userId),
              senderDeviceId = deviceId,
              message = message
            ), deviceId)
          case i: InternalClient ⇒
            val deviceId = consumers.byActorRef(i.ref)

            (UpdateEventBusMessage(
              id = id,
              senderId = None,
              senderDeviceId = deviceId,
              message = message
            ), deviceId)
        }

      val clients =
        (dests match {
          case Nil ⇒ consumers.devices.values
          case _   ⇒ consumers.devices.filter(did ⇒ dests.contains(did._1)).values
        }) filterNot (_ == client)

      clients foreach {
        case InternalClient(ref) ⇒
          ref ! EventBus.Message(id, client, deviceId, message)
        case ExternalClient(_, authId) ⇒
          weakExt.pushUpdate(authId, update, None, None)
      }

      sender() ! PostAck
    case KeepAlive(client, timeoutOpt) ⇒
      timeoutOpt match {
        case Some(timeout) ⇒ consumers.keepAlive(client, timeout)
        case None          ⇒ consumers.stopKeepAlive(client)
      }
      sender() ! KeepAliveAck
    case ConsumerTimedOut(client) ⇒
      disconnect(client)
    case Join(client, timeoutOpt) ⇒
      client.externalAuthId flatMap consumers.byAuthId match {
        case Some((_, deviceId)) ⇒ sender() ! JoinAck(deviceId)
        case None ⇒
          val deviceId = Random.nextLong()

          val update = client match {
            case ExternalClient(userId, _) ⇒ UpdateEventBusDeviceConnected(id, Some(userId), deviceId)
            case InternalClient(_)         ⇒ UpdateEventBusDeviceConnected(id, None, deviceId)
          }

          broadcast(update)
          this.consumers.actorRefs foreach (_ ! EventBus.Joined(id, client, deviceId))
          consumers.put(client, deviceId)

          timeoutOpt foreach (consumers.keepAlive(client, _))
          client.internalActorRef foreach context.watch

          sender() ! JoinAck(deviceId)
      }
    case Dispose(client: Client) ⇒
      if (owner.contains(client)) {
        log.debug("Disposing by owner request")
        dispose()
      } else sender() ! Status.Failure(new RuntimeException("Attempt to dispose by not an owner"))
    case Terminated(ref) ⇒
      log.debug("Terminated {}", ref)
      disconnect(InternalClient(ref))
  }

  private def disconnect(client: Client) = {
    log.debug("Disconnecting {}", client)
    log.debug("owner: {}", owner)
    log.debug("consumers: {}", consumers.owners)
    if ((owner.isDefined && consumers.owners == Set(client)) || consumers.devices.toSet == Set(client)) {
      log.debug("Disposing as no more clients connected")
      dispose()
    } else {
      consumers.remove(client) match {
        case Some((deviceId, _)) ⇒
          client match {
            case EventBus.ExternalClient(userId, _) ⇒
              broadcast(UpdateEventBusDeviceDisconnected(id, Some(userId), deviceId))
            case _ ⇒
          }
          this.consumers.actorRefs foreach (_ ! EventBus.Disconnected(id, client, deviceId))
        case None ⇒ log.error("Consumer timed out with unknown client: {}", client)
      }
    }
  }

  private def dispose(): Unit = {
    broadcast(UpdateEventBusDisposed(id))
    consumers.actorRefs foreach (_ ! EventBus.Disposed(id))
    context stop self
  }

  private def broadcast(update: Update): Unit =
    consumers.authIds foreach (weakExt.pushUpdate(_, update, None, None))

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    super.preRestart(reason, message)
    log.error(reason, "Failure while processing message: {}", message)
  }
}