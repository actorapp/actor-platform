package im.actor.server.api.rpc.service.llectro.interceptors

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

import akka.actor._
import akka.contrib.pattern.{ ClusterSingletonManager, ClusterSingletonProxy }
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc.peers.Peer
import im.actor.api.rpc.peers.PeerType.{ Group, Private }
import im.actor.server.api.rpc.service.llectro.{ LlectroAds, LlectroInterceptionConfig }
import im.actor.server.llectro.Llectro
import im.actor.server.persist
import im.actor.server.push.SeqUpdatesManagerRegion
import im.actor.server.util.FileStorageAdapter
import im.actor.utils.http.DownloadManager

object MessageInterceptor {

  private case object FetchUsers

  private case object FetchGroups

  private case class SubscribeUsers(users: Set[Int])
  private case class SubscribeGroups(groups: Set[Int])

  private def props(
    llectroAds:         LlectroAds,
    mediator:           ActorRef,
    interceptionConfig: LlectroInterceptionConfig
  )(implicit db: Database, seqUpdManagerRegion: SeqUpdatesManagerRegion): Props =
    Props(classOf[MessageInterceptor], llectroAds, mediator, interceptionConfig, db, seqUpdManagerRegion)

  private val singletonName: String = "messagesInterceptor"

  def startSingleton(
    llectro:            Llectro,
    downloadManager:    DownloadManager,
    mediator:           ActorRef,
    interceptionConfig: LlectroInterceptionConfig
  )(
    implicit
    db:                  Database,
    system:              ActorSystem,
    seqUpdManagerRegion: SeqUpdatesManagerRegion,
    fsAdapter:           FileStorageAdapter
  ): ActorRef = {
    val llectroAds = new LlectroAds(llectro, downloadManager, fsAdapter)
    system.actorOf(
      ClusterSingletonManager.props(
        singletonProps = props(llectroAds, mediator, interceptionConfig),
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

  def reFetch(singleton: ActorRef)(implicit system: ActorSystem) = {
    singleton ! FetchUsers
    singleton ! FetchGroups
  }
}

class MessageInterceptor(
  llectroAds:         LlectroAds,
  mediator:           ActorRef,
  interceptionConfig: LlectroInterceptionConfig
)(implicit db: Database, seqUpdManagerRegion: SeqUpdatesManagerRegion) extends Actor with ActorLogging {

  import PeerInterceptor._
  import MessageInterceptor._

  private[this] implicit val ec: ExecutionContext = context.dispatcher
  private[this] implicit val system: ActorSystem = context.system

  private[this] val scheduledFetch = context.system.scheduler.schedule(Duration.Zero, 1.minute) { reFetch(self) }

  private[this] var users = Set.empty[Int]
  private[this] var groups = Set.empty[Int]

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    super.preRestart(reason, message)

    log.error(reason, "MessageInterceptor crashed")
  }

  override def postStop(): Unit = {
    super.postStop()
    scheduledFetch.cancel()
  }

  def receive = {
    case FetchUsers  ⇒ fetchUsers()
    case FetchGroups ⇒ fetchGroups()
    case SubscribeUsers(userIds) ⇒
      val newUsers = userIds diff users
      newUsers foreach createPrivateInterceptor
      users ++= newUsers
    case SubscribeGroups(groupIds) ⇒
      val newGroups = groupIds diff groups
      newGroups foreach createGroupInterceptor
      groups ++= newGroups
  }

  private def fetchUsers(): Unit = {
    log.debug("Fetching llectro users")
    for (userIds ← db.run(persist.llectro.LlectroUser.findIds())) yield {
      log.debug("Llectro userIds are {}", userIds)
      self ! SubscribeUsers(userIds.toSet)
    }
  }

  private def fetchGroups(): Unit = {
    log.debug("Fetching groups for llectro")
    db.run {
      for (groupIds ← persist.Group.groups.map(_.id).result) yield {
        log.debug("GroupIds for Llectro are {}", groupIds)
        self ! SubscribeGroups(groupIds.toSet)
      }
    }
  }

  private def createPrivateInterceptor(userId: Int): Unit = {
    log.debug("Subscribing to {}", userId)

    db.run {
      for {
        llectroUser ← persist.llectro.LlectroUser.findByUserId(userId) map (_.getOrElse {
          throw new Exception(s"Failed to find llectro user ${userId}")
        })
      } yield {
        val interceptor = context.actorOf(
          PrivatePeerInterceptor.props(
            llectroAds,
            llectroUser,
            interceptionConfig,
            mediator
          ),
          interceptorGroupId(Peer(Private, userId))
        )
      }
    } onFailure {
      case e ⇒
        // FIXME: resubscribe
        log.error(e, s"Failed to subscribe user ${userId}")
    }
  }

  private def createGroupInterceptor(groupId: Int): Unit = {
    log.debug("Subscribing to group {}", groupId)

    val interceptor = context.actorOf(
      GroupPeerInterceptor.props(
        llectroAds,
        groupId,
        interceptionConfig,
        mediator
      ),
      interceptorGroupId(Peer(Group, groupId))
    )
  }
}
