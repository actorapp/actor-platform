package im.actor.server.dialog.pair

import akka.actor.{ ActorSystem, Props, ActorRef }
import akka.contrib.pattern.{ ClusterSharding, ShardRegion }

object PairDialogRegion {
  private val idExtractor: ShardRegion.IdExtractor = {
    case c: PairDialogCommand ⇒ (c.dialogPath, c)
  }

  private val shardResolver: ShardRegion.ShardResolver = msg ⇒ msg match {
    case c: PairDialogCommand ⇒
      val leftId = (c.dialogPath split "_")(0).toInt
      (leftId % 100).toString // TODO: configurable
  }

  val typeName = "PairDialog"

  private def start(props: Option[Props])(implicit system: ActorSystem): PairDialogRegion =
    PairDialogRegion(ClusterSharding(system).start(
      typeName = typeName,
      entryProps = props,
      idExtractor = idExtractor,
      shardResolver = shardResolver
    ))

  def start()(implicit system: ActorSystem): PairDialogRegion = start(Some(PairDialog.props))

  def startProxy()(implicit system: ActorSystem): PairDialogRegion =
    start(None)

}

case class PairDialogRegion(ref: ActorRef)