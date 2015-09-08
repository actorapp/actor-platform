package im.actor.server.api.rpc.service.messaging

import akka.actor._
import akka.contrib.pattern.ClusterSingletonManager
import akka.event.Logging
import akka.util.Timeout
import im.actor.server.db.DbExtension
import im.actor.server.group.{ GroupViewRegion, GroupExtension, GroupOffice }
import im.actor.server.persist
import im.actor.util.log.AnyRefLogSource

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object ReverseHooksListener {

  private[messaging] case object Resubscribe
  private[messaging] case object RefetchGroups
  private[messaging] case class SubscribeGroups(groupIds: Set[Int])

  private val singletonName: String = "reverseHooksListener"

  def startSingleton(mediator: ActorRef)(implicit system: ActorSystem): ActorRef =
    system.actorOf(
      ClusterSingletonManager.props(
        singletonProps = props(mediator),
        singletonName = singletonName,
        terminationMessage = PoisonPill,
        role = None
      ),
      name = s"${singletonName}Manager"
    )

  def props(mediator: ActorRef): Props =
    Props(classOf[ReverseHooksListener], mediator)
}

private[messaging] final class ReverseHooksListener(mediator: ActorRef) extends Actor with ActorLogging with AnyRefLogSource {

  import ReverseHooksListener._
  import ReverseHooksWorker._

  private[this] implicit val ec: ExecutionContext = context.dispatcher
  private[this] implicit val system: ActorSystem = context.system
  private[this] implicit val groupViewRegion: GroupViewRegion = GroupExtension(system).viewRegion
  private[this] implicit val timeout: Timeout = Timeout(5.seconds)

  private[this] val scheduledFetch = context.system.scheduler.schedule(Duration.Zero, 1.minute, self, RefetchGroups)
  private[this] val db = DbExtension(system).db

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

    for (groupIds ← db.run(persist.Group.allIds)) yield {
      log.debug("Group ids to subscribe to reverse hooks {}", groupIds)
      self ! SubscribeGroups(groupIds.toSet)
    }
  }

  private def createGroupInterceptor(groupId: Int): Unit = {
    log.debug("Creating interceptor for group {}", groupId)
    for {
      optToken ← GroupOffice.getIntegrationToken(groupId)
    } yield {
      optToken.map { token ⇒
        context.actorOf(
          ReverseHooksWorker.props(groupId, token, mediator),
          interceptorGroupId(groupId)
        )
        ()
      } getOrElse {
        log.warning("Failed to create interceptor for group {}", groupId)
      }
    }
  }
}