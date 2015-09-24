package im.actor.server.notifications

import akka.actor._
import akka.contrib.pattern.ClusterSingletonManager
import akka.event.Logging
import akka.http.scaladsl.{ Http, HttpExt }
import akka.stream.Materializer
import akka.util.Timeout
import com.typesafe.config.Config
import im.actor.server.db.DbExtension
import im.actor.server.group.{ GroupExtension, GroupViewRegion }
import im.actor.server.sms.{ ClickatellSmsEngine, SmsEngine }
import im.actor.util.log.AnyRefLogSource
import slick.driver.PostgresDriver.api._

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

object NotificationsSender {

  private[notifications] case object Notify

  private val singletonName: String = "notificationsSender"

  def startSingleton(notificationsConfig: NotificationsConfig, smsConfig: Config)(
    implicit
    system:       ActorSystem,
    materializer: Materializer
  ): ActorRef = {
    implicit val http: HttpExt = Http()
    implicit val engine: ClickatellSmsEngine = new ClickatellSmsEngine(smsConfig.getConfig("clickatell"))

    system.actorOf(
      ClusterSingletonManager.props(
        singletonProps = props(notificationsConfig, engine),
        singletonName = singletonName,
        terminationMessage = PoisonPill,
        role = None
      ),
      name = s"${singletonName}Manager"
    )
  }

  private def props(implicit config: NotificationsConfig, engine: SmsEngine) =
    Props(classOf[NotificationsSender], config, engine)
}

class NotificationsSender(implicit config: NotificationsConfig, engine: SmsEngine) extends Actor with ActorLogging {

  import AnyRefLogSource._
  import NotificationsSender._

  implicit val system: ActorSystem = context.system
  implicit val ec: ExecutionContextExecutor = system.dispatcher
  implicit val db: Database = DbExtension(system).db
  implicit val watcherConfig: UnreadWatcherConfig = config.watcherConfig
  implicit val groupViewRegion: GroupViewRegion = GroupExtension(context.system).viewRegion
  implicit val timeout = Timeout(5.seconds)

  override val log = Logging(system, this)

  private val unreadWatcher = new UnreadWatcher()
  private val notifier = new PhoneNotifier(engine)

  val scheduledSend = system.scheduler.schedule(config.initialDelay, config.interval, self, Notify)

  override def postStop(): Unit = {
    super.postStop()
    scheduledSend.cancel()
  }

  def receive: Receive = {
    case Notify ⇒
      log.debug("Finding users to notify about unread messages")
      unreadWatcher.getNotifications.map { tasks ⇒
        tasks.foreach { task ⇒
          log.debug("processing task: messages to userId {} from users {}", task.userId, task.data)
          notifier.processTask(task)
        }
      }
    case _ ⇒
  }

}
