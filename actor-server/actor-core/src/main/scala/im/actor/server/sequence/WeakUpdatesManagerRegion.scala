package im.actor.server.sequence

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.contrib.pattern.{ ClusterSharding, ShardRegion }
import im.actor.server.sequence.WeakUpdatesManager.Envelope

object WeakUpdatesManagerRegion {
  private val idExtractor: ShardRegion.IdExtractor = {
    case env @ Envelope(authId, payload) ⇒ (authId.toString, env)
  }

  private val shardResolver: ShardRegion.ShardResolver = msg ⇒ msg match {
    case Envelope(authId, _) ⇒ (authId % 32).toString // TODO: configurable
  }

  private val typeName = "WeakUpdatesManager"

  private def startRegion(props: Option[Props])(implicit system: ActorSystem): WeakUpdatesManagerRegion = {
    WeakUpdatesManagerRegion(ClusterSharding(system).start(
      typeName = typeName,
      entryProps = props,
      idExtractor = idExtractor,
      shardResolver = shardResolver
    ))
  }

  def startRegion()(implicit system: ActorSystem): WeakUpdatesManagerRegion = startRegion(Some(WeakUpdatesManager.props))

  def startRegionProxy()(implicit system: ActorSystem): WeakUpdatesManagerRegion = startRegion(None)

}

case class WeakUpdatesManagerRegion(ref: ActorRef)
