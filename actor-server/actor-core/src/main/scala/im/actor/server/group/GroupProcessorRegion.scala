package im.actor.server.group

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.cluster.sharding.{ ClusterSharding, ClusterShardingSettings, ShardRegion }
import akka.event.Logging

object GroupProcessorRegion {
  private def extractEntityId(system: ActorSystem): ShardRegion.ExtractEntityId = {
    val log = Logging(system, getClass)

    {
      case c: GroupCommand ⇒ (c.groupId.toString, c)
      case q: GroupQuery   ⇒ (q.groupId.toString, q)
      case GroupEnvelope(groupId, dialogEnvelope) ⇒
        (
          groupId.toString,
          dialogEnvelope.get
        )
    }
  }

  private def extractShardId(system: ActorSystem): ShardRegion.ExtractShardId = msg ⇒ msg match {
    case c: GroupCommand  ⇒ (c.groupId % 100).toString // TODO: configurable
    case q: GroupQuery    ⇒ (q.groupId % 100).toString
    case e: GroupEnvelope ⇒ (e.groupId % 100).toString
  }

  val typeName = "GroupProcessor"

  private def start(props: Props)(implicit system: ActorSystem): GroupProcessorRegion =
    GroupProcessorRegion(ClusterSharding(system).start(
      typeName = typeName,
      entityProps = props,
      settings = ClusterShardingSettings(system),
      extractEntityId = extractEntityId(system),
      extractShardId = extractShardId(system)
    ))

  def start()(implicit system: ActorSystem): GroupProcessorRegion = start(GroupProcessor.props)

  def startProxy()(implicit system: ActorSystem): GroupProcessorRegion =
    GroupProcessorRegion(ClusterSharding(system).startProxy(
      typeName = typeName,
      role = None,
      extractEntityId = extractEntityId(system),
      extractShardId = extractShardId(system)
    ))
}

case class GroupProcessorRegion(ref: ActorRef)

case class GroupViewRegion(ref: ActorRef)