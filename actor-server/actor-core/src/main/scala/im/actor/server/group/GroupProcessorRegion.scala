package im.actor.server.group

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.cluster.sharding.{ ClusterSharding, ClusterShardingSettings, ShardRegion }
import akka.event.Logging
import im.actor.server.dialog.DialogEnvelope
import im.actor.server.model.{ Peer, PeerType }

import scala.util.{ Success, Try }

object GroupProcessorRegion {
  private def extractEntityId(system: ActorSystem): ShardRegion.ExtractEntityId = {
    val log = Logging(system, getClass)

    {
      case c: GroupCommand ⇒ (c.groupId.toString, c)
      case q: GroupQuery   ⇒ (q.groupId.toString, q)
      case e @ DialogEnvelope(peer, command, query) ⇒ peer match {
        case Peer(PeerType.Group, groupId) ⇒
          Try(e.getField(DialogEnvelope.descriptor.findFieldByNumber(command.number))) match {
            case Success(any) ⇒ (groupId.toString, any)
            case _ ⇒
              val error = new RuntimeException(s"Payload not found for $e")
              log.error(error, error.getMessage)
              throw error
          }
        case Peer(peerType, _) ⇒ throw new RuntimeException(s"DialogCommand with peerType: $peerType passed in GroupProcessor")
      }
    }
  }

  private def extractShardId(system: ActorSystem): ShardRegion.ExtractShardId = msg ⇒ msg match {
    case c: GroupCommand ⇒ (c.groupId % 100).toString // TODO: configurable
    case q: GroupQuery   ⇒ (q.groupId % 100).toString
    case DialogEnvelope(peer, _, _) ⇒ peer match {
      case Peer(PeerType.Group, groupId) ⇒ (groupId % 100).toString
      case Peer(peerType, _)             ⇒ throw new RuntimeException(s"DialogCommand with peerType: $peerType passed in GroupProcessor")
    }
  }

  val typeName = "GroupProcessor"

  private def start(props: Props)(implicit system: ActorSystem): GroupProcessorRegion =
    GroupProcessorRegion(ClusterSharding(system).start(
      typeName = typeName,
      entityProps = props,
      settings = ClusterShardingSettings(system),
      extractEntityId = extractEntityId(system),
      extractShardId = extractShardId(system)
    ))

  def start()(implicit system: ActorSystem): GroupProcessorRegion = start(GroupProcessor.props)

  def startProxy()(implicit system: ActorSystem): GroupProcessorRegion =
    GroupProcessorRegion(ClusterSharding(system).startProxy(
      typeName = typeName,
      role = None,
      extractEntityId = extractEntityId(system),
      extractShardId = extractShardId(system)
    ))
}

case class GroupProcessorRegion(ref: ActorRef)

case class GroupViewRegion(ref: ActorRef)