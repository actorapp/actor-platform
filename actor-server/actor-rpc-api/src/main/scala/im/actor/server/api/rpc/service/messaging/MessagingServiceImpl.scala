package im.actor.server.api.rpc.service.messaging

import akka.actor._
import akka.contrib.pattern.DistributedPubSubMediator
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc.Implicits._
import im.actor.api.rpc.messaging._
import im.actor.api.rpc.peers.{ PeerType, Peer }
import im.actor.server.peermanagers.{ UserEntityRegion, GroupPeerManagerRegion }
import im.actor.server.models
import im.actor.server.push.SeqUpdatesManagerRegion
import im.actor.server.social.SocialManagerRegion

sealed trait Event

object Events {
  final case class PeerMessage(fromPeer: models.Peer, toPeer: models.Peer, randomId: Long, date: Long, message: Message) extends Event
}

object MessagingService {
  val privateMessagesTopic: String = "messaging.messages.private"
  val groupMessagesTopic: String = "messaging.messages.group"

  def messagesTopic(peer: models.Peer): String = {
    val strType = peer.typ match {
      case models.PeerType.Private ⇒ "private"
      case models.PeerType.Group   ⇒ "group"
    }

    s"messaging.messages.${strType}.${peer.id}"
  }

  def messagesTopic(peer: Peer): String =
    messagesTopic(peer.asModel)

  def publish(mediator: ActorRef, message: Events.PeerMessage): Unit = {
    message.toPeer.typ match {
      case models.PeerType.Private ⇒
        val senderTopic = MessagingService.messagesTopic(Peer(PeerType.Private, message.fromPeer.id))
        val receiverTopic = messagesTopic(message.toPeer)

        mediator ! DistributedPubSubMediator.Publish(privateMessagesTopic, message, sendOneMessageToEachGroup = true)
        mediator ! DistributedPubSubMediator.Publish(senderTopic, message, sendOneMessageToEachGroup = true)
        mediator ! DistributedPubSubMediator.Publish(receiverTopic, message, sendOneMessageToEachGroup = true)
      case models.PeerType.Group ⇒
        val topic = messagesTopic(message.toPeer)

        mediator ! DistributedPubSubMediator.Publish(groupMessagesTopic, message, sendOneMessageToEachGroup = true)
        mediator ! DistributedPubSubMediator.Publish(topic, message, sendOneMessageToEachGroup = true)
    }
  }
}

object MessagingServiceImpl {
  def apply(mediator: ActorRef)(
    implicit
    privatePeerManagerRegion: UserEntityRegion,
    groupPeerManagerRegion:   GroupPeerManagerRegion,
    seqUpdManagerRegion:      SeqUpdatesManagerRegion,
    socialManagerRegion:      SocialManagerRegion,
    db:                       Database,
    actorSystem:              ActorSystem
  ): MessagingServiceImpl = {
    val onMessage = (MessagingService.publish _).curried(mediator)

    new MessagingServiceImpl(onMessage)
  }
}

class MessagingServiceImpl(
  protected val onMessage: Events.PeerMessage ⇒ Unit
)(
  implicit
  protected val privatePeerManagerRegion: UserEntityRegion,
  protected val groupPeerManagerRegion:   GroupPeerManagerRegion,
  protected val seqUpdManagerRegion:      SeqUpdatesManagerRegion,
  protected val socialManagerRegion:      SocialManagerRegion,
  protected val db:                       Database,
  protected val actorSystem:              ActorSystem
)
  extends MessagingService with MessagingHandlers with HistoryHandlers
