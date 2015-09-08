package im.actor.server.api.rpc.service.messaging

import akka.actor._
import akka.contrib.pattern.DistributedPubSubMediator.{ Subscribe, SubscribeAck }
import akka.event.Logging
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{ HttpMethods, HttpRequest, RequestEntity, StatusCodes }
import akka.http.scaladsl.{ Http, HttpExt }
import akka.stream.scaladsl.Sink
import akka.stream.{ ActorMaterializer, Materializer }
import akka.util.Timeout
import com.google.protobuf.CodedInputStream
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import im.actor.api.rpc.messaging.{ ApiMessage, ApiTextMessage }
import im.actor.api.rpc.peers.{ ApiPeer, ApiPeerType }
import im.actor.server.{ KeyValueMappings, models }
import im.actor.server.models.PeerType.{ Group, Private }
import im.actor.server.user.{ UserExtension, UserOffice, UserViewRegion }
import im.actor.util.log.AnyRefLogSource
import play.api.libs.json.{ Format, Json }
import shardakka.ShardakkaExtension

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

object ReverseHooksWorker {
  private[messaging] case object Resubscribe

  def props(groupId: Int, token: String, mediator: ActorRef) = Props(classOf[ReverseHooksWorker], groupId, token, mediator)

  private[messaging] def interceptorGroupId(groupId: Int): String = s"group-$groupId"

  case class MessageToWebhook(command: String, text: Option[String], nick: Option[String])

  implicit val format: Format[MessageToWebhook] = Json.format[MessageToWebhook]
}

private[messaging] final class ReverseHooksWorker(groupId: Int, token: String, mediator: ActorRef)
  extends Actor
  with ActorLogging
  with AnyRefLogSource
  with PlayJsonSupport
  with CommandParser {

  import ReverseHooksWorker._

  private[this] implicit val system: ActorSystem = context.system
  private[this] implicit val ec: ExecutionContext = system.dispatcher
  private[this] implicit val timeout: Timeout = Timeout(5.seconds)
  private[this] implicit val materializer: Materializer = ActorMaterializer()
  private[this] implicit val userViewRegion: UserViewRegion = UserExtension(system).viewRegion

  private[this] val scheduledResubscribe = system.scheduler.schedule(Duration.Zero, 1.minute, self, Resubscribe)
  private[this] val reverseHooksKv = ShardakkaExtension(system).simpleKeyValue(KeyValueMappings.ReverseHooks + "_" + token)
  private[this] val http: HttpExt = Http()

  override val log = Logging(system, this)

  def receive = init

  def init: Receive = {
    case Resubscribe ⇒
      mediator ! Subscribe(MessagingService.messagesTopic(ApiPeer(ApiPeerType.Group, groupId)), None, self)
    case SubscribeAck(Subscribe(topic, _, _)) ⇒
      log.debug("Watching for group's {} reverse hooks", groupId)
      scheduledResubscribe.cancel()
      context become working
  }

  def working: Receive = {
    case Events.PeerMessage(from, _, _, _, message) ⇒
      log.debug("Got message from group {} to forward to webhook", groupId)
      val parsed = ApiMessage.parseFrom(CodedInputStream.newInstance(message.toByteArray))

      val optNickname = from match {
        case models.Peer(Private, id) ⇒ UserOffice.getApiStruct(id, 0, 0L) map (_.nick)
        case models.Peer(Group, _)    ⇒ Future.successful(None)
      }

      parsed.left foreach (_ ⇒ log.debug("Failed to parse message for groupId: {}", groupId))

      parsed.right map {
        case ApiTextMessage(content, _, _) ⇒
          parseCommand(content) map {
            case (command, text) ⇒
              for {
                keys ← reverseHooksKv.getKeys()

                nick ← optNickname
                message = MessageToWebhook(command, text, nick)
                entity ← Marshal(List(message)).to[RequestEntity]

                idUrls ← Future.sequence(keys map (k ⇒ reverseHooksKv.get(k) map (_ map (k → _)))) map (_.flatten)
                _ = log.debug("Will forward message {} from group {} to urls {}", message, groupId, idUrls.map(_._2))
                _ ← Future.sequence(idUrls map {
                  case (key, url) ⇒
                    log.debug("Forwarding message {} from group {} to url {}", message, groupId, url)
                    val request = HttpRequest(HttpMethods.POST, url, entity = entity)
                    val sendFuture = for {
                      resp ← http.singleRequest(request)
                      _ ← if (resp.status == StatusCodes.Gone) { reverseHooksKv.delete(key) } else { Future.successful(()) }
                      _ ← resp.entity.dataBytes.runWith(Sink.ignore)
                    } yield resp
                    sendFuture onComplete {
                      case Success(resp) ⇒
                        if (resp.status.isSuccess()) {
                          log.debug("Successfully forwarded message {} from group {} to url: {}, status: {}", message, groupId, url, resp.status)
                        } else {
                          log.debug("Failed to forward message {} from group {} to url: {}, status: {}", message, groupId, url, resp.status)
                        }
                      case Failure(e) ⇒
                        log.debug("Error while forwarding message {} from group {} to url: {}, error: {}", message, groupId, url, e)
                    }
                    sendFuture
                })
              } yield ()
          }
        case _ ⇒ log.debug("Does not support non text messages in reverse hooks")
      }
  }

}
