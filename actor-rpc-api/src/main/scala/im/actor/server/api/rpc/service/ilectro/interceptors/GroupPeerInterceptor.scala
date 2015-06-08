package im.actor.server.api.rpc.service.ilectro.interceptors

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.forkjoin.ThreadLocalRandom

import akka.actor._
import akka.contrib.pattern.DistributedPubSubMediator
import akka.pattern.pipe
import play.api.libs.json.Json
import slick.driver.PostgresDriver.api._

import im.actor.api.PeersImplicits
import im.actor.api.rpc.Update
import im.actor.api.rpc.files.FileLocation
import im.actor.api.rpc.messaging.{ JsonMessage, UpdateMessage, UpdateMessageContentChanged, UpdateMessageDateChanged }
import im.actor.api.rpc.peers.Peer
import im.actor.api.rpc.peers.PeerType.Group
import im.actor.server.api.rpc.service.ilectro._
import im.actor.server.api.rpc.service.messaging.{ Events, MessagingService }
import im.actor.server.ilectro.results.Banner
import im.actor.server.persist
import im.actor.server.push.{ SeqUpdatesManager, SeqUpdatesManagerRegion }

object GroupPeerInterceptor {

  case object Refetch

  /**
   * Message that stores ids of users of given group
   * @param userIds Set of user ids
   */
  case class RegisterUsers(userIds: Set[Int])

  /**
   * Message that stores mapping from users ids
   * to `randomId` of an ad message
   * @param usersAdIds Map from user ids to message `randomId`
   */
  case class PublishedAds(usersAdIds: Set[(Int, Long)])

  def props(
    ilectroAds:         ILectroAds,
    groupId:            Int,
    interceptionConfig: ILectroInterceptionConfig,
    mediator:           ActorRef
  )(
    implicit
    db:                  Database,
    seqUpdManagerRegion: SeqUpdatesManagerRegion
  ) =
    Props(classOf[GroupPeerInterceptor], ilectroAds, groupId, interceptionConfig, mediator, db, seqUpdManagerRegion)
}

class GroupPeerInterceptor(
  ilectroAds:         ILectroAds,
  groupId:            Int,
  interceptionConfig: ILectroInterceptionConfig,
  mediator:           ActorRef
)(
  implicit
  db:                  Database,
  seqUpdManagerRegion: SeqUpdatesManagerRegion
) extends PeerInterceptor with PeersImplicits {

  import DistributedPubSubMediator._

  import GroupPeerInterceptor._
  import PeerInterceptor._
  import MessageFormats._

  val MessagesBetweenAds = interceptionConfig.messagesBetweenAds

  var countdown: Int = MessagesBetweenAds

  val scheduledResubscribe = system.scheduler.schedule(Duration.Zero, 5.minutes) { self ! Resubscribe }
  val scheduledRefetch = system.scheduler.schedule(Duration.Zero, 5.minutes) { self ! Refetch }

  var users = Set.empty[Int]
  var usersAd = Map.empty[Int, Long]

  def receive = {
    case Resubscribe ⇒
      val peer = Peer(Group, groupId)
      mediator ! Subscribe(MessagingService.messagesTopic(peer), Some(interceptorGroupId(peer)), self)
    case ack: SubscribeAck ⇒
      scheduledResubscribe.cancel()
    case Refetch ⇒ refetchUsers()
    case RegisterUsers(userIds) ⇒
      users ++= (userIds diff users)
    case Events.PeerMessage(fromPeer, toPeer, randomId, _, _) ⇒
      log.debug("New message, increasing counter")
      countdown -= 1
      if (countdown == 0) insertAds(toPeer.asStruct)
    case PublishedAds(usersAdIds) ⇒
      usersAd ++= usersAdIds.toMap
      countdown = MessagesBetweenAds
  }

  private def refetchUsers() =
    db.run(persist.GroupUser.findUserIds(groupId)).map(users ⇒ RegisterUsers(users.toSet)) pipeTo self

  private def insertAds(groupPeer: Peer): Future[PublishedAds] = {
    val usersAdIds = users map { userId ⇒
      db.run {
        for {
          optIlectroUser ← persist.ilectro.ILectroUser.findByUserId(userId)
          result ← optIlectroUser.map { ilectroUser ⇒
            val updates = for {
              banner ← ilectroAds.getBanner(ilectroUser.uuid)
              (filePath, fileSize) ← ilectroAds.downloadBanner(banner)
              fileLocation ← ilectroAds.uploadBannerInternally(banner, filePath, ilectroAds.genBannerFileName(banner))
            } yield getAdIdAndUpdates(groupPeer, userId, banner, fileLocation, fileSize)
            for {
              upd ← DBIO.from(updates)
              (adRandomId, u) = upd
              _ ← DBIO.sequence(u map (SeqUpdatesManager.broadcastUserUpdate(ilectroUser.userId, _, None)))
            } yield adRandomId.map(userId → _)
          } getOrElse DBIO.successful(None)
        } yield result
      } map { result ⇒
        result foreach { r ⇒
          log.debug("Inserted ad with randomId {} in group {} for user {} ", r._2, groupId, userId)
        }
        result
      } recover {
        case e: Exception ⇒
          log.error(e, "Failed to insert ad")
          None
      }
    }
    (for (ids ← Future.sequence(usersAdIds)) yield PublishedAds(ids.flatten)) pipeTo self
  }

  private def getAdIdAndUpdates(groupPeer: Peer, userId: Int, banner: Banner, fileLocation: FileLocation, fileSize: Long): (Option[Long], Seq[Update]) = {
    log.debug("Inserting ads for user {} in group {}", userId, groupPeer.id)
    val message = JsonMessage(
      Json.stringify(Json.toJson(
        Message.banner(banner.advertUrl, fileLocation.fileId, fileLocation.accessHash, fileSize, 234, 60)
      ))
    )

    usersAd.get(userId) match {
      case Some(randomId) ⇒
        None → Seq(
          UpdateMessageDateChanged(groupPeer, randomId, System.currentTimeMillis()),
          UpdateMessageContentChanged(groupPeer, randomId, message)
        )
      case None ⇒
        val randomId = ThreadLocalRandom.current().nextLong()
        Some(randomId) → Seq(UpdateMessage(groupPeer, userId, System.currentTimeMillis(), randomId, message))
    }
  }

}