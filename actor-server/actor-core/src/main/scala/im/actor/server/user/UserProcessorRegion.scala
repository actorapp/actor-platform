package im.actor.server.user

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.cluster.sharding.{ ClusterShardingSettings, ClusterSharding, ShardRegion }

object UserProcessorRegion {
  private val extractEntityId: ShardRegion.ExtractEntityId = {
    case c: UserCommand ⇒ (c.userId.toString, c)
    case q: UserQuery   ⇒ (q.userId.toString, q)
  }

  private val extractShardId: ShardRegion.ExtractShardId = msg ⇒ msg match {
    case c: UserCommand ⇒ (c.userId % 100).toString // TODO: configurable
    case q: UserQuery   ⇒ (q.userId % 100).toString
  }

  val typeName = "UserProcessor"

  private def start(props: Props)(implicit system: ActorSystem): UserProcessorRegion =
    UserProcessorRegion(ClusterSharding(system).start(
      typeName = typeName,
      entityProps = props,
      settings = ClusterShardingSettings(system),
      extractEntityId = extractEntityId,
      extractShardId = extractShardId
    ))

  def start()(implicit system: ActorSystem): UserProcessorRegion =
    start(UserProcessor.props)

  def startProxy()(implicit system: ActorSystem): UserProcessorRegion =
    UserProcessorRegion(ClusterSharding(system).startProxy(
      typeName = typeName,
      role = None,
      extractEntityId = extractEntityId,
      extractShardId = extractShardId
    ))
}

case class UserProcessorRegion(val ref: ActorRef)