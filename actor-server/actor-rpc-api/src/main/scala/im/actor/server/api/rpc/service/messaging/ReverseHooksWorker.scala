package im.actor.server.api.rpc.service.messaging

import akka.actor._
import akka.cluster.pubsub.DistributedPubSubMediator.{ Subscribe, SubscribeAck }
import akka.event.Logging
import akka.http.scaladsl.util.FastFuture
import akka.util.Timeout
import com.google.protobuf.CodedInputStream
import im.actor.api.rpc.messaging.{ ApiMessage, ApiTextMessage }
import im.actor.api.rpc.peers.{ ApiPeer, ApiPeerType }
import im.actor.server.model.PeerType.{ Group, Private }
import im.actor.server.pubsub.{ PeerMessage, PubSubExtension }
import im.actor.server.user.UserExtension
import im.actor.server.{ KeyValueMappings, model }
import im.actor.util.log.AnyRefLogSource
import play.api.libs.json.{ Format, Json }
import shardakka.ShardakkaExtension
import spray.httpx.PlayJsonSupport
import spray.httpx.marshalling._
import spray.client.pipelining._
import spray.http.HttpMethods.POST
import spray.http.{ HttpRequest, StatusCodes }

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

object ReverseHooksWorker {
  private[messaging] case object Resubscribe

  def props(groupId: Int, token: String) = Props(classOf[ReverseHooksWorker], groupId, token)

  private[messaging] def interceptorGroupId(groupId: Int): String = s"group-$groupId"

  case class MessageToWebhook(command: String, text: Option[String], nick: Option[String])

  implicit val format: Format[MessageToWebhook] = Json.format[MessageToWebhook]
}

private[messaging] final class ReverseHooksWorker(groupId: Int, token: String)
  extends Actor
  with ActorLogging
  with AnyRefLogSource
  with PlayJsonSupport
  with CommandParser {

  import ReverseHooksWorker._

  private[this] implicit val system: ActorSystem = context.system
  private[this] implicit val ec: ExecutionContext = system.dispatcher
  private[this] implicit val timeout: Timeout = Timeout(5.seconds)

  private[this] val scheduledResubscribe = system.scheduler.schedule(Duration.Zero, 1.minute, self, Resubscribe)
  private[this] val reverseHooksKv = ShardakkaExtension(system).simpleKeyValue(KeyValueMappings.ReverseHooks + "_" + token)
  private[this] val pubSubExt = PubSubExtension(system)
  private[this] val singleRequest = sendReceive

  override val log = Logging(system, this)

  def receive = init

  def init: Receive = {
    case Resubscribe ⇒
      pubSubExt.subscribe(Subscribe(pubSubExt.messagesTopic(ApiPeer(ApiPeerType.Group, groupId)), None, self))
    case SubscribeAck(Subscribe(topic, _, _)) ⇒
      log.debug("Watching for group's {} reverse hooks", groupId)
      scheduledResubscribe.cancel()
      context become working
  }

  def working: Receive = {
    case PeerMessage(from, _, _, _, message) ⇒
      log.debug("Got message from group {}, peer {} to forward to webhook", groupId, from)
      val parsed = ApiMessage.parseFrom(CodedInputStream.newInstance(message.toByteArray))

      val optNickname = from match {
        case model.Peer(Group, _) ⇒
          FastFuture.successful(None)
        case model.Peer(Private, id) ⇒
          UserExtension(system).getApiStruct(id, 0, 0L) map (_.nick)
      }

      parsed.left foreach (_ ⇒ log.debug("Failed to parse message for groupId: {}", groupId))

      parsed.right map {
        case ApiTextMessage(content, _, _) ⇒
          parseCommand(content) foreach {
            case (command, text) ⇒
              (for {
                keys ← reverseHooksKv.getKeys()

                nick ← optNickname
                message = MessageToWebhook(command, text, nick)
                entity ← marshal(List(message)) match {
                  case Right(marshaled) ⇒ FastFuture.successful(marshaled)
                  case Left(err)        ⇒ FastFuture.failed(err)
                }
                idUrls ← Future.sequence(keys map (k ⇒ reverseHooksKv.get(k) map (_ map (k → _)))) map (_.flatten)
                _ = log.debug("Will forward message {} from group {} to urls {}", message, groupId, idUrls.map(_._2))
                _ ← Future.sequence(idUrls map {
                  case (key, url) ⇒
                    log.debug("Forwarding message {} from group {} to url {}", message, groupId, url)
                    val request = HttpRequest(POST, url, entity = entity)
                    val sendFuture = for {
                      resp ← singleRequest(request)
                      _ ← if (resp.status == StatusCodes.Gone) { reverseHooksKv.delete(key) } else { FastFuture.successful(()) }
                    } yield resp

                    sendFuture onComplete {
                      case Success(resp) ⇒
                        if (resp.status.isSuccess) {
                          log.debug("Successfully forwarded message {} from group {} to url: {}, status: {}", message, groupId, url, resp.status)
                        } else {
                          log.debug("Failed to forward message {} from group {} to url: {}, status: {}", message, groupId, url, resp.status)
                        }
                      case Failure(e) ⇒
                        log.debug("Error while forwarding message {} from group {} to url: {}, error: {}", message, groupId, url, e)
                    }
                    sendFuture
                })
              } yield ()) onFailure {
                case e ⇒ log.error(e, "Failed to process message")
              }
          }
        case _ ⇒ log.debug("Does not support non text messages in reverse hooks")
      }
  }

}
