package im.actor.server.api.rpc.service.llectro

import java.math.BigInteger
import java.nio.file.Path
import java.security.MessageDigest
import java.util.UUID

import scala.concurrent.duration._
import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

import akka.actor.{ Actor, ActorLogging, ActorSystem, Props }
import akka.contrib.pattern.DistributedPubSubMediator
import play.api.libs.json.Json
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc.files.FileLocation
import im.actor.api.rpc.messaging.{ JsonMessage, UpdateMessage }
import im.actor.api.rpc.peers.{ Peer, PeerType }
import im.actor.server.api.rpc.service.messaging.Events
import im.actor.server.ilectro.ILectro
import im.actor.server.ilectro.results.Banner
import im.actor.server.push.{ SeqUpdatesManager, SeqUpdatesManagerRegion }
import im.actor.server.util.{ UploadManager, UserUtils }
import im.actor.server.{ models, persist }
import im.actor.utils.http.DownloadManager

object PrivatePeerInterceptor {
  private case object ResetCountdown

  def props(
    ilectro:         ILectro,
    downloadManager: DownloadManager,
    uploadManager:   UploadManager,
    user:            models.User,
    ilectroUser:     models.ilectro.ILectroUser
  )(
    implicit
    db:                  Database,
    seqUpdManagerRegion: SeqUpdatesManagerRegion
  ) =
    Props(classOf[PrivatePeerInterceptor], ilectro, downloadManager, uploadManager, user, ilectroUser, db, seqUpdManagerRegion)
}

class PrivatePeerInterceptor(
  ilectro:         ILectro,
  downloadManager: DownloadManager,
  uploadManager:   UploadManager,
  user:            models.User,
  ilectroUser:     models.ilectro.ILectroUser
)(
  implicit
  db:                  Database,
  seqUpdManagerRegion: SeqUpdatesManagerRegion
) extends Actor with ActorLogging {
  import DistributedPubSubMediator._

  import MessageFormats._
  import PrivatePeerInterceptor._

  implicit val ec: ExecutionContext = context.dispatcher
  implicit val system: ActorSystem = context.system

  val MessagesBetweenAds = 10

  var countdown: Int = MessagesBetweenAds
  var adRandomId: Option[Long] = None

  val scheduledResubscribe =
    context.system.scheduler.scheduleOnce(
      30.seconds, context.parent, MessageInterceptor.Resubscribe(Peer(PeerType.Private, user.id))
    )

  def receive = {
    case ack: SubscribeAck ⇒
      scheduledResubscribe.cancel()
    case ResetCountdown ⇒
      countdown = MessagesBetweenAds
    case Events.PeerMessage(fromPeer, toPeer, randomId, message) ⇒
      log.debug("New message, increasing counter")
      countdown -= 1
      if (countdown == 0) {
        val dialogPeer =
          if (toPeer.id == user.id)
            fromPeer
          else
            toPeer

        insertAds(dialogPeer) andThen {
          case _ ⇒ self ! ResetCountdown
        }
      }
  }

  private def insertAds(dialogPeer: Peer): Future[Long] = {
    log.debug("Inserting ads for peer {}", dialogPeer)

    (for {
      banner ← getBanner(ilectroUser.uuid)
      (filePath, fileSize) ← downloadBanner(banner)

      fileLocation ← uploadBannerInternally(banner, filePath, genBannerFileName(banner))

      rng = ThreadLocalRandom.current()
      randomId = rng.nextLong()
      message = JsonMessage(
        Json.stringify(Json.toJson(
          Message.banner(banner.advertUrl, fileLocation.fileId, fileLocation.accessHash, 234, 60)
        ))
      )

      update = UpdateMessage(dialogPeer, user.id, System.currentTimeMillis(), randomId, message)

      _ ← db.run(SeqUpdatesManager.broadcastUserUpdate(user.id, update, None))
    } yield randomId) andThen {
      case Success(randomId) ⇒
        log.debug("Inserted an ad with randomId {}", randomId)
      case Failure(e) ⇒
        log.error(e, "Failed to insert ad")
    }
  }

  private def getBanner(userUuid: UUID): Future[Banner] = {
    ilectro.getBanners(userUuid) andThen {
      case Success(banner) ⇒
        log.debug("Loaded banner from API for user {}: {}", user.id, banner)
      case Failure(e) ⇒
        log.error(e, s"Failed to load banner for user ${user.id}")
    }
  }

  private def downloadBanner(banner: Banner): Future[(Path, Long)] = {
    downloadManager.download(banner.imageUrl) andThen {
      case Success(_) ⇒
        log.debug("Downloaded banner {}", banner)
      case Failure(e) ⇒
        log.error(e, s"Failed to download banner ${banner}")
    }
  }

  private def uploadBannerInternally(banner: Banner, path: Path, internalFileName: String): Future[FileLocation] = {
    uploadManager.uploadFile(internalFileName, path.toFile) andThen {
      case Success(location) ⇒
        log.debug("Uploaded banner internally {} {}", banner, location)
      case Failure(e) ⇒
        log.error(e, s"Failed to upload banner internally ${banner}")
    }
  }

  private def genBannerFileName(banner: Banner): String = {
    val md = MessageDigest.getInstance("MD5")
    val digestBytes = md.digest((banner.advertUrl ++ banner.imageUrl).getBytes)
    new BigInteger(digestBytes) toString (16)
  }
}