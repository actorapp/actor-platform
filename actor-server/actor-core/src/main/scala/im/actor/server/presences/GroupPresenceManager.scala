package im.actor.server.presences

import akka.actor._
import akka.cluster.sharding.ShardRegion.Passivate
import akka.pattern.pipe
import akka.util.Timeout
import im.actor.server.db.DbExtension
import im.actor.server.persist.GroupUserRepo
import slick.driver.PostgresDriver.api._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

@SerialVersionUID(1L)
case class GroupPresenceState(groupId: Int, onlineCount: Int)

object GroupPresenceManager {

  private val SubscribeRetryTimeout = 5.seconds

  @SerialVersionUID(1L)
  private[presences] case class Envelope(groupId: Int, payload: Message)

  private[presences] sealed trait Message

  @SerialVersionUID(1L)
  private[presences] case class Subscribe(consumer: ActorRef) extends Message

  @SerialVersionUID(1L)
  private[presences] case class SubscribeAck(consumer: ActorRef)

  @SerialVersionUID(1L)
  private[presences] case class Unsubscribe(consumer: ActorRef) extends Message

  @SerialVersionUID(1L)
  private[presences] case class UnsubscribeAck(consumer: ActorRef)

  @SerialVersionUID(1L)
  private[presences] case class UserAdded(userId: Int) extends Message

  @SerialVersionUID(1L)
  private[presences] case class UserRemoved(userId: Int) extends Message

  private case class Initialized(groupId: Int, userIds: Set[Int])

  def props = Props(classOf[GroupPresenceManager])
}

class GroupPresenceManager extends Actor with ActorLogging with Stash {
  import GroupPresenceManager._
  import Presences._

  implicit val ec: ExecutionContext = context.dispatcher
  implicit val timeout = Timeout(5.seconds)

  private val db: Database = DbExtension(context.system).db
  private val presenceExt = PresenceExtension(context.system)

  private val receiveTimeout = 15.minutes // TODO: configurable
  context.setReceiveTimeout(receiveTimeout)

  private[this] val groupId = self.path.name.toInt
  private[this] var userIds = Set.empty[Int]
  private[this] var onlineUserIds = Set.empty[Int]
  private[this] var consumers = Set.empty[ActorRef]

  def receive = {
    case env @ Envelope(groupId, _) ⇒
      stash()
      db.run(GroupUserRepo.findUserIds(groupId))
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
    presenceExt.subscribe(userIds, self) onFailure {
      case e ⇒
        log.error(e, "Failed to subscribe to users presences")
        context.system.scheduler.scheduleOnce(SubscribeRetryTimeout) {
          subscribeToUserPresences(userIds)
        }
    }
  }

  private def unsubscribeFromUserPresences(userId: Int): Unit = {
    presenceExt.unsubscribe(userId, self) onFailure {
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