package im.actor.server.user

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.cluster.sharding.{ ClusterSharding, ClusterShardingSettings, ShardRegion }

object UserProcessorRegion {
  private def extractEntityId(system: ActorSystem): ShardRegion.ExtractEntityId = {
    {
      case c: UserCommand ⇒ (c.userId.toString, c)
      case q: UserQuery   ⇒ (q.userId.toString, q)
      case e @ UserEnvelope(
        userId,
        dialogRootEnvelope,
        dialogEnvelope
        ) ⇒
        (
          userId.toString,
          dialogRootEnvelope.getOrElse(dialogEnvelope.get)
        )
    }
  }

  private def extractShardId(system: ActorSystem): ShardRegion.ExtractShardId = {
    case c: UserCommand  ⇒ (c.userId % 100).toString // TODO: configurable
    case q: UserQuery    ⇒ (q.userId % 100).toString
    case e: UserEnvelope ⇒ (e.userId % 100).toString
  }

  val typeName = "UserProcessor"

  private def start(props: Props)(implicit system: ActorSystem): UserProcessorRegion =
    UserProcessorRegion(ClusterSharding(system).start(
      typeName = typeName,
      entityProps = props,
      settings = ClusterShardingSettings(system),
      extractEntityId = extractEntityId(system),
      extractShardId = extractShardId(system)
    ))

  def start()(implicit system: ActorSystem): UserProcessorRegion =
    start(UserProcessor.props)

  def startProxy()(implicit system: ActorSystem): UserProcessorRegion =
    UserProcessorRegion(ClusterSharding(system).startProxy(
      typeName = typeName,
      role = None,
      extractEntityId = extractEntityId(system),
      extractShardId = extractShardId(system)
    ))
}

final case class UserProcessorRegion(ref: ActorRef)