package im.actor.server.email

import im.actor.config.ActorConfig

import scala.util.Try
import com.github.kxbmap.configs.syntax._
import com.typesafe.config.Config

private[email] case class Sender(address: String, name: String, prefix: String)
private[email] case class Smtp(host: String, port: Int, username: String, password: String, tls: Boolean)

case class EmailConfig(
  sender: Sender,
  smtp:   Smtp
)

object EmailConfig {
  def load(config: Config): Try[EmailConfig] = Try(config.extract[EmailConfig])

  def load: Try[EmailConfig] = load(ActorConfig.load().getConfig("services.email"))
}