package im.actor.server.eventbus

import java.util.UUID

import akka.actor._
import akka.cluster.sharding.{ ClusterSharding, ClusterShardingSettings }
import akka.pattern.ask
import akka.util.Timeout
import im.actor.config.ActorConfig
import im.actor.types._

import scala.concurrent.Future

object EventBus {
  type EventBusId = String
  type DeviceId = Long

  sealed trait Client {
    def isInternal: Boolean
    def isExternal: Boolean
    def externalUserId: Option[UserId]
    def externalAuthId: Option[AuthId]
    def internalActorRef: Option[ActorRef]
  }
  final case class InternalClient(ref: ActorRef) extends Client {
    override def isInternal: Boolean = true

    override def isExternal: Boolean = false

    override def externalUserId: Option[UserId] = None

    override def externalAuthId: Option[AuthId] = None

    override def internalActorRef = Some(ref)
  }
  final case class ExternalClient(userId: UserId, authId: AuthId) extends Client {
    override def isInternal: Boolean = false

    override def isExternal: Boolean = true

    override def externalUserId: Option[UserId] = Some(userId)

    override def externalAuthId: Option[AuthId] = Some(authId)

    override def internalActorRef = None
  }

  final case class Message(id: String, client: Client, deviceId: Option[Long], message: Array[Byte])
  final case class Disposed(id: String)
  final case class Joined(id: String, client: Client, deviceId: Long)
  final case class Disconnected(id: String, client: Client, deviceId: Long)
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
  ): Future[(EventBusId, DeviceId)] =
    create(EventBus.ExternalClient(clientUserId, clientAuthId), timeout, isOwned)

  def create(ref: ActorRef, timeout: Option[Long], isOwned: Option[Boolean]): Future[(String, EventBus.DeviceId)] =
    create(EventBus.InternalClient(ref), timeout, isOwned)

  def create(client: EventBus.Client, timeout: Option[Long], isOwned: Option[Boolean]): Future[(String, EventBus.DeviceId)] = {
    val id = UUID.randomUUID().toString
    (region ? EventBusEnvelope(id, Create(client, timeout, isOwned))).mapTo[CreateAck] map (ack ⇒ (id, ack.deviceId))
  }

  def dispose(clientUserId: UserId, clientAuthId: AuthId, id: String): Future[Unit] =
    (region ? EventBusEnvelope(id, Dispose(ExternalClient(clientUserId, clientAuthId)))) map (_ ⇒ ())

  def post(
    client:       Client,
    id:           String,
    destinations: Seq[Long],
    message:      Array[Byte]
  ): Future[Unit] = (region ? EventBusEnvelope(id, Post(client, destinations, message))) map (_ ⇒ ())

  def keepAlive(client: Client, id: String, timeout: Option[Long]): Future[Unit] =
    region ? EventBusEnvelope(id, KeepAlive(client, timeout)) map (_ ⇒ ())

  def join(client: Client, id: String, timeout: Option[Long]): Future[DeviceId] =
    (region ? EventBusEnvelope(id, Join(client, timeout))).mapTo[JoinAck] map (_.deviceId)

  def fetchOwner(id: String): Future[DeviceId] =
    (region ? EventBusEnvelope(id, FetchInfo)).mapTo[FetchInfoAck] map (_.ownerDeviceId)
}

object EventBusExtension extends ExtensionId[EventBusExtension] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): EventBusExtension = new EventBusExtension(system)

  override def lookup(): ExtensionId[_ <: Extension] = EventBusExtension
}