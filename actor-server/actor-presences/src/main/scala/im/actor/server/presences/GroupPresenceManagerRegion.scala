package im.actor.server.presences

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.contrib.pattern.{ ClusterSharding, ShardRegion }
import im.actor.server.presences.GroupPresenceManager.Envelope

object GroupPresenceManagerRegion {
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

  def startRegion()(implicit system: ActorSystem): GroupPresenceManagerRegion = startRegion(Some(GroupPresenceManager.props))

  def startRegionProxy()(implicit system: ActorSystem): GroupPresenceManagerRegion = startRegion(None)

}

case class GroupPresenceManagerRegion(ref: ActorRef)