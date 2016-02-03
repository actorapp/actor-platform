package im.actor.server.presences

import akka.actor._
import akka.cluster.sharding.ShardRegion.Passivate
import im.actor.server.db.DbExtension
import im.actor.server.model.presences.UserPresence
import im.actor.server.persist.presences.UserPresenceRepo
import org.joda.time.DateTime
import slick.driver.PostgresDriver.api._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

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

  private val InitRetryTimeout = 5.seconds

  private[presences] sealed trait Message

  @SerialVersionUID(1L)
  private[presences] case class UserPresenceChange(presence: Presence, authId: Long, timeout: Long) extends Message

  @SerialVersionUID(1L)
  private[presences] case class Subscribe(consumer: ActorRef) extends Message

  @SerialVersionUID(1L)
  private[presences] case class SubscribeAck(consumer: ActorRef)

  @SerialVersionUID(1L)
  private[presences] case class Unsubscribe(consumer: ActorRef) extends Message

  @SerialVersionUID(1L)
  private[presences] case class UnsubscribeAck(consumer: ActorRef)

  @SerialVersionUID(1L)
  private[presences] case class Envelope(userId: Int, payload: Message)

  @SerialVersionUID(1L)
  private case class Initialized(lastSeenAt: Option[DateTime])

  @SerialVersionUID(1L)
  private case object DeliverState extends Message

  def props = Props(classOf[PresenceManager])
}

class PresenceManager extends Actor with ActorLogging with Stash {
  import PresenceManager._
  import Presences._

  implicit val ec: ExecutionContext = context.dispatcher
  private val db: Database = DbExtension(context.system).db

  private val DeliverStateInterval = context.system.settings.config.getDuration("modules.presence.deliver-state-interval").getSeconds.seconds

  private val receiveTimeout = 15.minutes // TODO: configurable
  context.setReceiveTimeout(receiveTimeout)

  private val userId = self.path.name.toInt

  private[this] var scheduledTimeouts = Map.empty[Long, Cancellable]
  private[this] var devicePresences = Map.empty[Long, Presence]
  private[this] var consumers = Set.empty[ActorRef]
  private[this] var state = PresenceState(userId, Offline, None)

  initialize()

  private def initialize(): Unit = {
    db.run(UserPresenceRepo.find(userId).map {
      case Some(userPresence) ⇒
        self ! Initialized(userPresence.lastSeenAt)
      case None ⇒
        db.run(UserPresenceRepo.createOrUpdate(UserPresence(userId, None)))
        self ! Initialized(None)
    }) onFailure {
      case e ⇒
        log.error(e, "Failed to recover PresenceManager state. Retry in {}", InitRetryTimeout)

        context.system.scheduler.scheduleOnce(InitRetryTimeout) {
          initialize()
        }
    }
  }

  def receive = {
    case Initialized(lastSeenAt: Option[DateTime]) ⇒
      unstashAll()
      this.state = this.state.copy(lastSeenAt = lastSeenAt)
      scheduleDeliverState()
      context.become(working)
    case msg ⇒ stash()
  }

  def working: Receive = {
    case Subscribe(consumer) ⇒
      if (!consumers.contains(consumer)) {
        context.watch(consumer)
        consumers += consumer
      }

      sender ! SubscribeAck(consumer)
      deliverState(consumer)
    case Unsubscribe(consumer) ⇒
      consumers -= consumer
      context.unwatch(consumer)
      sender ! UnsubscribeAck(consumer)
    case Terminated(consumer) if consumers.contains(consumer) ⇒
      consumers -= consumer
    case change @ UserPresenceChange(presence, authId, timeout) ⇒
      scheduledTimeouts.get(authId) foreach (_.cancel())

      if (presence != Offline) {
        this.state = this.state.copy(lastSeenAt = Some((new DateTime).plusMillis(timeout.toInt)))
        db.run(UserPresenceRepo.createOrUpdate(UserPresence(userId, this.state.lastSeenAt)))

        this.scheduledTimeouts = this.scheduledTimeouts +
          (authId → context.system.scheduler.scheduleOnce(timeout.millis, self, UserPresenceChange(Offline, authId, 0)))
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
    case DeliverState ⇒
      // TODO: schedule only if online
      if (this.state.presence != Offline)
        deliverState()

      scheduleDeliverState()
  }

  private def scheduleDeliverState(): Unit =
    context.system.scheduler.scheduleOnce(DeliverStateInterval, self, DeliverState)

  private def deliverState(): Unit =
    consumers foreach deliverState

  private def deliverState(consumer: ActorRef): Unit =
    consumer ! this.state
}