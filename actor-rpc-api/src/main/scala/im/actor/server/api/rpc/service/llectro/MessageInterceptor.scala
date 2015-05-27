package im.actor.server.api.rpc.service.llectro

import scala.concurrent.{ Future, ExecutionContext }
import scala.concurrent.duration._

import akka.actor._
import akka.contrib.pattern.{ ClusterSingletonProxy, ClusterSingletonManager, DistributedPubSubExtension, DistributedPubSubMediator }
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

  private val singletonName: String = "messagesInterceptor"

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
        singletonName = singletonName,
        terminationMessage = PoisonPill,
        role = None
      ),
      name = s"${singletonName}Manager"
    )
  }

  def startSingletonProxy()(implicit system: ActorSystem): ActorRef = {
    system.actorOf(
      ClusterSingletonProxy.props(
        singletonPath = s"/user/${singletonName}Manager/${singletonName}",
        role = None
      ),
      name = s"${singletonName}Proxy"
    )
  }

  def reFetchUsers(singleton: ActorRef)(implicit system: ActorSystem) = singleton ! FetchUserIds
}

class MessageInterceptor(
  ilectro:         ILectro,
  downloadManager: DownloadManager,
  uploadManager:   UploadManager
)(implicit db: Database, seqUpdManagerRegion: SeqUpdatesManagerRegion) extends Actor with ActorLogging {
  import DistributedPubSubMediator._
  import MessageInterceptor._

  implicit val ec: ExecutionContext = context.dispatcher
  implicit val system: ActorSystem = context.system

  val mediator = DistributedPubSubExtension(context.system).mediator
  val scheduledFetch = context.system.scheduler.schedule(Duration.Zero, 1.minute) { reFetchUsers(self) }

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
      mediator ! Subscribe(MessagingService.messagesTopic(peer), interceptorGroupId(peer), sender())
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

        val peer = Peer(PeerType.Private, userId)
        val topic = MessagingService.messagesTopic(peer)
        mediator ! Subscribe(topic, interceptorGroupId(peer), interceptor)
      }
    } onFailure {
      case e ⇒
        // FIXME: resubscribe
        log.error(e, s"Failed to subscribe user ${userId}")
    }
  }

  private def interceptorGroupId(peer: Peer): Option[String] = {
    Some(peer.`type` match {
      case PeerType.Group   ⇒ s"group-${peer.id}"
      case PeerType.Private ⇒ s"private-${peer.id}"
    })
  }

}
