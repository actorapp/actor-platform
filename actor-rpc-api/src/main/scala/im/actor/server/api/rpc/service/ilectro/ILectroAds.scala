package im.actor.server.api.rpc.service.ilectro

import java.math.BigInteger
import java.nio.file.Path
import java.security.MessageDigest
import java.util.UUID

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

import akka.actor.ActorSystem
import akka.event.Logging

import im.actor.api.rpc.files.FileLocation
import im.actor.server.ilectro.ILectro
import im.actor.server.ilectro.results.Banner
import im.actor.server.util.{ AnyRefLogSource, UploadManager }
import im.actor.utils.http.DownloadManager

class ILectroAds(ilectro: ILectro, downloadManager: DownloadManager, uploadManager: UploadManager)(implicit system: ActorSystem) {

  import AnyRefLogSource._

  val log = Logging(system, this)

  implicit val ec: ExecutionContext = system.dispatcher

  private[ilectro] def getBanner(userUuid: UUID): Future[Banner] = {
    ilectro.getBanners(userUuid) andThen {
      case Success(banner) ⇒
        log.debug("Loaded banner from API for user {}: {}", userUuid, banner)
      case Failure(e) ⇒
        log.error(e, s"Failed to load banner for user ${userUuid}")
    }
  }

  private[ilectro] def downloadBanner(banner: Banner): Future[(Path, Long)] = {
    downloadManager.download(banner.imageUrl) andThen {
      case Success(_) ⇒
        log.debug("Downloaded banner {}", banner)
      case Failure(e) ⇒
        log.error(e, s"Failed to download banner ${banner}")
    }
  }

  private[ilectro] def uploadBannerInternally(banner: Banner, path: Path, internalFileName: String): Future[FileLocation] = {
    uploadManager.uploadFile(internalFileName, path.toFile) andThen {
      case Success(location) ⇒
        log.debug("Uploaded banner internally {} {}", banner, location)
      case Failure(e) ⇒
        log.error(e, s"Failed to upload banner internally ${banner}")
    }
  }

  private[ilectro] def genBannerFileName(banner: Banner): String = {
    val md = MessageDigest.getInstance("MD5")
    val digestBytes = md.digest((banner.advertUrl ++ banner.imageUrl).getBytes)
    (new BigInteger(digestBytes) toString 16) + ".jpg"
  }

}
