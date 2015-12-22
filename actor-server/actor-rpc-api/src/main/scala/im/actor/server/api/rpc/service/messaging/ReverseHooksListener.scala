package im.actor.server.api.rpc.service.messaging

import akka.actor._
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.singleton.{ ClusterSingletonManagerSettings, ClusterSingletonManager }
import akka.event.Logging
import im.actor.server.db.DbExtension
import im.actor.server.group.GroupExtension
import im.actor.server.persist
import im.actor.util.log.AnyRefLogSource

import scala.concurrent.duration._

object ReverseHooksListener {

  private[messaging] case object Resubscribe
  private[messaging] case object RefetchGroups
  private[messaging] case class SubscribeGroups(groupIds: Set[Int])

  private val singletonName: String = "reverseHooksListener"

  def startSingleton()(implicit system: ActorSystem): ActorRef =
    system.actorOf(
      ClusterSingletonManager.props(
        singletonProps = props,
        terminationMessage = PoisonPill,
        settings = ClusterSingletonManagerSettings(system)
      ),
      name = s"${singletonName}Manager"
    )

  def props: Props =
    Props(classOf[ReverseHooksListener])
}

private[messaging] final class ReverseHooksListener extends Actor with ActorLogging with AnyRefLogSource {

  import ReverseHooksListener._
  import ReverseHooksWorker._

  private implicit val system: ActorSystem = context.system
  import system.dispatcher

  private val scheduledFetch = context.system.scheduler.schedule(Duration.Zero, 1.minute, self, RefetchGroups)
  private val db = DbExtension(system).db

  private[this] var groups = Set.empty[Int]

  override val log = Logging(system, this)

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    super.preRestart(reason, message)
    log.error(reason, "ReverseHooksListener crashed")
  }

  override def postStop(): Unit = {
    super.postStop()
    scheduledFetch.cancel()
  }

  override def receive = {
    case RefetchGroups ⇒ fetchGroups()
    case SubscribeGroups(groupIds) ⇒
      val newGroups = groupIds diff groups
      newGroups foreach createGroupInterceptor
      groups ++= newGroups
  }

  private def fetchGroups(): Unit = {
    log.debug("Fetching groups to subscribe to reverse hooks")

    for (groupIds ← db.run(persist.GroupRepo.findAllIds)) yield {
      log.debug("Group ids to subscribe to reverse hooks {}", groupIds)
      self ! SubscribeGroups(groupIds.toSet)
    }
  }

  private def createGroupInterceptor(groupId: Int): Unit = {
    log.debug("Creating interceptor for group {}", groupId)
    for {
      optToken ← GroupExtension(system).getIntegrationToken(groupId)
    } yield {
      optToken.map { token ⇒
        context.actorOf(
          ReverseHooksWorker.props(groupId, token),
          interceptorGroupId(groupId)
        )
        ()
      } getOrElse {
        log.warning("Failed to create interceptor for group {}", groupId)
      }
    }
  }
}