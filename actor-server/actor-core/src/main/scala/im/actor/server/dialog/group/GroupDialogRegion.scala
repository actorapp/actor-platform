package im.actor.server.dialog.group

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.contrib.pattern.{ ClusterSharding, ShardRegion }
import im.actor.server.dialog.{ DialogIdContainer, DialogCommand }

object GroupDialogRegion {

  private def idExtractor: ShardRegion.IdExtractor = {
    case c: DialogCommand ⇒ c.dialogId match {
      case DialogIdContainer(id) if id.isGroup ⇒ (c.dialogId.getGroup.stringId, c)
      case _                                   ⇒ throw new Exception("Unsupported dialogId")
    }
  }

  private def shardResolver: ShardRegion.ShardResolver = {
    case c: DialogCommand ⇒ c.dialogId match {
      case DialogIdContainer(id) if id.isGroup ⇒ (c.dialogId.getGroup.groupId % 100).toString
      case _                                   ⇒ throw new Exception("Unsupported dialogId")
    }
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
