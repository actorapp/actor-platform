package im.actor.server.api.rpc.service.messaging

import scala.concurrent.duration._

import akka.actor._
import akka.contrib.pattern.DistributedPubSubMediator
import akka.util.Timeout
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc.Implicits._
import im.actor.api.rpc.messaging._
import im.actor.api.rpc.peers.{ Peer, PeerType }
import im.actor.server.db.DbExtension
import im.actor.server.group.{ GroupProcessorRegion, GroupExtension, GroupViewRegion }
import im.actor.server.models
import im.actor.server.push.SeqUpdatesExtension
import im.actor.server.social.{ SocialExtension, SocialManagerRegion }
import im.actor.server.user.{ UserProcessorRegion, UserExtension, UserViewRegion }

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
    actorSystem: ActorSystem
  ): MessagingServiceImpl = {
    val onMessage = (MessagingService.publish _).curried(mediator)

    new MessagingServiceImpl(onMessage)
  }
}

final class MessagingServiceImpl(
  protected val onMessage: Events.PeerMessage ⇒ Unit
)(
  implicit
  protected val actorSystem: ActorSystem
)
  extends MessagingService with MessagingHandlers with HistoryHandlers {
  protected implicit val db: Database = DbExtension(actorSystem).db
  protected implicit val seqUpdExt: SeqUpdatesExtension = SeqUpdatesExtension(actorSystem)
  protected implicit val userProcessorRegion: UserProcessorRegion = UserExtension(actorSystem).processorRegion
  protected implicit val userViewRegion: UserViewRegion = UserExtension(actorSystem).viewRegion
  protected implicit val groupProcessorRegion: GroupProcessorRegion = GroupExtension(actorSystem).processorRegion
  protected implicit val groupViewRegion: GroupViewRegion = GroupExtension(actorSystem).viewRegion
  protected implicit val socialRegion: SocialManagerRegion = SocialExtension(actorSystem).region
  protected implicit val timeout = Timeout(30.seconds)
}
