package im.actor.server.api.rpc.service.messaging

import akka.actor._
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc.Implicits._
import im.actor.api.rpc.messaging._
import im.actor.api.rpc.peers.Peer
import im.actor.server.models
import im.actor.server.push.SeqUpdatesManagerRegion
import im.actor.server.social.SocialManagerRegion

sealed trait Event

object Events {
  final case class PeerMessage(peer: Peer, senderUserId: Int, randomId: Long, message: Message) extends Event
}

object MessagingService {
  def messagesTopic(peer: models.Peer) = {
    val strType = peer.typ match {
      case models.PeerType.Private ⇒ "private"
      case models.PeerType.Group   ⇒ "group"
    }

    s"messaging.messages.${strType}.${peer.id}"
  }

  def messagesTopic(peer: Peer): String =
    messagesTopic(peer.asModel)
}

class MessagingServiceImpl(protected val mediator: ActorRef)(
  implicit
  val seqUpdManagerRegion: SeqUpdatesManagerRegion,
  val socialManagerRegion: SocialManagerRegion,
  val db:                  Database,
  val actorSystem:         ActorSystem
)
  extends MessagingService with MessagingHandlers with HistoryHandlers
