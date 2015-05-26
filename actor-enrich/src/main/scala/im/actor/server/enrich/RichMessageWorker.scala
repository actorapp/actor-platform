package im.actor.server.enrich

import scala.concurrent.ExecutionContextExecutor
import scala.util.Try

import akka.actor._
import akka.contrib.pattern.DistributedPubSubMediator
import akka.pattern.pipe
import akka.stream.FlowMaterializer
import com.sksamuel.scrimage.AsyncImage
import org.joda.time.DateTime
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc.Implicits._
import im.actor.api.rpc.messaging._
import im.actor.server.api.rpc.service.messaging.Events
import im.actor.server.api.rpc.service.messaging.MessagingService._
import im.actor.server.models.PeerType
import im.actor.server.push.{ SeqUpdatesManager, SeqUpdatesManagerRegion }
import im.actor.server.util.{ FileUtils, UploadManager }
import im.actor.server.{ models, persist }

object RichMessageWorker {
  val groupId = Some("RichMessageWorker")

  case class MessageInfo(usersIds: Set[Int], randomId: Long, toPeer: models.Peer)

  def startWorker(config: RichMessageConfig, mediator: ActorRef)(
    implicit
    system:              ActorSystem,
    db:                  Database,
    seqUpdManagerRegion: SeqUpdatesManagerRegion,
    flowMaterializer:    FlowMaterializer,
    uploadManager:       UploadManager
  ): ActorRef = system.actorOf(Props(
    classOf[RichMessageWorker],
    config, mediator, db, seqUpdManagerRegion, flowMaterializer, uploadManager
  ))
}

class RichMessageWorker(config: RichMessageConfig, mediator: ActorRef)(
  implicit
  db:                  Database,
  seqUpdManagerRegion: SeqUpdatesManagerRegion,
  flowMaterializer:    FlowMaterializer,
  uploadManager:       UploadManager
) extends Actor with ActorLogging {

  import PreviewMaker._
  import RichMessageWorker._
  import SeqUpdatesManager._

  implicit val system: ActorSystem = context.system
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  val previewMaker = PreviewMaker(config, "previewMaker")

  import DistributedPubSubMediator.Subscribe
  mediator ! Subscribe(privateMessagesTopic, groupId, self)
  mediator ! Subscribe(groupMessagesTopic, groupId, self)

  def receive: Receive = {
    var privateAck = false
    var groupAck = false
    import DistributedPubSubMediator.{ Subscribe, SubscribeAck }

    {
      case SubscribeAck(Subscribe(`privateMessagesTopic`, `groupId`, `self`)) ⇒
        if (groupAck) context.become(ready) else privateAck = true
      case SubscribeAck(Subscribe(`groupMessagesTopic`, `groupId`, `self`)) ⇒
        if (privateAck) context.become(ready) else groupAck = true
    }
  }

  def ready: Receive = {
    case Events.PeerMessage(fromPeer, toPeer, randomId, _, message) ⇒
      message match {
        case TextMessage(text, _, _) ⇒
          val action =
            toPeer.typ match {
              case PeerType.Group   ⇒ persist.GroupUser.findUserIds(toPeer.id)
              case PeerType.Private ⇒ DBIO.successful(Seq(fromPeer.id, toPeer.id))
            }
          val result = for (ids ← db.run(action))
            yield GetPreview(text.trim, MessageInfo(ids.toSet, randomId, toPeer))
          result pipeTo previewMaker
        case _ ⇒
      }
    case PreviewSuccess(imageBytes, optFileName, mimeType, info) ⇒
      val fullName = optFileName getOrElse {
        val name = (new DateTime).toString("yyyyMMddHHmmss")
        val ext = Try(mimeType.split("/").last).getOrElse("tmp")
        s"$name.$ext"
      }
      db.run {
        for {
          (file, fileSize) ← DBIO.from(FileUtils.writeBytes(imageBytes))
          location ← DBIO.from(uploadManager.uploadFile(fullName, file.toFile))
          image ← DBIO.from(AsyncImage(imageBytes.toArray))

          updated = DocumentMessage(
            fileId = location.fileId,
            accessHash = location.accessHash,
            fileSize = fileSize.toInt,
            name = fullName,
            mimeType = mimeType,
            thumb = None, //TODO: 90*90 jpeg 0.6
            ext = Some(DocumentExPhoto(image.width, image.height))
          )
          _ ← persist.HistoryMessage.updateContentAll(info.usersIds, info.randomId, info.toPeer, updated.header, updated.toByteArray)
          _ ← broadcastUsersUpdate(info.usersIds, getUpdate(info, updated), None)
        } yield ()
      }
    case PreviewFailure(_) ⇒
  }

  private def getUpdate(info: MessageInfo, updated: Message) =
    UpdateMessageContentChanged(info.toPeer.asStruct, info.randomId, updated)

}
