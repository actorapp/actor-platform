package im.actor.server.api.rpc.service.messaging

import akka.actor._
import akka.contrib.pattern.DistributedPubSubMediator.{ Subscribe, SubscribeAck }
import akka.event.Logging
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{ RequestEntity, StatusCodes, HttpMethods, HttpRequest }
import akka.stream.scaladsl.Sink
import akka.stream.{ ActorMaterializer, Materializer }
import akka.util.Timeout
import com.google.protobuf.CodedInputStream
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import im.actor.api.rpc.messaging.{ Message, TextMessage }
import im.actor.api.rpc.peers.{ PeerType, Peer }
import im.actor.server.commons.KeyValueMappings
import im.actor.server.models
import im.actor.server.models.PeerType.{ Group, Private }
import im.actor.server.user.{ UserExtension, UserViewRegion, UserOffice }
import im.actor.server.util.AnyRefLogSource
import play.api.libs.json.{ Format, Json }
import shardakka.ShardakkaExtension

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration._

object ReverseHooksWorker {
  private[messaging] case object Resubscribe

  def props(groupId: Int, token: String, mediator: ActorRef, http: HttpExt) = Props(classOf[ReverseHooksWorker], groupId, token, mediator, http)

  private[messaging] def interceptorGroupId(groupId: Int): String = s"group-$groupId"

  case class MessageToWebhook(text: String, nick: Option[String])

  implicit val format: Format[MessageToWebhook] = Json.format[MessageToWebhook]
}

class ReverseHooksWorker(groupId: Int, token: String, mediator: ActorRef, http: HttpExt)
  extends Actor
  with ActorLogging
  with AnyRefLogSource
  with PlayJsonSupport {

  import ReverseHooksWorker._

  private[this] implicit val system: ActorSystem = context.system
  private[this] implicit val ec: ExecutionContext = system.dispatcher
  private[this] implicit val timeout: Timeout = Timeout(5.seconds)
  private[this] implicit val materializer: Materializer = ActorMaterializer()
  private[this] implicit val userViewRegion: UserViewRegion = UserExtension(system).viewRegion

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
    case Events.PeerMessage(from, _, _, _, message) ⇒
      val parsed = Message.parseFrom(CodedInputStream.newInstance(message.toByteArray))

      val optNickname = from match {
        case models.Peer(Private, id) ⇒ UserOffice.getApiStruct(id, 0, 0L) map (_.nick)
        case models.Peer(Group, _)    ⇒ Future.successful(None)
      }

      parsed.left foreach (_ ⇒ log.debug("Failed to parse message for groupId: {}", groupId))

      parsed.right map {
        case TextMessage(text, _, _) ⇒
          for {
            keys ← reverseHooksKv.getKeys()

            nick ← optNickname
            entity ← Marshal(List(MessageToWebhook(text, nick))).to[RequestEntity]

            idUrls ← Future.sequence(keys map (k ⇒ reverseHooksKv.get(k) map (_ map (k → _)))) map (_.flatten)
            _ ← Future.sequence(idUrls map {
              case (key, url) ⇒
                val request = HttpRequest(HttpMethods.POST, url, entity = entity)
                for {
                  resp ← http.singleRequest(request)

                  _ ← if (resp.status == StatusCodes.Gone) { reverseHooksKv.delete(key) } else { Future.successful(()) }

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
