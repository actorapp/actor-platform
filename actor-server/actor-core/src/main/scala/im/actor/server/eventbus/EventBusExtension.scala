package im.actor.server.eventbus

import java.util.UUID

import akka.actor._
import akka.cluster.sharding.{ ClusterShardingSettings, ClusterSharding }
import akka.pattern.ask
import akka.util.Timeout
import im.actor.api.rpc.eventbus.ApiEventBusDestination
import im.actor.config.ActorConfig
import im.actor.types._

import scala.concurrent.Future

object EventBus {
  type EventBusId = String
  type DeviceId = Long
}

final class EventBusExtension(system: ActorSystem) extends Extension {
  import EventBus._
  import EventBusMessages._
  import system.dispatcher

  private implicit val askTimeout = Timeout(ActorConfig.defaultTimeout)

  private val region = ClusterSharding(system)
    .start(
      "EventBusMediator",
      EventBusMediator.props,
      ClusterShardingSettings(system),
      EventBusMediator.extractEntityId,
      EventBusMediator.extractShardId
    )

  def create(
    clientUserId: UserId,
    clientAuthId: AuthId,
    timeout:      Option[Long],
    isOwned:      Option[Boolean]
  ): Future[(EventBusId, DeviceId)] = {
    val id = UUID.randomUUID().toString
    (region ? EventBusEnvelope(id, Create(clientUserId, clientAuthId, timeout, isOwned))).mapTo[CreateAck] map (ack ⇒ (id, ack.deviceId))
  }

  def dispose(clientUserId: UserId, id: String): Future[Unit] =
    (region ? EventBusEnvelope(id, Dispose(clientUserId))) map (_ ⇒ ())

  def post(
    clientUserId: UserId,
    clientAuthId: AuthId,
    id:           String,
    destinations: Seq[ApiEventBusDestination],
    message:      Array[Byte]
  ): Future[Unit] = (region ? EventBusEnvelope(id, Post(clientUserId, clientAuthId, destinations, message))) map (_ ⇒ ())

  def keepAlive(clientAuthId: AuthId, id: String, timeout: Option[Long]): Unit =
    region ? EventBusEnvelope(id, KeepAlive(clientAuthId, timeout))

  def join(clientUserId: UserId, clientAuthId: AuthId, id: String, timeout: Option[Long]): Future[DeviceId] =
    (region ? EventBusEnvelope(id, Join(clientUserId, clientAuthId, timeout))).mapTo[JoinAck] map (_.deviceId)
}

object EventBusExtension extends ExtensionId[EventBusExtension] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): EventBusExtension = new EventBusExtension(system)

  override def lookup(): ExtensionId[_ <: Extension] = EventBusExtension
}