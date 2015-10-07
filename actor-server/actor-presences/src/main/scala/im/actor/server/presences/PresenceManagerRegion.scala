package im.actor.server.presences

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.contrib.pattern.{ ClusterSharding, ShardRegion }
import im.actor.server.presences.PresenceManager.Envelope

object PresenceManagerRegion {
  private val idExtractor: ShardRegion.IdExtractor = {
    case env @ Envelope(userId, payload) ⇒ (userId.toString, env)
  }

  private val shardResolver: ShardRegion.ShardResolver = msg ⇒ msg match {
    case Envelope(userId, _) ⇒ (userId % 32).toString // TODO: configurable
  }

  private val typeName = "PresenceManager"

  private def startRegion(props: Option[Props])(implicit system: ActorSystem): PresenceManagerRegion =
    PresenceManagerRegion(ClusterSharding(system).start(
      typeName = typeName,
      entryProps = props,
      idExtractor = idExtractor,
      shardResolver = shardResolver
    ))

  def startRegion()(implicit system: ActorSystem): PresenceManagerRegion = startRegion(Some(PresenceManager.props))

  def startRegionProxy()(implicit system: ActorSystem): PresenceManagerRegion = startRegion(None)

}

case class PresenceManagerRegion(val ref: ActorRef)