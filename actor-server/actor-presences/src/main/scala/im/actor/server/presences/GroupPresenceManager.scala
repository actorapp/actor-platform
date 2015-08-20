package im.actor.server.presences

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

import akka.actor._
import akka.contrib.pattern.ShardRegion.Passivate
import akka.contrib.pattern.{ ClusterSharding, ShardRegion }
import akka.pattern.{ ask, pipe }
import akka.util.Timeout
import slick.driver.PostgresDriver.api._

import im.actor.server.db.DbExtension
import im.actor.server.persist

case class GroupPresenceManagerRegion(ref: ActorRef)

@SerialVersionUID(1L)
case class GroupPresenceState(groupId: Int, onlineCount: Int)

object GroupPresenceManager {

  private val SubscribeRetryTimeout = 5.seconds

  @SerialVersionUID(1L)
  private case class Envelope(groupId: Int, payload: Message)

  private sealed trait Message

  @SerialVersionUID(1L)
  private case class Subscribe(consumer: ActorRef) extends Message

  @SerialVersionUID(1L)
  private case class SubscribeAck(consumer: ActorRef)

  @SerialVersionUID(1L)
  private case class Unsubscribe(consumer: ActorRef) extends Message

  @SerialVersionUID(1L)
  private case class UnsubscribeAck(consumer: ActorRef)

  @SerialVersionUID(1L)
  private case class UserAdded(userId: Int) extends Message

  @SerialVersionUID(1L)
  private case class UserRemoved(userId: Int) extends Message

  private case class Initialized(groupId: Int, userIds: Set[Int])

  private val idExtractor: ShardRegion.IdExtractor = {
    case env @ Envelope(userId, payload) ⇒ (userId.toString, env)
  }

  private val shardResolver: ShardRegion.ShardResolver = msg ⇒ msg match {
    case Envelope(userId, _) ⇒ (userId % 32).toString // TODO: configurable
  }

  private def startRegion(props: Option[Props])(implicit system: ActorSystem): GroupPresenceManagerRegion =
    GroupPresenceManagerRegion(ClusterSharding(system).start(
      typeName = "GroupPresenceManager",
      entryProps = props,
      idExtractor = idExtractor,
      shardResolver = shardResolver
    ))

  def startRegion()(implicit presenceManagerRegion: PresenceManagerRegion, system: ActorSystem): GroupPresenceManagerRegion = startRegion(Some(props))

  def startRegionProxy()(implicit system: ActorSystem): GroupPresenceManagerRegion = startRegion(None)

  def props(implicit presenceManagerRegion: PresenceManagerRegion) = Props(classOf[GroupPresenceManager], presenceManagerRegion)

  def subscribe(groupId: Int, consumer: ActorRef)(implicit region: GroupPresenceManagerRegion, ec: ExecutionContext, timeout: Timeout): Future[Unit] = {
    region.ref.ask(Envelope(groupId, Subscribe(consumer))).mapTo[SubscribeAck].map(_ ⇒ ())
  }

  def subscribe(groupIds: Set[Int], consumer: ActorRef)(implicit region: GroupPresenceManagerRegion, ec: ExecutionContext, timeout: Timeout): Future[Unit] =
    Future.sequence(groupIds map (subscribe(_, consumer))) map (_ ⇒ ())

  def unsubscribe(groupId: Int, consumer: ActorRef)(implicit region: GroupPresenceManagerRegion, ec: ExecutionContext, timeout: Timeout): Future[Unit] = {
    region.ref.ask(Envelope(groupId, Unsubscribe(consumer))).mapTo[UnsubscribeAck].map(_ ⇒ ())
  }

  def notifyGroupUserAdded(groupId: Int, userId: Int)(implicit region: GroupPresenceManagerRegion): Unit = {
    region.ref ! Envelope(groupId, UserAdded(userId))
  }

  def notifyGroupUserRemoved(groupId: Int, userId: Int)(implicit region: GroupPresenceManagerRegion): Unit = {
    region.ref ! Envelope(groupId, UserRemoved(userId))
  }
}

class GroupPresenceManager(
  implicit
  presenceManagerRegion: PresenceManagerRegion
) extends Actor with ActorLogging with Stash {
  import GroupPresenceManager._
  import Presences._

  implicit val ec: ExecutionContext = context.dispatcher
  implicit val timeout = Timeout(5.seconds)

  private val db: Database = DbExtension(context.system).db

  private val receiveTimeout = 15.minutes // TODO: configurable
  context.setReceiveTimeout(receiveTimeout)

  private[this] val groupId = self.path.name.toInt
  private[this] var userIds = Set.empty[Int]
  private[this] var onlineUserIds = Set.empty[Int]
  private[this] var consumers = Set.empty[ActorRef]

  def receive = {
    case env @ Envelope(groupId, _) ⇒
      stash()
      db.run(persist.GroupUser.findUserIds(groupId))
        .map(ids ⇒ Initialized(groupId, ids.toSet))
        .pipeTo(self)
        .onFailure {
          case e ⇒
            log.error("Failed to load group users")
            self ! PoisonPill
        }
    case Initialized(groupId, userIds) ⇒
      this.userIds = userIds
      context.become(working)
      unstashAll()

      subscribeToUserPresences(userIds)
    case msg ⇒ stash()
  }

  def working: Receive = {
    case Envelope(_, Subscribe(consumer)) ⇒
      if (!consumers.contains(consumer)) {
        context.watch(consumer)
        consumers += consumer
      }

      sender ! SubscribeAck(consumer)
      deliverState(groupId, consumer)
    case Envelope(_, Unsubscribe(consumer)) ⇒
      consumers -= consumer
      context.unwatch(consumer)
      sender ! UnsubscribeAck(consumer)
    case Envelope(_, UserAdded(userId)) ⇒
      subscribeToUserPresences(Set(userId))
    case Envelope(_, UserRemoved(userId)) ⇒
      onlineUserIds -= userId
      unsubscribeFromUserPresences(userId)
      deliverState(groupId)
    case PresenceState(userId, Online, _) ⇒
      if (!onlineUserIds.contains(userId)) {
        onlineUserIds += userId
        deliverState(groupId)
      }
    case PresenceState(userId, Offline, _) ⇒
      if (onlineUserIds.contains(userId)) {
        onlineUserIds -= userId
        deliverState(groupId)
      }
    case Terminated(consumer) if consumers.contains(consumer) ⇒
      consumers -= consumer
    case ReceiveTimeout ⇒
      if (consumers.isEmpty) {
        context.parent ! Passivate(stopMessage = PoisonPill)
      }
  }

  private def subscribeToUserPresences(userIds: Set[Int]): Unit = {
    PresenceManager.subscribe(userIds, self) onFailure {
      case e ⇒
        log.error(e, "Failed to subscribe to users presences")
        context.system.scheduler.scheduleOnce(SubscribeRetryTimeout) {
          subscribeToUserPresences(userIds)
        }
    }
  }

  private def unsubscribeFromUserPresences(userId: Int): Unit = {
    PresenceManager.unsubscribe(userId, self) onFailure {
      case e ⇒
        log.error(e, "Failed to unsubscribe from user presences")
        context.system.scheduler.scheduleOnce(SubscribeRetryTimeout) {
          unsubscribeFromUserPresences(userId)
        }
    }
  }

  private def deliverState(groupId: Int): Unit =
    consumers foreach (deliverState(groupId, _))

  private def deliverState(groupId: Int, consumer: ActorRef): Unit =
    consumer ! GroupPresenceState(groupId, onlineUserIds.size)
}