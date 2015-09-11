package im.actor.server.dialog.privat

import akka.actor.{ ActorSystem, Props, ActorRef }
import akka.contrib.pattern.{ ClusterSharding, ShardRegion }
import im.actor.server.dialog.{ DialogIdContainer, DialogCommand }

object PrivateDialogErrors {
  final object MessageToSelf extends Exception("Private dialog with self is not allowed")
}

object PrivateDialogRegion {

  private def idExtractor: ShardRegion.IdExtractor = {
    case c: DialogCommand ⇒ c.dialogId match {
      case DialogIdContainer(id) if id.isPrivat ⇒ (c.dialogId.getPrivat.stringId, c)
      case _                                    ⇒ throw new Exception("Unsupported dialogId")
    }
  }

  private def shardResolver: ShardRegion.ShardResolver = {
    case c: DialogCommand ⇒ c.dialogId match {
      case DialogIdContainer(id) if id.isPrivat ⇒ (c.dialogId.getPrivat.left % 100).toString
      case _                                    ⇒ throw new Exception("Unsupported dialogId")
    }
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