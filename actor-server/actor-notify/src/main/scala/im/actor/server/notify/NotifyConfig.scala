package im.actor.server.notify

import scala.util.Try
import com.github.kxbmap.configs.syntax._
import com.typesafe.config.Config
import im.actor.config.ActorConfig

import scala.concurrent.duration.FiniteDuration

private[notify] final case class NotifyConfig(
  notifyAfter:       FiniteDuration,
  emailTemplatePath: String,
  resolvedDomains:   Set[String]
)

private[notify] object NotifyConfig {
  def load(config: Config): Try[NotifyConfig] = Try(config.extract[NotifyConfig])
  def load: Try[NotifyConfig] = load(ActorConfig.load().getConfig("services.notify"))
}