package im.actor.server.api.rpc.service.ilectro

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

import akka.actor._
import akka.contrib.pattern.{ ClusterSingletonManager, ClusterSingletonProxy }
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc.peers.Peer
import im.actor.api.rpc.peers.PeerType.{ Group, Private }
import im.actor.server.ilectro.ILectro
import im.actor.server.persist
import im.actor.server.push.SeqUpdatesManagerRegion
import im.actor.server.util.UploadManager
import im.actor.utils.http.DownloadManager

object MessageInterceptor {

  private case object FetchUsers

  private case object FetchGroups

  private case class SubscribeUsers(users: Set[Int])
  private case class SubscribeGroups(groups: Set[Int])

  private def props(
    iLectroAds:         ILectroAds,
    mediator:           ActorRef,
    interceptionConfig: ILectroInterceptionConfig
  )(implicit db: Database, seqUpdManagerRegion: SeqUpdatesManagerRegion): Props =
    Props(classOf[MessageInterceptor], iLectroAds, mediator, interceptionConfig, db, seqUpdManagerRegion)

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
    val ilectroAds = new ILectroAds(ilectro, downloadManager, uploadManager)
    system.actorOf(
      ClusterSingletonManager.props(
        singletonProps = props(ilectroAds, mediator, interceptionConfig),
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
  iLectroAds:         ILectroAds,
  mediator:           ActorRef,
  interceptionConfig: ILectroInterceptionConfig
)(implicit db: Database, seqUpdManagerRegion: SeqUpdatesManagerRegion) extends Actor with ActorLogging {

  import InterceptorsCommon.interceptorGroupId
  import MessageInterceptor._

  implicit val ec: ExecutionContext = context.dispatcher
  implicit val system: ActorSystem = context.system

  val scheduledFetch = context.system.scheduler.schedule(Duration.Zero, 1.minute) { reFetch(self) }

  var users = Set.empty[Int]
  var groups = Set.empty[Int]

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
    case _ ⇒
  }

  private def fetchUsers(): Unit = {
    log.debug("Fetching ilectro users")
    for (userIds ← db.run(persist.ilectro.ILectroUser.findIds())) yield {
      log.debug("Ilectro userIds are {}", userIds)
      self ! SubscribeUsers(userIds.toSet)
    }
  }

  private def fetchGroups(): Unit = {
    log.debug("Fetching ilectro groups")
    db.run {
      for {
        userIds ← persist.ilectro.ILectroUser.findIds()
        groupIds ← DBIO.sequence(userIds.map { persist.GroupUser.findByUserId(_).map(e ⇒ e.map(_.groupId)) })
        result = groupIds.flatten
      } yield {
        log.debug("Ilectro groupIds are {}", result)
        self ! SubscribeGroups(result.toSet)
      }
    }
  }

  private def createPrivateInterceptor(userId: Int): Unit = {
    log.debug("Subscribing to {}", userId)

    db.run {
      for {
        ilectroUser ← persist.ilectro.ILectroUser.findByUserId(userId) map (_.getOrElse {
          throw new Exception(s"Failed to find ilectro user ${userId}")
        })
      } yield {
        val interceptor = context.actorOf(
          PrivatePeerInterceptor.props(
            iLectroAds,
            ilectroUser,
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
        iLectroAds,
        groupId,
        interceptionConfig,
        mediator
      ),
      interceptorGroupId(Peer(Group, groupId))
    )
  }
}
