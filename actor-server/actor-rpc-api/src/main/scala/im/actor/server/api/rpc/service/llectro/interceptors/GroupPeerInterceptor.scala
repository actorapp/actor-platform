package im.actor.server.api.rpc.service.llectro.interceptors

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.forkjoin.ThreadLocalRandom

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
import im.actor.api.rpc.peers.PeerType.Group
import im.actor.server.api.rpc.service.llectro._
import im.actor.server.api.rpc.service.messaging.{ Events, MessagingService }
import im.actor.server.llectro.results.Banner
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
   * Message that stores mapping from (userId, authId)
   * to `randomId` of an ad message
   * @param usersAdIds Map from user ids to message `randomId`
   */
  case class PublishedAds(usersAdIds: Set[((Int, Long), Long)])

  def props(
    llectroAds:         LlectroAds,
    groupId:            Int,
    interceptionConfig: LlectroInterceptionConfig,
    mediator:           ActorRef
  )(
    implicit
    db:                  Database,
    seqUpdManagerRegion: SeqUpdatesManagerRegion
  ) =
    Props(classOf[GroupPeerInterceptor], llectroAds, groupId, interceptionConfig, mediator, db, seqUpdManagerRegion)
}

class GroupPeerInterceptor(
  llectroAds:         LlectroAds,
  groupId:            Int,
  interceptionConfig: LlectroInterceptionConfig,
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
  var usersAd = Map.empty[(Int, Long), Long]

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
          optLlectroUser ← persist.llectro.LlectroUser.findByUserId(userId)
          results ← optLlectroUser.map { llectroUser ⇒
            val updatesAction = for {
              devices ← getLlectroDevices(userId)
              devicesBanners ← DBIO.sequence(devices map { device ⇒
                DBIO.from(
                  llectroAds.getBanner(llectroUser.uuid, device.screenWidth, device.screenHeight) map (device → _)
                )
              })
              devicesBannersFiles ← DBIO.sequence(devicesBanners map (device ⇒ DBIO.from(fetchBanner(llectroAds, device._2) map (device ++ _))))
            } yield {
              devicesBannersFiles map {
                case (device, banner, fileLocation, fileSize) ⇒
                  (device.authId, getAdIdAndUpdates(groupPeer, userId, device.authId, banner, fileLocation, fileSize))
              }
            }

            for {
              allUpdates ← updatesAction
              results ← DBIO.sequence(
                allUpdates map {
                  case (authId, (randomIdOpt, updates)) ⇒
                    for {
                      _ ← DBIO.sequence(updates map (SeqUpdatesManager.persistAndPushUpdate(authId, _, None)))
                    } yield {
                      randomIdOpt map (userId → authId → _)
                    }
                }
              )
            } yield results
          } getOrElse DBIO.successful(Seq.empty)
        } yield results
      } map { results ⇒
        results foreach { rOpt ⇒
          rOpt foreach { r ⇒
            log.debug("Inserted ad with randomId {} in group {} for user {} ", r._2, groupId, userId)
          }
        }

        results
      } recover {
        case e: Exception ⇒
          log.error(e, "Failed to insert ad")
          Set.empty
      }
    }
    (for (ids ← Future.sequence(usersAdIds)) yield {
      PublishedAds(ids.view.map(_.flatten).reduce(_ ++ _).toSet)
    }) pipeTo self
  }

  private def getAdIdAndUpdates(groupPeer: Peer, userId: Int, authId: Long, banner: Banner, fileLocation: FileLocation, fileSize: Long): (Option[Long], Seq[Update]) = {
    log.debug("Inserting ads for user {} in group {}", userId, groupPeer.id)
    val message = JsonMessage(
      Json.stringify(Json.toJson(
        Message.banner(banner.advertUrl, fileLocation.fileId, fileLocation.accessHash, fileSize)
      ))
    )

    usersAd get (userId → authId) match {
      case Some(randomId) ⇒
        None → Seq(
          UpdateMessageContentChanged(groupPeer, randomId, message),
          UpdateMessageDateChanged(groupPeer, randomId, System.currentTimeMillis())
        )
      case None ⇒
        val randomId = ThreadLocalRandom.current().nextLong()
        Some(randomId) → Seq(UpdateMessage(groupPeer, userId, System.currentTimeMillis(), randomId, message))
    }
  }

}