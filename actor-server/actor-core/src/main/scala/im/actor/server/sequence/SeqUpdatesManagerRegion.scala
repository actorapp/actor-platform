package im.actor.server.sequence

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.cluster.sharding.{ ClusterSharding, ClusterShardingSettings, ShardRegion }

case class SeqUpdatesManagerRegion(ref: ActorRef)

object SeqUpdatesManagerRegion {

  import UserSequenceCommands._

  private val extractEntityId: ShardRegion.ExtractEntityId = {
    case e @ Envelope(userId, payload) ⇒ (userId.toString, e.getField(Envelope.descriptor.findFieldByNumber(payload.number)) match {
      case Some(any) ⇒ any
      case None      ⇒ throw new RuntimeException(s"Payload not found for $e")
    })
  }

  private val extractShardId: ShardRegion.ExtractShardId = {
    case Envelope(userId, _) ⇒ (userId % 10).toString // TODO: configurable
  }

  private val typeName = "SeqUpdatesManager"

  private def start(props: Props)(implicit system: ActorSystem): SeqUpdatesManagerRegion =
    SeqUpdatesManagerRegion(ClusterSharding(system).start(
      typeName = typeName,
      entityProps = props,
      settings = ClusterShardingSettings(system),
      extractEntityId = extractEntityId,
      extractShardId = extractShardId
    ))

  def start()(
    implicit
    system:            ActorSystem,
    googlePushManager: GooglePushManager,
    applePushManager:  ApplePushManager
  ): SeqUpdatesManagerRegion =
    start(UserSequence.props)

  def startProxy()(implicit system: ActorSystem): SeqUpdatesManagerRegion =
    SeqUpdatesManagerRegion(ClusterSharding(system).startProxy(
      typeName = typeName,
      role = None,
      extractEntityId = extractEntityId,
      extractShardId = extractShardId
    ))
}