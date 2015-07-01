package im.actor.server.api.rpc.service.llectro

import java.math.BigInteger
import java.nio.file.Path
import java.security.MessageDigest
import java.util.UUID

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

import akka.actor.ActorSystem
import akka.event.Logging

import im.actor.api.rpc.files.FileLocation
import im.actor.server.llectro.Llectro
import im.actor.server.llectro.results.Banner
import im.actor.server.util.{ FileStorageAdapter, AnyRefLogSource }
import im.actor.utils.http.DownloadManager

class LlectroAds(llectro: Llectro, downloadManager: DownloadManager, fsAdapter: FileStorageAdapter)(implicit system: ActorSystem) {

  import AnyRefLogSource._

  val log = Logging(system, this)

  implicit val ec: ExecutionContext = system.dispatcher

  private[llectro] def getBanner(userUuid: UUID, screenWidth: Int, screenHeight: Int): Future[Banner] = {
    llectro.getBanners(userUuid, screenWidth, screenHeight) andThen {
      case Success(banner) ⇒
        log.debug("Loaded banner from API for user {}: {}", userUuid, banner)
      case Failure(e) ⇒
        log.error(e, s"Failed to load banner for user ${userUuid}")
    }
  }

  private[llectro] def downloadBanner(banner: Banner): Future[(Path, Long)] = {
    downloadManager.download(banner.imageUrl) andThen {
      case Success(_) ⇒
        log.debug("Downloaded banner {}", banner)
      case Failure(e) ⇒
        log.error(e, s"Failed to download banner ${banner}")
    }
  }

  private[llectro] def uploadBannerInternally(banner: Banner, path: Path, internalFileName: String): Future[FileLocation] = {
    fsAdapter.uploadFileF(internalFileName, path.toFile) andThen {
      case Success(location) ⇒
        log.debug("Uploaded banner internally {} {}", banner, location)
      case Failure(e) ⇒
        log.error(e, s"Failed to upload banner internally ${banner}")
    }
  }

  private[llectro] def genBannerFileName(banner: Banner): String = {
    val md = MessageDigest.getInstance("MD5")
    val digestBytes = md.digest((banner.advertUrl ++ banner.imageUrl).getBytes)
    (new BigInteger(digestBytes) toString 16) + ".jpg"
  }

}
