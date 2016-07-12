package im.actor.server.group

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.cluster.sharding.{ ClusterSharding, ClusterShardingSettings, ShardRegion }

object GroupProcessorRegion {
  private def extractEntityId(system: ActorSystem): ShardRegion.ExtractEntityId = {
    case GroupEnvelope(groupId, Some(dialogEnvelope), _, _) ⇒
      (
        groupId.toString,
        dialogEnvelope
      )
    case env @ GroupEnvelope(groupId, _, command, query) ⇒
      (
        groupId.toString,
        // payload
        if (query.isDefined) {
          env.getField(GroupEnvelope.descriptor.findFieldByNumber(query.number))
        } else {
          env.getField(GroupEnvelope.descriptor.findFieldByNumber(command.number))
        }
      )
  }

  private def extractShardId(system: ActorSystem): ShardRegion.ExtractShardId = {
    case env: GroupEnvelope ⇒ (env.groupId % 100).toString // TODO: configurable
  }

  private val typeName = "GroupProcessor"

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
