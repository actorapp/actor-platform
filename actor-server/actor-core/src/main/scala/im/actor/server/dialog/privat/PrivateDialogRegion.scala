package im.actor.server.dialog.privat

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.contrib.pattern.{ ClusterSharding, ShardRegion }
import im.actor.server.dialog.DialogCommand
import im.actor.server.dialog.DialogIdExtractors.getPrivateDialogId

object PrivateDialogErrors {
  final object MessageToSelf extends Exception("Private dialog with self is not allowed")
}

object PrivateDialogRegion {

  private def idExtractor: ShardRegion.IdExtractor = {
    case c: DialogCommand ⇒ (getPrivateDialogId(c.dialogId).stringId, c)
  }

  private def shardResolver: ShardRegion.ShardResolver = {
    case c: DialogCommand ⇒ (getPrivateDialogId(c.dialogId).left % 100).toString
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