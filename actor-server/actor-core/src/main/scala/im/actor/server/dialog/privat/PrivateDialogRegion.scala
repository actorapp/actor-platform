package im.actor.server.dialog.privat

import akka.actor.{ ActorSystem, Props, ActorRef }
import akka.contrib.pattern.{ ClusterSharding, ShardRegion }

object PrivateDialogRegion {
  private val idExtractor: ShardRegion.IdExtractor = {
    case c: PrivateDialogCommand ⇒ (c.dialogId.stringId, c)
  }

  private val shardResolver: ShardRegion.ShardResolver = msg ⇒ msg match {
    case c: PrivateDialogCommand ⇒ (c.dialogId.left % 100).toString // TODO: configurable
  }

  val typeName = "PrivateDialog"

  private def start(props: Option[Props])(implicit system: ActorSystem): PrivateDialogRegion =
    PrivateDialogRegion(ClusterSharding(system).start(
      typeName = typeName,
      entryProps = props,
      idExtractor = idExtractor,
      shardResolver = shardResolver
    ))

  def start()(implicit system: ActorSystem): PrivateDialogRegion = start(Some(PrivateDialog.props))

  def startProxy()(implicit system: ActorSystem): PrivateDialogRegion =
    start(None)

}

case class PrivateDialogRegion(ref: ActorRef)