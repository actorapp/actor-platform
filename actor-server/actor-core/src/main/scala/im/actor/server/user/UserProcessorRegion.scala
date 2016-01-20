package im.actor.server.user

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.cluster.sharding.{ ClusterSharding, ClusterShardingSettings, ShardRegion }
import akka.event.Logging
import im.actor.server.dialog.DialogCommands.Envelope
import im.actor.server.model.{ Peer, PeerType }

object UserProcessorRegion {
  private def extractEntityId(system: ActorSystem): ShardRegion.ExtractEntityId = {
    val log = Logging(system, getClass)

    {
      case c: UserCommand ⇒ (c.userId.toString, c)
      case q: UserQuery   ⇒ (q.userId.toString, q)
      case e @ Envelope(peer, payload) ⇒ peer match {
        case Peer(PeerType.Private, userId) ⇒
          e.getField(Envelope.descriptor.findFieldByNumber(payload.number)) match {
            case Some(any) ⇒ (userId.toString, any)
            case None ⇒
              val error = new RuntimeException(s"Payload not found for $e")
              log.error(error, error.getMessage)
              throw error
          }
        case Peer(peerType, _) ⇒ throw new RuntimeException(s"DialogCommand with peerType: $peerType passed in UserProcessor")
      }
    }
  }

  private def extractShardId(system: ActorSystem): ShardRegion.ExtractShardId = msg ⇒ msg match {
    case c: UserCommand ⇒ (c.userId % 100).toString // TODO: configurable
    case q: UserQuery   ⇒ (q.userId % 100).toString
    case Envelope(peer, payload) ⇒ peer match {
      case Peer(PeerType.Private, userId) ⇒ (userId % 100).toString
      case Peer(peerType, _)              ⇒ throw new RuntimeException(s"DialogCommand with peerType: $peerType passed in UserProcessor")
    }
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

case class UserProcessorRegion(val ref: ActorRef)