package im.actor.server.dialog.group

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.contrib.pattern.{ ClusterSharding, ShardRegion }
import im.actor.server.dialog.DialogCommand
import im.actor.server.dialog.DialogIdExtractors.getGroupDialogId

object GroupDialogRegion {

  private def idExtractor: ShardRegion.IdExtractor = {
    case c: DialogCommand ⇒ (getGroupDialogId(c.dialogId).stringId, c)
  }

  private def shardResolver: ShardRegion.ShardResolver = {
    case c: DialogCommand ⇒ (getGroupDialogId(c.dialogId).groupId % 100).toString
  }

  val typeName = "GroupDialog"

  private def start(props: Option[Props])(implicit system: ActorSystem): GroupDialogRegion =
    GroupDialogRegion(ClusterSharding(system).start(
      typeName = typeName,
      entryProps = props,
      idExtractor = idExtractor,
      shardResolver = shardResolver
    ))

  def start()(implicit system: ActorSystem): GroupDialogRegion = start(Some(GroupDialog.props))

  def startProxy()(implicit system: ActorSystem): GroupDialogRegion =
    start(None)
}

case class GroupDialogRegion(ref: ActorRef)
