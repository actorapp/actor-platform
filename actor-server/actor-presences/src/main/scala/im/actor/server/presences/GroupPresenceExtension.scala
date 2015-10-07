package im.actor.server.presences

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Future
import scala.concurrent.duration._

sealed trait GroupPresenceExtension extends Extension

class GroupPresenceExtensionImpl(system: ActorSystem) extends GroupPresenceExtension {
  import GroupPresenceManager._
  import system.dispatcher
  implicit val timeout: Timeout = Timeout(20.seconds)

  private val region = GroupPresenceManagerRegion.startRegion()(system)

  def subscribe(groupId: Int, consumer: ActorRef): Future[Unit] = {
    region.ref.ask(Envelope(groupId, Subscribe(consumer))).mapTo[SubscribeAck].map(_ ⇒ ())
  }

  def subscribe(groupIds: Set[Int], consumer: ActorRef): Future[Unit] =
    Future.sequence(groupIds map (subscribe(_, consumer))) map (_ ⇒ ())

  def unsubscribe(groupId: Int, consumer: ActorRef): Future[Unit] = {
    region.ref.ask(Envelope(groupId, Unsubscribe(consumer))).mapTo[UnsubscribeAck].map(_ ⇒ ())
  }

  def notifyGroupUserAdded(groupId: Int, userId: Int): Unit = {
    region.ref ! Envelope(groupId, UserAdded(userId))
  }

  def notifyGroupUserRemoved(groupId: Int, userId: Int): Unit = {
    region.ref ! Envelope(groupId, UserRemoved(userId))
  }

}

object GroupPresenceExtension extends ExtensionId[GroupPresenceExtensionImpl] with ExtensionIdProvider {
  override def lookup = GroupPresenceExtension
  override def createExtension(system: ExtendedActorSystem) = new GroupPresenceExtensionImpl(system)
}
