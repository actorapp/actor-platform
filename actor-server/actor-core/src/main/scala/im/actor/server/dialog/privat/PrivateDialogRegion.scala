package im.actor.server.dialog.privat

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.cluster.sharding.{ ClusterShardingSettings, ClusterSharding, ShardRegion }
import im.actor.server.dialog.DialogCommand
import im.actor.server.dialog.DialogIdExtractors.getPrivateDialogId

object PrivateDialogErrors {
  final object MessageToSelf extends Exception("Private dialog with self is not allowed")
}

object PrivateDialogRegion {

  private def extractEntityId: ShardRegion.ExtractEntityId = {
    case c: DialogCommand ⇒ (getPrivateDialogId(c.dialogId).stringId, c)
  }

  private def extractShardId: ShardRegion.ExtractShardId = {
    case c: DialogCommand ⇒ (getPrivateDialogId(c.dialogId).left % 100).toString
  }

  val typeName = "PrivateDialog"

  private def start(props: Props)(implicit system: ActorSystem): PrivateDialogRegion =
    PrivateDialogRegion(ClusterSharding(system).start(
      typeName = typeName,
      entityProps = props,
      settings = ClusterShardingSettings(system),
      extractEntityId = extractEntityId,
      extractShardId = extractShardId
    ))

  def start()(implicit system: ActorSystem): PrivateDialogRegion = start(PrivateDialog.props)

  def startProxy()(implicit system: ActorSystem): PrivateDialogRegion =
    PrivateDialogRegion(ClusterSharding(system).startProxy(
      typeName = typeName,
      role = None,
      extractEntityId = extractEntityId,
      extractShardId = extractShardId
    ))

}

case class PrivateDialogRegion(ref: ActorRef)