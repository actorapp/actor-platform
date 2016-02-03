package im.actor.server.presences

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.cluster.sharding.{ ClusterSharding, ClusterShardingSettings, ShardRegion }
import im.actor.server.presences.PresenceManager.Envelope

object PresenceManagerRegion {
  private val extractEntityId: ShardRegion.ExtractEntityId = {
    case env @ Envelope(userId, payload) ⇒ (userId.toString, payload)
  }

  private val extractShardId: ShardRegion.ExtractShardId = msg ⇒ msg match {
    case Envelope(userId, _) ⇒ (userId % 32).toString // TODO: configurable
  }

  private val typeName = "PresenceManager"

  private def startRegion(props: Props)(implicit system: ActorSystem): PresenceManagerRegion =
    PresenceManagerRegion(ClusterSharding(system).start(
      typeName = typeName,
      entityProps = props,
      settings = ClusterShardingSettings(system),
      extractEntityId = extractEntityId,
      extractShardId = extractShardId
    ))

  def startRegion()(implicit system: ActorSystem): PresenceManagerRegion = startRegion(PresenceManager.props)

  def startRegionProxy()(implicit system: ActorSystem): PresenceManagerRegion =
    PresenceManagerRegion(ClusterSharding(system).startProxy(
      typeName = typeName,
      role = None,
      extractEntityId = extractEntityId,
      extractShardId = extractShardId
    ))

}

case class PresenceManagerRegion(val ref: ActorRef)