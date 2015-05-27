package im.actor.server.api.rpc.service.llectro

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

import akka.actor._
import akka.contrib.pattern.{ ClusterSingletonManager, DistributedPubSubExtension, DistributedPubSubMediator }
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc.messaging.MessagingService
import im.actor.api.rpc.peers.{ Peer, PeerType }
import im.actor.server.api.rpc.service.messaging.MessagingService
import im.actor.server.ilectro.ILectro
import im.actor.server.persist
import im.actor.server.push.SeqUpdatesManagerRegion
import im.actor.server.util.{ UploadManager, UserUtils }
import im.actor.utils.http.DownloadManager

object MessageInterceptor {
  private case object FetchUserIds
  private case class SubscribeUsers(ids: Seq[Int])
  private[llectro] case class Resubscribe(peer: Peer)

  private def props(
    ilectro:         ILectro,
    downloadManager: DownloadManager,
    uploadManager:   UploadManager
  )(implicit db: Database, seqUpdManagerRegion: SeqUpdatesManagerRegion): Props =
    Props(classOf[MessageInterceptor], ilectro, downloadManager, uploadManager, db, seqUpdManagerRegion)

  def startSingleton(
    ilectro:         ILectro,
    downloadManager: DownloadManager,
    uploadManager:   UploadManager
  )(
    implicit
    db:                  Database,
    system:              ActorSystem,
    seqUpdManagerRegion: SeqUpdatesManagerRegion
  ): ActorRef = {
    system.actorOf(
      ClusterSingletonManager.props(
        singletonProps = props(ilectro, downloadManager, uploadManager),
        singletonName = "messagesInterceptor",
        terminationMessage = PoisonPill,
        role = None
      ),
      name = "messagesInterceptorSingletonManager"
    )
  }

}

class MessageInterceptor(
  ilectro:         ILectro,
  downloadManager: DownloadManager,
  uploadManager:   UploadManager
)(implicit db: Database, seqUpdManagerRegion: SeqUpdatesManagerRegion) extends Actor with ActorLogging {
  import DistributedPubSubMediator._
  println("Starting interceptor")
  import MessageInterceptor._

  implicit val ec: ExecutionContext = context.dispatcher

  val mediator = DistributedPubSubExtension(context.system).mediator
  val scheduledFetch = context.system.scheduler.schedule(Duration.Zero, 1.minute, self, FetchUserIds)

  var subscribedUserIds = Set.empty[Int]

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    super.preRestart(reason, message)

    log.error(reason, "MessageInterceptor crashed")
  }

  override def postStop(): Unit = {
    super.postStop()
    scheduledFetch.cancel()
  }

  def receive = {
    case FetchUserIds ⇒
      fetchUserIds()
    case SubscribeUsers(ids) ⇒
      val newIds = ids.toSet.diff(subscribedUserIds)
      newIds foreach startIntercepting
      subscribedUserIds ++= newIds
    case Resubscribe(peer) ⇒
      log.debug("Resubscribe {}", peer)
      mediator ! Subscribe(MessagingService.messagesTopic(peer), PrivatePeerInterceptor.groupId, sender())
    case _ ⇒
  }

  private def fetchUserIds(): Unit = {
    log.debug("Fetching ilectro users")

    for (userIds ← db.run(persist.ilectro.ILectroUser.findIds)) yield {
      log.debug("Ilectro userIds are {}", userIds)
      self ! SubscribeUsers(userIds)
    }
  }

  private def startIntercepting(userId: Int): Unit = {
    log.debug("Subscribing to {}", userId)

    db.run {
      for {
        user ← UserUtils.getUserUnsafe(userId)
        ilectroUser ← persist.ilectro.ILectroUser.findByUserId(userId) map (_.getOrElse { throw new Exception(s"Failed to find ilectro user ${userId}") })
      } yield {
        val interceptor = context.actorOf(
          PrivatePeerInterceptor.props(
            ilectro,
            downloadManager,
            uploadManager,
            user,
            ilectroUser
          ),
          s"private-${userId}"
        )

        val topic = MessagingService.messagesTopic(Peer(PeerType.Private, userId))
        mediator ! Subscribe(topic, PrivatePeerInterceptor.groupId, interceptor)
      }
    } onFailure {
      case e ⇒
        // FIXME: resubscribe
        log.error(e, s"Failed to subscribe user ${userId}")
    }
  }
}
