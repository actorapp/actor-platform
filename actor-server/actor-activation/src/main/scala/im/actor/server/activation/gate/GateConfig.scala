package im.actor.server.activation.gate

import com.github.kxbmap.configs.syntax._
import com.typesafe.config.Config
import im.actor.config.ActorConfig

import scala.util.Try

case class GateConfig(uri: String, authToken: String)

object GateConfig {
  def load(config: Config): Try[GateConfig] =
    for {
      uri ← config.get[Try[String]]("uri")
      authToken ← config.get[Try[String]]("auth-token")
    } yield GateConfig(uri, authToken)

  def load: Try[GateConfig] = {
    for {
      config ← Try(ActorConfig.load().getConfig("services.actor-activation"))
        .orElse(Try(ActorConfig.load().getConfig("services.activation-gate")))
      gateConfig ← load(config)
    } yield gateConfig
  }
}