package im.actor.server.api.rpc.service.ilectro

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.util.{ Failure, Success }

import akka.actor._
import akka.contrib.pattern.DistributedPubSubMediator
import play.api.libs.json.Json
import slick.driver.PostgresDriver.api._

import im.actor.api.PeersImplicits
import im.actor.api.rpc.Update
import im.actor.api.rpc.files.FileLocation
import im.actor.api.rpc.messaging.{ JsonMessage, UpdateMessage, UpdateMessageContentChanged, UpdateMessageDateChanged }
import im.actor.api.rpc.peers.Peer
import im.actor.api.rpc.peers.PeerType.Private
import im.actor.server.api.rpc.service.messaging.{ Events, MessagingService }
import im.actor.server.ilectro.results.Banner
import im.actor.server.models
import im.actor.server.push.{ SeqUpdatesManager, SeqUpdatesManagerRegion }

object PrivatePeerInterceptor {
  def props(
    ilectroAds:         ILectroAds,
    adsUser:            models.ilectro.ILectroUser,
    interceptionConfig: ILectroInterceptionConfig,
    mediator:           ActorRef
  )(
    implicit
    db:                  Database,
    seqUpdManagerRegion: SeqUpdatesManagerRegion
  ) =
    Props(classOf[PrivatePeerInterceptor], ilectroAds, adsUser, interceptionConfig, mediator, db, seqUpdManagerRegion)
}

class PrivatePeerInterceptor(
  ilectroAds:         ILectroAds,
  adsUser:            models.ilectro.ILectroUser,
  interceptionConfig: ILectroInterceptionConfig,
  mediator:           ActorRef
)(
  implicit
  db:                  Database,
  seqUpdManagerRegion: SeqUpdatesManagerRegion
) extends Actor with ActorLogging with PeersImplicits {
  import DistributedPubSubMediator._
  import context._

  import InterceptorsCommon._
  import MessageFormats._

  val MessagesBetweenAds = interceptionConfig.messagesBetweenAds

  var countdown: Int = MessagesBetweenAds
  var adRandomId: Option[Long] = None

  self ! Resubscribe
  val scheduledResubscribe = system.scheduler.schedule(1.minute, 5.minutes) { self ! Resubscribe }

  def receive = {
    case Resubscribe ⇒
      val peer = Peer(Private, adsUser.userId)
      mediator ! Subscribe(MessagingService.messagesTopic(peer), Some(interceptorGroupId(peer)), self)
    case ack: SubscribeAck ⇒
      scheduledResubscribe.cancel()
    case ResetCountdown ⇒
      countdown = MessagesBetweenAds
    case Events.PeerMessage(fromPeer, toPeer, _, _, _) ⇒
      log.debug("New message, increasing counter")
      countdown -= 1
      if (countdown == 0) {
        val dialogPeer =
          if (toPeer.id == adsUser.userId) fromPeer else toPeer
        insertAds(dialogPeer.asStruct) andThen {
          case _ ⇒ self ! ResetCountdown
        }
      }
  }

  private def insertAds(dialogPeer: Peer): Future[Option[Long]] = {
    log.debug("Inserting ads for peer {}", dialogPeer)

    (for {
      banner ← ilectroAds.getBanner(adsUser.uuid)
      (filePath, fileSize) ← ilectroAds.downloadBanner(banner)
      fileLocation ← ilectroAds.uploadBannerInternally(banner, filePath, ilectroAds.genBannerFileName(banner))

      updates = getUpdates(dialogPeer, banner, fileLocation, fileSize)

      _ ← db.run(DBIO.sequence(
        updates map (SeqUpdatesManager.broadcastUserUpdate(adsUser.userId, _, None))
      ))
    } yield adRandomId) andThen {
      case Success(randomId) ⇒
        log.debug("Inserted an ad with randomId {}", randomId)
      case Failure(e) ⇒
        log.error(e, "Failed to insert ad")
    }
  }

  private def getUpdates(dialogPeer: Peer, banner: Banner, fileLocation: FileLocation, fileSize: Long): Seq[Update] = {
    val message = JsonMessage(
      Json.stringify(Json.toJson(
        Message.banner(banner.advertUrl, fileLocation.fileId, fileLocation.accessHash, fileSize, 234, 60)
      ))
    )

    adRandomId match {
      case Some(randomId) ⇒
        Seq(
          UpdateMessageDateChanged(dialogPeer, randomId, System.currentTimeMillis()),
          UpdateMessageContentChanged(dialogPeer, randomId, message)
        )
      case None ⇒
        val randomId = ThreadLocalRandom.current().nextLong()
        adRandomId = Some(randomId)

        Seq(UpdateMessage(dialogPeer, adsUser.userId, System.currentTimeMillis(), randomId, message))
    }
  }

}