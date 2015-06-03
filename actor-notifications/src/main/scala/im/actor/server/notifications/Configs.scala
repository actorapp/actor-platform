package im.actor.server.notifications

import java.util.concurrent.TimeUnit

import scala.concurrent.duration._

import com.typesafe.config.Config

case class UnreadWatcherConfig(unreadTimeout: FiniteDuration)

case class NotificationsConfig(interval: FiniteDuration, watcherConfig: UnreadWatcherConfig)

object NotificationsConfig {
  def fromConfig(config: Config): NotificationsConfig =
    NotificationsConfig(
      config.getDuration("interval", TimeUnit.MINUTES).minutes,
      UnreadWatcherConfig(config.getDuration("watcher.unread-timeout", TimeUnit.MINUTES).minutes)
    )
}