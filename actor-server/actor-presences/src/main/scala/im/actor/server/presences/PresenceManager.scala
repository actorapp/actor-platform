package im.actor.server.presences

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

import akka.actor._
import akka.contrib.pattern.ShardRegion.Passivate
import akka.contrib.pattern.{ ClusterSharding, ShardRegion }
import akka.pattern.ask
import akka.util.Timeout
import org.joda.time.DateTime
import slick.driver.PostgresDriver.api._

import im.actor.server.db.DbExtension
import im.actor.server.{ models, persist }

case class PresenceManagerRegion(val ref: ActorRef)

sealed trait Presence

@SerialVersionUID(1L)
case class PresenceState(userId: Int, presence: Presence, lastSeenAt: Option[DateTime])

object Presences {

  @SerialVersionUID(1L)
  case object Online extends Presence

  @SerialVersionUID(1L)
  case object Offline extends Presence

}

object PresenceManager {
  import Presences._

  private val InitRetryTimeout = 5.seconds

  private sealed trait Message

  @SerialVersionUID(1L)
  private case class UserPresenceChange(presence: Presence, authId: Long, timeout: Long) extends Message

  @SerialVersionUID(1L)
  private case class Subscribe(consumer: ActorRef) extends Message

  @SerialVersionUID(1L)
  private case class SubscribeAck(consumer: ActorRef)

  @SerialVersionUID(1L)
  private case class Unsubscribe(consumer: ActorRef) extends Message

  @SerialVersionUID(1L)
  private case class UnsubscribeAck(consumer: ActorRef)

  @SerialVersionUID(1L)
  private case class Envelope(userId: Int, payload: Message)

  @SerialVersionUID(1L)
  private case class Initialized(lastSeenAt: Option[DateTime])

  private val idExtractor: ShardRegion.IdExtractor = {
    case env @ Envelope(userId, payload) ⇒ (userId.toString, env)
  }

  private val shardResolver: ShardRegion.ShardResolver = msg ⇒ msg match {
    case Envelope(userId, _) ⇒ (userId % 32).toString // TODO: configurable
  }

  private def startRegion(props: Option[Props])(implicit system: ActorSystem): PresenceManagerRegion =
    PresenceManagerRegion(ClusterSharding(system).start(
      typeName = "PresenceManager",
      entryProps = props,
      idExtractor = idExtractor,
      shardResolver = shardResolver
    ))

  def startRegion()(implicit system: ActorSystem): PresenceManagerRegion = startRegion(Some(props))

  def startRegionProxy()(implicit system: ActorSystem): PresenceManagerRegion = startRegion(None)

  def props = Props(classOf[PresenceManager])

  def subscribe(userId: Int, consumer: ActorRef)(implicit region: PresenceManagerRegion, ec: ExecutionContext, timeout: Timeout): Future[Unit] = {
    region.ref.ask(Envelope(userId, Subscribe(consumer))).mapTo[SubscribeAck].map(_ ⇒ ())
  }

  def subscribe(userIds: Set[Int], consumer: ActorRef)(implicit region: PresenceManagerRegion, ec: ExecutionContext, timeout: Timeout): Future[Unit] =
    Future.sequence(userIds map (subscribe(_, consumer))) map (_ ⇒ ())

  def unsubscribe(userId: Int, consumer: ActorRef)(implicit region: PresenceManagerRegion, ec: ExecutionContext, timeout: Timeout): Future[Unit] = {
    region.ref.ask(Envelope(userId, Unsubscribe(consumer))).mapTo[UnsubscribeAck].map(_ ⇒ ())
  }

  def presenceSetOnline(userId: Int, authId: Long, timeout: Long)(implicit region: PresenceManagerRegion): Unit = {
    region.ref ! Envelope(userId, UserPresenceChange(Online, authId, timeout))
  }

  def presenceSetOffline(userId: Int, authId: Long, timeout: Long)(implicit region: PresenceManagerRegion): Unit = {
    region.ref ! Envelope(userId, UserPresenceChange(Offline, authId, timeout))
  }
}

class PresenceManager extends Actor with ActorLogging with Stash {
  import Presences._
  import PresenceManager._

  implicit val ec: ExecutionContext = context.dispatcher
  private val db: Database = DbExtension(context.system).db

  private val receiveTimeout = 15.minutes // TODO: configurable
  context.setReceiveTimeout(receiveTimeout)

  private val userId = self.path.name.toInt

  private[this] var scheduledTimeouts = Map.empty[Long, Cancellable]
  private[this] var devicePresences = Map.empty[Long, Presence]
  private[this] var consumers = Set.empty[ActorRef]
  private[this] var state = PresenceState(userId, Offline, None)
  private[this] var lastChange = UserPresenceChange(Offline, 0, 0)

  private def initialize(userId: Int): Unit = {
    db.run(persist.presences.UserPresence.find(userId).map {
      case Some(userPresence) ⇒
        self ! Initialized(userPresence.lastSeenAt)
      case None ⇒
        db.run(persist.presences.UserPresence.createOrUpdate(models.presences.UserPresence(userId, None)))
        self ! Initialized(None)
    }) onFailure {
      case e ⇒
        log.error(e, "Failed to recover PresenceManager state. Retry in {}", InitRetryTimeout)

        context.system.scheduler.scheduleOnce(InitRetryTimeout) {
          initialize(userId)
        }
    }
  }

  def receive = {
    case Envelope(userId, _) ⇒
      stash()
      initialize(userId)
    case Initialized(lastSeenAt: Option[DateTime]) ⇒
      unstashAll()
      this.state = this.state.copy(lastSeenAt = lastSeenAt)
      context.become(working)
    case msg ⇒ stash()
  }

  def working: Receive = {
    case Envelope(userId, Subscribe(consumer)) ⇒
      if (!consumers.contains(consumer)) {
        context.watch(consumer)
        consumers += consumer
      }

      sender ! SubscribeAck(consumer)
      deliverState(consumer)
    case Envelope(userId, Unsubscribe(consumer)) ⇒
      consumers -= consumer
      context.unwatch(consumer)
      sender ! UnsubscribeAck(consumer)
    case Terminated(consumer) if consumers.contains(consumer) ⇒
      consumers -= consumer
    case Envelope(userId, change @ UserPresenceChange(presence, authId, timeout)) ⇒
      scheduledTimeouts.get(authId) foreach (_.cancel())

      if (presence != Offline) {
        this.state = this.state.copy(lastSeenAt = Some(new DateTime))
        db.run(persist.presences.UserPresence.createOrUpdate(models.presences.UserPresence(userId, this.state.lastSeenAt)))

        this.scheduledTimeouts = this.scheduledTimeouts +
          (authId → context.system.scheduler.scheduleOnce(timeout.millis, self, Envelope(userId, UserPresenceChange(Offline, authId, 0))))
      }

      this.devicePresences = this.devicePresences + (authId → presence)

      val oldPresence = this.state.presence

      val newPresence =
        if (this.devicePresences.exists(_._2 != Offline))
          Online
        else
          Offline

      this.state = this.state.copy(presence = newPresence)

      if (newPresence != oldPresence)
        deliverState()

    case ReceiveTimeout ⇒
      if (consumers.isEmpty) {
        context.parent ! Passivate(stopMessage = PoisonPill)
      }
  }

  private def deliverState(): Unit =
    consumers foreach deliverState

  private def deliverState(consumer: ActorRef): Unit =
    consumer ! this.state
}