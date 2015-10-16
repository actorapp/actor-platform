package im.actor.server.sequence

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.cluster.sharding.{ ClusterShardingSettings, ClusterSharding, ShardRegion }
import im.actor.server.sequence.WeakUpdatesManager.Envelope

object WeakUpdatesManagerRegion {
  private val extractEntityId: ShardRegion.ExtractEntityId = {
    case env @ Envelope(authId, payload) ⇒ (authId.toString, env)
  }

  private val extractShardId: ShardRegion.ExtractShardId = msg ⇒ msg match {
    case Envelope(authId, _) ⇒ (authId % 32).toString // TODO: configurable
  }

  private val typeName = "WeakUpdatesManager"

  private def startRegion(props: Props)(implicit system: ActorSystem): WeakUpdatesManagerRegion = {
    WeakUpdatesManagerRegion(ClusterSharding(system).start(
      typeName = typeName,
      entityProps = props,
      settings = ClusterShardingSettings(system),
      extractEntityId = extractEntityId,
      extractShardId = extractShardId
    ))
  }

  def startRegion()(implicit system: ActorSystem): WeakUpdatesManagerRegion = startRegion(WeakUpdatesManager.props)

  def startRegionProxy()(implicit system: ActorSystem): WeakUpdatesManagerRegion =
    WeakUpdatesManagerRegion(ClusterSharding(system).startProxy(
      typeName = typeName,
      role = None,
      extractEntityId = extractEntityId,
      extractShardId = extractShardId
    ))

}

case class WeakUpdatesManagerRegion(ref: ActorRef)
