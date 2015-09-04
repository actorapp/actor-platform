package im.actor.server.dialog.group

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.contrib.pattern.{ ClusterSharding, ShardRegion }

object GroupDialogRegion {
  private val idExtractor: ShardRegion.IdExtractor = {
    case c: GroupDialogCommand ⇒ (c.dialogId.groupId.toString, c)
  }

  private val shardResolver: ShardRegion.ShardResolver = msg ⇒ msg match {
    case c: GroupDialogCommand ⇒ (c.dialogId.groupId % 100).toString // TODO: configurable
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
