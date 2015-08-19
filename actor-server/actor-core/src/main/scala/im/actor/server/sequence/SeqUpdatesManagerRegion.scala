package im.actor.server.sequence

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.contrib.pattern.{ ClusterSharding, ShardRegion }

case class SeqUpdatesManagerRegion(ref: ActorRef)

object SeqUpdatesManagerRegion {

  private val idExtractor: ShardRegion.IdExtractor = {
    case msg: SeqUpdatesManagerMessage ⇒ (msg.authId.toString, msg)
  }

  private val shardResolver: ShardRegion.ShardResolver = msg ⇒ msg match {
    case msg: SeqUpdatesManagerMessage ⇒ (msg.authId % 32).toString // TODO: configurable
  }

  private def start(props: Option[Props])(implicit system: ActorSystem): SeqUpdatesManagerRegion =
    SeqUpdatesManagerRegion(ClusterSharding(system).start(
      typeName = "SeqUpdatesManager",
      entryProps = props,
      idExtractor = idExtractor,
      shardResolver = shardResolver
    ))

  def start()(
    implicit
    system:            ActorSystem,
    googlePushManager: GooglePushManager,
    applePushManager:  ApplePushManager
  ): SeqUpdatesManagerRegion =
    start(Some(SeqUpdatesManagerActor.props))

  def startProxy()(implicit system: ActorSystem): SeqUpdatesManagerRegion = start(None)
}