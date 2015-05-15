package im.actor.server.notifications

import akka.actor.{ ActorLogging, Actor, Props, ActorSystem }
import akka.http.scaladsl.Http
import akka.stream.ActorFlowMaterializer
import com.typesafe.config.Config
import slick.driver.PostgresDriver.api._

import im.actor.server.sms.{ SmsEngine, ClickatellSmsEngine }

object NotificationsService {

  def init(notificationsConfig: NotificationsConfig, smsConfig: Config)(implicit db: Database, system: ActorSystem, materializer: ActorFlowMaterializer) = {

    implicit val http = Http()
    implicit val ec = system.dispatcher
    implicit val engine = new ClickatellSmsEngine(smsConfig.getConfig("clickatell"))
    implicit val watcherConfig = notificationsConfig.watcherConfig

    val notificationSender = system.actorOf(NotificationsSender.props)

    system.scheduler.schedule(
      notificationsConfig.initialDelay,
      notificationsConfig.interval,
      notificationSender,
      NotificationsSender.Notify
    )
  }

}

object NotificationsSender {

  private[notifications] case object Notify

  def props(implicit db: Database, config: UnreadWatcherConfig, engine: SmsEngine) =
    Props(classOf[NotificationsSender], db, config, engine)
}

class NotificationsSender(implicit db: Database, config: UnreadWatcherConfig, engine: SmsEngine) extends Actor with ActorLogging {

  import NotificationsSender._

  implicit val ec = context.system.dispatcher

  val unreadWatcher = new UnreadWatcher()
  val notifier = new PhoneNotifier(engine)

  def receive: Receive = {
    case Notify ⇒
      unreadWatcher.getNotifications.map { tasks ⇒
        tasks.foreach(notifier.processTask)
      }
    case _ ⇒
  }

}
