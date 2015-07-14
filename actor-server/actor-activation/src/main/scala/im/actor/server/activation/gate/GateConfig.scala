package im.actor.server.activation.gate

import scala.util.Try

import com.github.kxbmap.configs._
import com.typesafe.config.{ Config, ConfigFactory }

case class GateConfig(uri: String, authToken: String)

object GateConfig {
  def load(config: Config): Try[GateConfig] =
    for {
      uri ← config.get[Try[String]]("uri")
      authToken ← config.get[Try[String]]("auth-token")
    } yield GateConfig(uri, authToken)

  def load: Try[GateConfig] = {
    load(ConfigFactory.load().getConfig("services.activation-gate"))
  }
}