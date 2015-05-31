package im.actor.server.api.rpc.service.llectro

import scala.concurrent.{ Future, ExecutionContext }
import scala.concurrent.duration._

import akka.actor._
import akka.contrib.pattern.{ ClusterSingletonProxy, ClusterSingletonManager, DistributedPubSubExtension, DistributedPubSubMediator }
import akka.pattern.pipe
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc.messaging.MessagingService
import im.actor.api.rpc.peers.PeerType.{ Private, Group }
import im.actor.api.rpc.peers.{ Peer, PeerType }
import im.actor.server.api.rpc.service.messaging.MessagingService
import im.actor.server.ilectro.ILectro
import im.actor.server.persist
import im.actor.server.push.SeqUpdatesManagerRegion
import im.actor.server.util.{ UploadManager, UserUtils }
import im.actor.utils.http.DownloadManager

object MessageInterceptor {
  private case object FetchUserIds
  private case class SubscribeUsers(usersToGroups: Map[Int, Set[Int]])
  private[llectro] case class Resubscribe(peer: Peer)

  private def props(
    ilectro:            ILectro,
    downloadManager:    DownloadManager,
    uploadManager:      UploadManager,
    mediator:           ActorRef,
    interceptionConfig: ILectroInterceptionConfig
  )(implicit db: Database, seqUpdManagerRegion: SeqUpdatesManagerRegion): Props =
    Props(classOf[MessageInterceptor], ilectro, downloadManager, uploadManager, mediator, interceptionConfig, db, seqUpdManagerRegion)

  private val singletonName: String = "messagesInterceptor"

  def startSingleton(
    ilectro:            ILectro,
    downloadManager:    DownloadManager,
    uploadManager:      UploadManager,
    mediator:           ActorRef,
    interceptionConfig: ILectroInterceptionConfig
  )(
    implicit
    db:                  Database,
    system:              ActorSystem,
    seqUpdManagerRegion: SeqUpdatesManagerRegion
  ): ActorRef = {
    system.actorOf(
      ClusterSingletonManager.props(
        singletonProps = props(ilectro, downloadManager, uploadManager, mediator, interceptionConfig),
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
  ilectro:            ILectro,
  downloadManager:    DownloadManager,
  uploadManager:      UploadManager,
  mediator:           ActorRef,
  interceptionConfig: ILectroInterceptionConfig
)(implicit db: Database, seqUpdManagerRegion: SeqUpdatesManagerRegion) extends Actor with ActorLogging {
  import DistributedPubSubMediator._
  import MessageInterceptor._

  implicit val ec: ExecutionContext = context.dispatcher
  implicit val system: ActorSystem = context.system

  val scheduledFetch = context.system.scheduler.schedule(Duration.Zero, 1.minute) { reFetchUsers(self) }

  var subscribedUsersToGroups = Map.empty[Int, Set[Int]]

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
      fetchUsersAndGroups()
    case SubscribeUsers(userToGroups) ⇒

      val newUsersToGroups = userToGroups.keySet diff subscribedUsersToGroups.keySet
      newUsersToGroups foreach startInterceptingPrivate
      newUsersToGroups foreach { u ⇒ userToGroups.get(u) foreach (_ foreach (startInterceptingGroup(u, _))) }

      val oldUsersToGroups = userToGroups -- newUsersToGroups
      oldUsersToGroups foreach {
        case (userId, groups) ⇒
          val updatedGroups = subscribedUsersToGroups.get(userId).map(groups diff _) getOrElse Set()
          updatedGroups foreach (startInterceptingGroup(userId, _))
      }

      subscribedUsersToGroups ++= userToGroups
    case Resubscribe(peer) ⇒
      log.debug("Resubscribe {}", peer)
      mediator ! Subscribe(MessagingService.messagesTopic(peer), Some(interceptorGroupId(peer)), sender())
    case _ ⇒
  }

  private def fetchUsersAndGroups(): Unit = {
    log.debug("Fetching ilectro users")

    val fetch =
      db.run {
        for {
          userIds ← persist.ilectro.ILectroUser.findIds()
          result ← DBIO.sequence(userIds.map { userId ⇒
            for (gu ← persist.GroupUser.findByUserId(userId)) yield userId → gu.map(_.groupId).toSet
          })
        } yield {
          log.debug("Ilectro userIds are {}", userIds)
          SubscribeUsers(result.toMap)
        }
      }
    fetch pipeTo self
  }

  private def startInterceptingPrivate(userId: Int): Unit = {
    log.debug("Subscribing to {}", userId)
    startInterceptingPeer(userId, Peer(PeerType.Private, userId))
  }

  private def startInterceptingGroup(userId: Int, groupId: Int): Unit = {
    log.debug("Subscribing to userId's {} group {}", userId, groupId)
    startInterceptingPeer(userId, Peer(Group, groupId))
  }

  private def startInterceptingPeer(userId: Int, peer: Peer): Unit = {
    db.run {
      for {
        user ← UserUtils.getUserUnsafe(userId)
        ilectroUser ← persist.ilectro.ILectroUser.findByUserId(userId) map (_.getOrElse { throw new Exception(s"Failed to find ilectro user ${userId}") })
      } yield {
        val interceptor = context.actorOf(
          PeerInterceptor.props(
            ilectro,
            downloadManager,
            uploadManager,
            user,
            ilectroUser,
            interceptionConfig
          ),
          interceptorGroupId(peer)
        )
        val topic = MessagingService.messagesTopic(peer)
        mediator ! Subscribe(topic, Some(interceptorGroupId(peer)), interceptor)
      }
    } onFailure {
      case e ⇒
        // FIXME: resubscribe
        log.error(e, s"Failed to subscribe user's ${userId} peer ${peer}")
    }
  }

  private def interceptorGroupId(peer: Peer): String = {
    peer match {
      case Peer(Group, id)   ⇒ s"group-$id"
      case Peer(Private, id) ⇒ s"private-$id"
    }
  }

}
