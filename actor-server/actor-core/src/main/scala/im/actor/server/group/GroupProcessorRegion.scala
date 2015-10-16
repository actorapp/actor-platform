package im.actor.server.group

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.cluster.sharding.{ ClusterShardingSettings, ClusterSharding, ShardRegion }

object GroupProcessorRegion {
  private val extractEntityId: ShardRegion.ExtractEntityId = {
    case c: GroupCommand ⇒ (c.groupId.toString, c)
    case q: GroupQuery   ⇒ (q.groupId.toString, q)
  }

  private val extractShardId: ShardRegion.ExtractShardId = msg ⇒ msg match {
    case c: GroupCommand ⇒ (c.groupId % 100).toString // TODO: configurable
    case q: GroupQuery   ⇒ (q.groupId % 100).toString
  }

  val typeName = "GroupProcessor"

  private def start(props: Props)(implicit system: ActorSystem): GroupProcessorRegion =
    GroupProcessorRegion(ClusterSharding(system).start(
      typeName = typeName,
      entityProps = props,
      settings = ClusterShardingSettings(system),
      extractEntityId = extractEntityId,
      extractShardId = extractShardId
    ))

  def start()(implicit system: ActorSystem): GroupProcessorRegion = start(GroupProcessor.props)

  def startProxy()(implicit system: ActorSystem): GroupProcessorRegion =
    GroupProcessorRegion(ClusterSharding(system).startProxy(
      typeName = typeName,
      role = None,
      extractEntityId = extractEntityId,
      extractShardId = extractShardId
    ))
}

case class GroupProcessorRegion(ref: ActorRef)

case class GroupViewRegion(ref: ActorRef)