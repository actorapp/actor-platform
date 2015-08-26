package im.actor.server.api.rpc.service.messaging

import akka.actor._
import akka.contrib.pattern.DistributedPubSubMediator.{ Subscribe, SubscribeAck }
import akka.event.Logging
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.{ HttpMethods, HttpRequest }
import akka.stream.scaladsl.Sink
import akka.stream.{ ActorMaterializer, Materializer }
import akka.util.Timeout
import cats.data.Xor
import com.google.protobuf.CodedInputStream
import im.actor.api.rpc.messaging.{ Message, TextMessage }
import im.actor.api.rpc.peers.{ PeerType, Peer }
import im.actor.server.commons.KeyValueMappings
import im.actor.server.util.AnyRefLogSource
import play.api.libs.json.{ Format, Json }
import shardakka.ShardakkaExtension

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration._

object ReverseHooksWorker {
  private[messaging] case object Resubscribe

  def props(groupId: Int, token: String, mediator: ActorRef, http: HttpExt) = Props(classOf[ReverseHooksWorker], groupId, token, mediator, http)

  private[messaging] def interceptorGroupId(groupId: Int): String = s"group-$groupId"

  case class MessageToWebhook(text: String)

  implicit val format: Format[MessageToWebhook] = Json.format[MessageToWebhook]
}

class ReverseHooksWorker(groupId: Int, token: String, mediator: ActorRef, http: HttpExt) extends Actor with ActorLogging with AnyRefLogSource {

  import ReverseHooksWorker._

  implicit val system: ActorSystem = context.system
  implicit val ec: ExecutionContext = system.dispatcher
  implicit val timeout: Timeout = Timeout(5.seconds)
  implicit val materializer: Materializer = ActorMaterializer()

  private[this] val scheduledResubscribe = system.scheduler.schedule(Duration.Zero, 5.minutes, self, Resubscribe)
  private[this] val reverseHooksKv = ShardakkaExtension(system).simpleKeyValue(KeyValueMappings.ReverseHooks + "_" + token)

  override val log = Logging(system, this)

  def receive = init

  def init: Receive = {
    case Resubscribe ⇒
      mediator ! Subscribe(MessagingService.messagesTopic(Peer(PeerType.Group, groupId)), Some(interceptorGroupId(groupId)), self)
    case SubscribeAck(Subscribe(topic, _, _)) ⇒
      log.debug("Watching for group's {} reverse hooks", groupId)
      scheduledResubscribe.cancel()
      context become working
  }

  def working: Receive = {
    case Events.PeerMessage(_, _, _, _, message) ⇒
      val parsed = Message.parseFrom(CodedInputStream.newInstance(message.toByteArray))

      if (parsed.isLeft) log.debug("Failed to parse message for groupId: {}", groupId)

      Xor.fromEither(parsed) map {
        case TextMessage(text, _, _) ⇒
          for {
            keys ← reverseHooksKv.getKeys()
            urls ← Future.sequence(keys map reverseHooksKv.get) map (_.flatten)
            _ ← Future.sequence(urls map { url ⇒
              for {
                resp ← http.singleRequest(HttpRequest(HttpMethods.POST, url, entity = Json.stringify(Json.toJson(MessageToWebhook(text)))))
                _ = if (resp.status.isSuccess()) {
                  log.debug("Successfully forwarded message from group {} to url: {}, status: {}", groupId, url, resp.status.toString())
                } else {
                  log.debug("Failed to forwarded message from group {} to url: {}, status: {}", groupId, url, resp.status.toString())
                }
                _ ← resp.entity.dataBytes.runWith(Sink.ignore)
              } yield ()
            })
          } yield ()
        case _ ⇒ log.debug("Does not support non text messages in reverse hooks")
      }
  }

}
