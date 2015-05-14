package im.actor.server.notifications

import java.util.concurrent.TimeUnit

import scala.concurrent.duration.{ Duration, _ }

import com.typesafe.config.Config

case class UnreadWatcherConfig(unreadTimeout: Duration)

object UnreadWatcherConfig {
  def apply(config: Config): UnreadWatcherConfig =
    UnreadWatcherConfig.apply(config.getDuration("unread-timeout", TimeUnit.HOURS).minutes)
}
