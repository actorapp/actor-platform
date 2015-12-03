package im.actor.server.presences

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.cluster.sharding.{ ClusterSharding, ClusterShardingSettings, ShardRegion }
import im.actor.server.presences.GroupPresenceManager.Envelope

object GroupPresenceManagerRegion {
  private val extractEntityId: ShardRegion.ExtractEntityId = {
    case env @ Envelope(userId, payload) ⇒ (userId.toString, env)
  }

  private val extractShardId: ShardRegion.ExtractShardId = msg ⇒ msg match {
    case Envelope(userId, _) ⇒ (userId % 32).toString // TODO: configurable
  }

  private val typeName = "GroupPresenceManager"

  private def startRegion(props: Props)(implicit system: ActorSystem): GroupPresenceManagerRegion =
    GroupPresenceManagerRegion(ClusterSharding(system).start(
      typeName = typeName,
      entityProps = props,
      settings = ClusterShardingSettings(system),
      extractEntityId = extractEntityId,
      extractShardId = extractShardId
    ))

  def startRegion()(implicit system: ActorSystem): GroupPresenceManagerRegion = startRegion(GroupPresenceManager.props)

  def startRegionProxy()(implicit system: ActorSystem): GroupPresenceManagerRegion =
    GroupPresenceManagerRegion(ClusterSharding(system).startProxy(
      typeName = typeName,
      role = None,
      extractEntityId = extractEntityId,
      extractShardId = extractShardId
    ))
}

case class GroupPresenceManagerRegion(ref: ActorRef)