package im.actor.server.sequence

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.cluster.sharding.{ ClusterSharding, ClusterShardingSettings, ShardRegion }

final case class SeqUpdatesManagerRegion(ref: ActorRef)

object SeqUpdatesManagerRegion {

  import UserSequenceCommands._

  private def extractEntityId(system: ActorSystem): ShardRegion.ExtractEntityId = {
    case e @ Envelope(userId, payload) ⇒ (userId.toString, e.getField(Envelope.descriptor.findFieldByNumber(payload.number)) match {
      case Some(any) ⇒ any
      case None ⇒
        val error = new RuntimeException(s"Payload not found for $e")
        system.log.error(error, error.getMessage)
        throw error
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
      extractEntityId = extractEntityId(system),
      extractShardId = extractShardId
    ))

  def start()(
    implicit
    system:            ActorSystem,
    googlePushManager: GooglePushManager,
    applePushManager:  ApplePushManager
  ): SeqUpdatesManagerRegion =
    start(UserSequence.props(googlePushManager, applePushManager))

  def startProxy()(implicit system: ActorSystem): SeqUpdatesManagerRegion =
    SeqUpdatesManagerRegion(ClusterSharding(system).startProxy(
      typeName = typeName,
      role = None,
      extractEntityId = extractEntityId(system),
      extractShardId = extractShardId
    ))
}