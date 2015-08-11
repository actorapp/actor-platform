package im.actor.server.peer

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.contrib.pattern.{ ClusterSharding, ShardRegion }

object GroupPeerRegion {
  private val idExtractor: ShardRegion.IdExtractor = {
    case c: GroupPeerCommand ⇒ (c.groupId.toString, c)
  }

  private val shardResolver: ShardRegion.ShardResolver = msg ⇒ msg match {
    case c: GroupPeerCommand ⇒ (c.groupId % 100).toString // TODO: configurable
  }

  val typeName = "GroupPeer"

  private def start(props: Option[Props])(implicit system: ActorSystem): GroupPeerRegion =
    GroupPeerRegion(ClusterSharding(system).start(
      typeName = typeName,
      entryProps = props,
      idExtractor = idExtractor,
      shardResolver = shardResolver
    ))

  def start()(implicit system: ActorSystem): GroupPeerRegion = start(Some(GroupPeer.props))

  def startProxy()(implicit system: ActorSystem): GroupPeerRegion =
    start(None)
}

case class GroupPeerRegion(ref: ActorRef)
