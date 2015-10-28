package im.actor.server.dialog.group

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.cluster.sharding.{ ClusterShardingSettings, ClusterSharding, ShardRegion }
import im.actor.server.dialog.DialogCommand
import im.actor.server.dialog.DialogIdExtractors.getGroupDialogId

object GroupDialogRegion {

  private def extractEntityId: ShardRegion.ExtractEntityId = {
    case c: DialogCommand ⇒ (getGroupDialogId(c.dialogId).stringId, c)
  }

  private def extractShardId: ShardRegion.ExtractShardId = {
    case c: DialogCommand ⇒ (getGroupDialogId(c.dialogId).groupId % 10).toString
  }

  val typeName = "GroupDialog"

  private def start(props: Props)(implicit system: ActorSystem): GroupDialogRegion =
    GroupDialogRegion(ClusterSharding(system).start(
      typeName = typeName,
      entityProps = props,
      settings = ClusterShardingSettings(system),
      extractEntityId = extractEntityId,
      extractShardId = extractShardId
    ))

  def start()(implicit system: ActorSystem): GroupDialogRegion = start(GroupDialog.props)

  def startProxy()(implicit system: ActorSystem): GroupDialogRegion =
    GroupDialogRegion(ClusterSharding(system).startProxy(
      typeName = typeName,
      role = None,
      extractEntityId = extractEntityId,
      extractShardId = extractShardId
    ))
}

case class GroupDialogRegion(ref: ActorRef)
