package im.actor.server.pubsub

import akka.actor._
import akka.cluster.pubsub.DistributedPubSubMediator.{ Publish, Subscribe }
import akka.cluster.pubsub.{ DistributedPubSub, DistributedPubSubMediator }
import im.actor.api.rpc.PeersImplicits
import im.actor.api.rpc.messaging.ApiMessage
import im.actor.api.rpc.peers.{ ApiPeerType, ApiPeer }
import im.actor.server.model.{ PeerType, Peer }

sealed trait PubSubExtension extends Extension

final case class PeerMessage(fromPeer: Peer, toPeer: Peer, randomId: Long, date: Long, message: ApiMessage)

final class PubSubExtensionImpl(system: ActorSystem) extends PubSubExtension with PeersImplicits {

  val privateMessagesTopic: String = "messaging.messages.private"
  val groupMessagesTopic: String = "messaging.messages.group"

  private val mediator = DistributedPubSub(system).mediator

  def messagesTopic(peer: Peer): String = {
    val strType = peer.typ match {
      case PeerType.Private ⇒ "private"
      case PeerType.Group   ⇒ "group"
      case _                ⇒ throw new RuntimeException(s"Unknown peer type ${peer.typ}")
    }

    s"messaging.messages.$strType.${peer.id}"
  }

  def messagesTopic(peer: ApiPeer): String =
    messagesTopic(peer.asModel)

  //need to pass SubscribeAck back to subscribing actor(which is `context.self`)
  def subscribe(subscribe: Subscribe)(implicit context: ActorContext): Unit = mediator.tell(subscribe, context.self)

  def publish(publish: Publish): Unit = mediator ! publish

  def publish(message: PeerMessage): Unit = {
    message.toPeer.typ match {
      case PeerType.Private ⇒
        val senderTopic = messagesTopic(ApiPeer(ApiPeerType.Private, message.fromPeer.id))
        val receiverTopic = messagesTopic(message.toPeer)

        mediator ! DistributedPubSubMediator.Publish(privateMessagesTopic, message, sendOneMessageToEachGroup = true)
        mediator ! DistributedPubSubMediator.Publish(senderTopic, message, sendOneMessageToEachGroup = true)
        mediator ! DistributedPubSubMediator.Publish(receiverTopic, message, sendOneMessageToEachGroup = true)
      case PeerType.Group ⇒
        val topic = messagesTopic(message.toPeer)

        mediator ! DistributedPubSubMediator.Publish(groupMessagesTopic, message, sendOneMessageToEachGroup = false)
        mediator ! DistributedPubSubMediator.Publish(topic, message, sendOneMessageToEachGroup = false)
      case unknown ⇒ throw new RuntimeException(s"Unknown peer type $unknown")
    }
  }
}

object PubSubExtension extends ExtensionId[PubSubExtensionImpl] with ExtensionIdProvider {
  override def lookup = PubSubExtension

  override def createExtension(system: ExtendedActorSystem) = new PubSubExtensionImpl(system)
}
