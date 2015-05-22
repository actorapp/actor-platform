package im.actor.server.api.rpc.service.messaging

import akka.actor._
import akka.contrib.pattern.DistributedPubSubMediator
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc.Implicits._
import im.actor.api.rpc.messaging._
import im.actor.api.rpc.peers.{ PeerType, Peer }
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

  def publish(mediator: ActorRef, message: Events.PeerMessage): Unit = {
    message.peer.`type` match {
      case PeerType.Private ⇒
        val senderTopic = MessagingService.messagesTopic(Peer(PeerType.Private, message.senderUserId))
        val receiverTopic = messagesTopic(message.peer)

        mediator ! DistributedPubSubMediator.Publish(senderTopic, message)
        mediator ! DistributedPubSubMediator.Publish(receiverTopic, message)
      case PeerType.Group ⇒
        val topic = messagesTopic(message.peer)
        mediator ! DistributedPubSubMediator.Publish(topic, message)
    }
  }
}

object MessagingServiceImpl {
  def apply(mediator: ActorRef)(
    implicit
    seqUpdManagerRegion: SeqUpdatesManagerRegion,
    socialManagerRegion: SocialManagerRegion,
    db:                  Database,
    actorSystem:         ActorSystem
  ): MessagingServiceImpl = {
    val onMessage = (MessagingService.publish _).curried(mediator)

    new MessagingServiceImpl(onMessage)
  }
}

class MessagingServiceImpl(
  protected val onMessage: Events.PeerMessage ⇒ Unit
)(
  implicit
  val seqUpdManagerRegion: SeqUpdatesManagerRegion,
  val socialManagerRegion: SocialManagerRegion,
  val db:                  Database,
  val actorSystem:         ActorSystem
)
  extends MessagingService with MessagingHandlers with HistoryHandlers
