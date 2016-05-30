package im.actor.server.enrich

import akka.actor._
import akka.cluster.pubsub.DistributedPubSubMediator.{ Subscribe, SubscribeAck }
import akka.event.Logging
import akka.util.Timeout
import com.sksamuel.scrimage.Image
import com.sksamuel.scrimage.nio.JpegWriter
import im.actor.api.rpc.files.ApiFastThumb
import im.actor.api.rpc.messaging._
import im.actor.server.file._
import im.actor.server.messaging.MessageUpdating
import im.actor.server.pubsub.{ PeerMessage, PubSubExtension }
import im.actor.util.log.AnyRefLogSource
import org.joda.time.DateTime
import spray.http.Uri

import scala.concurrent.duration._
import scala.util.{ Failure, Success, Try }

object RichMessageWorker {
  val groupId = Some("RichMessageWorker")

  def startWorker(config: RichMessageConfig)(implicit system: ActorSystem): ActorRef =
    system.actorOf(Props(classOf[RichMessageWorker], config), "rich-message-worker")
}

final class RichMessageWorker(config: RichMessageConfig) extends Actor with ActorLogging with MessageUpdating {

  import AnyRefLogSource._
  import PreviewMaker._
  import RichMessageWorker._

  private implicit val system: ActorSystem = context.system
  import system.dispatcher
  private implicit val timeout: Timeout = Timeout(10.seconds)

  private val pubSubExt = PubSubExtension(system)
  private val fsAdapter: FileStorageAdapter = FileStorageExtension(context.system).fsAdapter

  override val log = Logging(system, this)

  private val previewMaker = PreviewMaker(config, "previewMaker")

  private val privateSubscribe = Subscribe(pubSubExt.privateMessagesTopic, groupId, self)
  private val publicSubscribe = Subscribe(pubSubExt.groupMessagesTopic, None, self)

  pubSubExt.subscribe(privateSubscribe)
  pubSubExt.subscribe(publicSubscribe)

  def receive: Receive = subscribing(privateAckReceived = false, groupAckReceived = false)

  def subscribing(privateAckReceived: Boolean, groupAckReceived: Boolean): Receive = {
    case SubscribeAck(`privateSubscribe`) ⇒
      if (groupAckReceived)
        context.become(ready)
      else
        context.become(subscribing(privateAckReceived = true, groupAckReceived = groupAckReceived))
    case SubscribeAck(`publicSubscribe`) ⇒
      if (privateAckReceived)
        context.become(ready)
      else
        context.become(subscribing(privateAckReceived, groupAckReceived = true))
  }

  def ready: Receive = {
    case PeerMessage(fromPeer, toPeer, randomId, _, message) ⇒
      message match {
        case ApiTextMessage(text, _, _) ⇒
          Try(Uri.parseAbsolute(text.trim)) match {
            case Success(uri) ⇒
              log.debug("TextMessage with uri: {}", uri)
              previewMaker ! GetPreview(uri.toString(), fromPeer.id, toPeer, randomId)
            case Failure(_) ⇒
          }
        case _ ⇒
      }
    case PreviewSuccess(imageBytes, optFileName, mimeType, clientUserId, peer, randomId) ⇒
      log.debug("PreviewSuccess for message with randomId: {}, fileName: {}, mimeType: {}", randomId, optFileName, mimeType)
      val fullName = optFileName getOrElse {
        val name = (new DateTime).toString("yyyyMMddHHmmss")
        val ext = Try(mimeType.split("/").last).getOrElse("tmp")
        s"$name.$ext"
      }
      val image = Image(imageBytes.toArray).toPar
      for {
        location ← fsAdapter.uploadFileF(UnsafeFileName(fullName), imageBytes.toArray)
        thumb ← ImageUtils.scaleTo(image, 90)
        thumbBytes = thumb.toImage.forWriter(JpegWriter()).bytes

        _ = log.debug("uploaded file to location {}", location)
        _ = log.debug("image with width: {}, height: {}", image.width, image.height)

        updated = ApiDocumentMessage(
          fileId = location.fileId,
          accessHash = location.accessHash,
          fileSize = imageBytes.size,
          name = fullName,
          mimeType = mimeType,
          thumb = Some(ApiFastThumb(thumb.width, thumb.height, thumbBytes)),
          ext = Some(ApiDocumentExPhoto(image.width, image.height))
        )
        _ ← updateMessageContent(clientUserId, peer, randomId, updated)
      } yield ()
    case PreviewFailure(mess, randomId) ⇒
      log.debug("failed to make preview for message with randomId: {}, cause: {} ", randomId, mess)
  }

}
