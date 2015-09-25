package im.actor.server.sequence

import im.actor.api.rpc.peers.{ ApiPeerType, ApiPeer }

import scala.concurrent._

import akka.actor._
import akka.contrib.pattern.{ ClusterSharding, ShardRegion }
import akka.pattern.ask
import akka.util.Timeout
import slick.dbio.DBIO

import im.actor.api.rpc.Update
import im.actor.api.rpc.sequence.WeakUpdate
import im.actor.server.persist

case class WeakUpdatesManagerRegion(ref: ActorRef)

object WeakUpdatesManager {

  @SerialVersionUID(1L)
  private[sequence] case class Envelope(authId: Long, payload: Message)

  private[sequence] sealed trait Message

  @SerialVersionUID(1L)
  private[sequence] case class PushUpdate(header: Int, serializedData: Array[Byte], reduceKey: Option[String]) extends Message

  @SerialVersionUID(1L)
  private[sequence] case class Subscribe(consumer: ActorRef) extends Message

  @SerialVersionUID(1L)
  private[sequence] case class SubscribeAck(consumer: ActorRef) extends Message

  @SerialVersionUID(1L)
  case class UpdateReceived(update: WeakUpdate, reduceKey: Option[String])

  private val idExtractor: ShardRegion.IdExtractor = {
    case env @ Envelope(authId, payload) ⇒ (authId.toString, env)
  }

  private val shardResolver: ShardRegion.ShardResolver = msg ⇒ msg match {
    case Envelope(authId, _) ⇒ (authId % 32).toString // TODO: configurable
  }

  private def startRegion(props: Option[Props])(implicit system: ActorSystem): WeakUpdatesManagerRegion =
    WeakUpdatesManagerRegion(ClusterSharding(system).start(
      typeName = "WeakUpdatesManager",
      entryProps = props,
      idExtractor = idExtractor,
      shardResolver = shardResolver
    ))

  def startRegion()(implicit system: ActorSystem): WeakUpdatesManagerRegion = startRegion(Some(Props(classOf[WeakUpdatesManager])))

  def startRegionProxy()(implicit system: ActorSystem): WeakUpdatesManagerRegion = startRegion(None)

  def broadcastUserWeakUpdate(userId: Int, update: Update, reduceKey: Option[String])(implicit region: WeakUpdatesManagerRegion, ec: ExecutionContext): DBIO[Unit] = {
    val header = update.header
    val serializedData = update.toByteArray
    val msg = PushUpdate(header, serializedData, reduceKey)

    for (authIds ← persist.AuthId.findIdByUserId(userId)) yield {
      authIds foreach { authId ⇒
        region.ref ! Envelope(authId, msg)
      }
    }
  }

  def pushUpdate(authId: Long, update: Update, reduceKey: Option[String])(implicit region: WeakUpdatesManagerRegion, ec: ExecutionContext): Unit = {
    val header = update.header
    val serializedData = update.toByteArray
    region.ref ! Envelope(authId, PushUpdate(header, serializedData, reduceKey))
  }

  def reduceKey(updateHeader: Int, peer: ApiPeer): String = s"${updateHeader}-${peer.`type`.id}-${peer.id}"
  def reduceKeyUser(updateHeader: Int, userId: Int): String = s"${updateHeader}-${ApiPeerType.Private.id}-${userId}"
  def reduceKeyGroup(updateHeader: Int, groupId: Int): String = s"${updateHeader}-${ApiPeerType.Group.id}-${groupId}"

  private[sequence] def subscribe(authId: Long, consumer: ActorRef)(implicit region: WeakUpdatesManagerRegion, ec: ExecutionContext, timeout: Timeout): Future[Unit] = {
    region.ref.ask(Envelope(authId, Subscribe(consumer))).mapTo[SubscribeAck].map(_ ⇒ ())
  }
}

class WeakUpdatesManager extends Actor with ActorLogging {

  import WeakUpdatesManager._

  // TODO: set receive timeout

  def receive = working(Set.empty)

  def working(consumers: Set[ActorRef]): Receive = {
    case Envelope(authId, PushUpdate(header, serializedData, reduceKey)) ⇒
      consumers foreach (_ ! UpdateReceived(WeakUpdate(System.currentTimeMillis(), header, serializedData), reduceKey))
    case Envelope(_, Subscribe(consumer)) ⇒
      context.watch(consumer)
      context.become(working(consumers + consumer))
      sender() ! SubscribeAck(consumer)

      log.debug("Consumer subscribed {}", consumer)
    case Terminated(consumer) ⇒
      context.become(working(consumers - consumer))
  }
}
