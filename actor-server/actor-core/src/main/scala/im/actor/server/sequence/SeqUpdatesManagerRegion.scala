package im.actor.server.sequence

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.cluster.sharding.{ ClusterShardingSettings, ClusterSharding, ShardRegion }

case class SeqUpdatesManagerRegion(ref: ActorRef)

object SeqUpdatesManagerRegion {

  private val extractEntityId: ShardRegion.ExtractEntityId = {
    case msg: SeqUpdatesManagerMessage ⇒ (msg.authId.toString, msg)
  }

  private val extractShardId: ShardRegion.ExtractShardId = msg ⇒ msg match {
    case msg: SeqUpdatesManagerMessage ⇒ (msg.authId % 32).toString // TODO: configurable
  }

  private val typeName = "SeqUpdatesManager"

  private def start(props: Props)(implicit system: ActorSystem): SeqUpdatesManagerRegion =
    SeqUpdatesManagerRegion(ClusterSharding(system).start(
      typeName = typeName,
      entityProps = props,
      settings = ClusterShardingSettings(system),
      extractEntityId = extractEntityId,
      extractShardId = extractShardId
    ))

  def start()(
    implicit
    system:            ActorSystem,
    googlePushManager: GooglePushManager,
    applePushManager:  ApplePushManager
  ): SeqUpdatesManagerRegion =
    start(SeqUpdatesManagerActor.props)

  def startProxy()(implicit system: ActorSystem): SeqUpdatesManagerRegion =
    SeqUpdatesManagerRegion(ClusterSharding(system).startProxy(
      typeName = typeName,
      role = None,
      extractEntityId = extractEntityId,
      extractShardId = extractShardId
    ))
}