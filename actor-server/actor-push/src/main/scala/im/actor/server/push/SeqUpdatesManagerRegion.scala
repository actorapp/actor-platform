package im.actor.server.push

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.contrib.pattern.{ ClusterSharding, ShardRegion }
import slick.driver.PostgresDriver.api._

import im.actor.server.push.SeqUpdatesManager.Envelope

case class SeqUpdatesManagerRegion(ref: ActorRef)

object SeqUpdatesManagerRegion {

  private val idExtractor: ShardRegion.IdExtractor = {
    case env @ Envelope(authId, payload) ⇒ (authId.toString, payload)
  }

  private val shardResolver: ShardRegion.ShardResolver = msg ⇒ msg match {
    case Envelope(authId, _) ⇒ (authId % 32).toString // TODO: configurable
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
    applePushManager:  ApplePushManager,
    db:                Database
  ): SeqUpdatesManagerRegion =
    start(Some(SeqUpdatesManagerActor.props))

  def startProxy()(implicit system: ActorSystem): SeqUpdatesManagerRegion = start(None)
}