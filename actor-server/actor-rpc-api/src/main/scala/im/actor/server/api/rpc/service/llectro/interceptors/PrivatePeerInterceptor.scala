package im.actor.server.api.rpc.service.llectro.interceptors

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.util.{ Failure, Success }

import akka.actor._
import akka.contrib.pattern.DistributedPubSubMediator
import akka.pattern.pipe
import play.api.libs.json.Json
import shapeless._, syntax.std.tuple._
import slick.driver.PostgresDriver.api._

import im.actor.api.PeersImplicits
import im.actor.api.rpc.Update
import im.actor.api.rpc.files.FileLocation
import im.actor.api.rpc.messaging.{ JsonMessage, UpdateMessage, UpdateMessageContentChanged, UpdateMessageDateChanged }
import im.actor.api.rpc.peers.Peer
import im.actor.api.rpc.peers.PeerType.Private
import im.actor.server.api.rpc.service.llectro.{ LlectroAds, LlectroInterceptionConfig, Message, MessageFormats }
import im.actor.server.api.rpc.service.messaging.{ Events, MessagingService }
import im.actor.server.llectro.results.Banner
import im.actor.server.models
import im.actor.server.push.{ SeqUpdatesManager, SeqUpdatesManagerRegion }

object PrivatePeerInterceptor {

  /**
   * Message that stores optional `randomId` of an ad message
   * it should be a collection of authId -> randomIdOpt pairs
   * @param randomId `randomId` of an ad message
   */
  case class PublishedAd(randomIds: Seq[(Long, Option[Long])])

  def props(
    llectroAds:         LlectroAds,
    adsUser:            models.llectro.LlectroUser,
    interceptionConfig: LlectroInterceptionConfig,
    mediator:           ActorRef
  )(
    implicit
    db:                  Database,
    seqUpdManagerRegion: SeqUpdatesManagerRegion
  ) =
    Props(classOf[PrivatePeerInterceptor], llectroAds, adsUser, interceptionConfig, mediator, db, seqUpdManagerRegion)
}

class PrivatePeerInterceptor(
  llectroAds:         LlectroAds,
  adsUser:            models.llectro.LlectroUser,
  interceptionConfig: LlectroInterceptionConfig,
  mediator:           ActorRef
)(
  implicit
  db:                  Database,
  seqUpdManagerRegion: SeqUpdatesManagerRegion
) extends PeerInterceptor with PeersImplicits {
  import DistributedPubSubMediator._

  import PeerInterceptor._
  import MessageFormats._
  import PrivatePeerInterceptor._

  val MessagesBetweenAds = interceptionConfig.messagesBetweenAds

  private[this] var countdown: Int = MessagesBetweenAds
  private[this] var adRandomIds = Map.empty[Long, Long]

  private[this] val scheduledResubscribe = system.scheduler.schedule(Duration.Zero, 5.minutes) { self ! Resubscribe }

  def receive = {
    case Resubscribe ⇒
      val peer = Peer(Private, adsUser.userId)
      mediator ! Subscribe(MessagingService.messagesTopic(peer), Some(interceptorGroupId(peer)), self)
    case ack: SubscribeAck ⇒
      scheduledResubscribe.cancel()
    case Events.PeerMessage(fromPeer, toPeer, _, _, _) ⇒
      log.debug("New message, increasing counter")
      countdown -= 1
      if (countdown == 0) {
        val dialogPeer =
          if (toPeer.id == adsUser.userId) fromPeer else toPeer
        insertAds(dialogPeer.asStruct)
      }
    case PublishedAd(ids) ⇒
      ids foreach {
        case (authId, randomIdOpt) ⇒
          randomIdOpt foreach { randomId ⇒
            adRandomIds = adRandomIds + (authId → randomId)
          }
      }

      countdown = MessagesBetweenAds
  }

  private def insertAds(dialogPeer: Peer): Future[PublishedAd] = {
    log.debug("Inserting ads for peer {}", dialogPeer)

    val randomIdFuture =
      db.run(for {
        devices ← getLlectroDevices(adsUser.userId)
        devicesBanners ← DBIO.sequence(devices map { device ⇒
          DBIO.from(llectroAds.getBanner(adsUser.uuid, device.screenWidth, device.screenHeight) map (device → _))
        })
        devicesBannersFiles ← DBIO.sequence(
          devicesBanners map (deviceBanner ⇒ DBIO.from(fetchBanner(llectroAds, deviceBanner._2) map (deviceBanner ++ _)))
        )
        ids ← DBIO.sequence(
          devicesBannersFiles map {
            case (device, banner, fileLocation, fileSize) ⇒
              val (randomIdOpt, updates) = getUpdates(dialogPeer, banner, device.authId, fileLocation, fileSize)
              DBIO.sequence(updates map (SeqUpdatesManager.persistAndPushUpdate(device.authId, _, None))) map (_ ⇒ device.authId → randomIdOpt)
          }
        )
      } yield PublishedAd(ids)) andThen {
        case Success(randomId) ⇒
          log.debug("Inserted an ad with randomId {}", randomId)
        case Failure(e) ⇒
          log.error(e, "Failed to insert ad")
      }
    randomIdFuture pipeTo self
  }

  private def getUpdates(dialogPeer: Peer, banner: Banner, authId: Long, fileLocation: FileLocation, fileSize: Long): (Option[Long], Seq[Update]) = {
    val message = JsonMessage(
      Json.stringify(Json.toJson(
        Message.banner(banner.advertUrl, fileLocation.fileId, fileLocation.accessHash, fileSize)
      ))
    )

    adRandomIds get (authId) match {
      case Some(randomId) ⇒
        None → Seq(
          UpdateMessageContentChanged(dialogPeer, randomId, message),
          UpdateMessageDateChanged(dialogPeer, randomId, System.currentTimeMillis())
        )
      case None ⇒
        val randomId = ThreadLocalRandom.current().nextLong()
        Some(randomId) → Seq(UpdateMessage(dialogPeer, adsUser.userId, System.currentTimeMillis(), randomId, message))
    }
  }

}