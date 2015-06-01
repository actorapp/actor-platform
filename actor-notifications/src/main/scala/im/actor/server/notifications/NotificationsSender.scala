package im.actor.server.notifications

import scala.concurrent.ExecutionContextExecutor

import akka.actor._
import akka.contrib.pattern.ClusterSingletonManager
import akka.http.scaladsl.{ HttpExt, Http }
import akka.stream.ActorFlowMaterializer
import com.typesafe.config.Config
import slick.driver.PostgresDriver.api._

import im.actor.server.sms.{ ClickatellSmsEngine, SmsEngine }

object NotificationsSender {

  private[notifications] case object Notify

  private val singletonName: String = "notificationsSender"

  def startSingleton(notificationsConfig: NotificationsConfig, smsConfig: Config)(
    implicit
    db:           Database,
    system:       ActorSystem,
    materializer: ActorFlowMaterializer
  ): ActorRef = {
    implicit val http: HttpExt = Http()
    implicit val engine: ClickatellSmsEngine = new ClickatellSmsEngine(smsConfig.getConfig("clickatell"))

    system.actorOf(
      ClusterSingletonManager.props(
        singletonProps = props(db, notificationsConfig, engine),
        singletonName = singletonName,
        terminationMessage = PoisonPill,
        role = None
      ),
      name = s"${singletonName}Manager"
    )
  }

  private def props(implicit db: Database, config: NotificationsConfig, engine: SmsEngine) =
    Props(classOf[NotificationsSender], db, config, engine)
}

class NotificationsSender(implicit db: Database, config: NotificationsConfig, engine: SmsEngine) extends Actor with ActorLogging {

  import NotificationsSender._

  implicit val ec: ExecutionContextExecutor = context.system.dispatcher
  implicit val watcherConfig: UnreadWatcherConfig = config.watcherConfig

  private val unreadWatcher = new UnreadWatcher()
  private val notifier = new PhoneNotifier(engine)

  val scheduledSend = context.system.scheduler.schedule(config.initialDelay, config.interval, self, Notify)

  override def postStop(): Unit = {
    super.postStop()
    scheduledSend.cancel()
  }

  def receive: Receive = {
    case Notify ⇒
      unreadWatcher.getNotifications.map { tasks ⇒
        tasks.foreach(notifier.processTask)
      }
    case _ ⇒
  }

}
