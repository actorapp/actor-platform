package im.actor.server.activation.internal

import com.github.kxbmap.configs.syntax._
import com.typesafe.config.Config
import im.actor.config.ActorConfig

import scala.concurrent.duration.Duration
import scala.util.Try

case class InternalActivationConfig(onlineWindow: Duration, senderUserId: Int, messageTemplate: String)

object InternalActivationConfig {
  def load(config: Config): Try[InternalActivationConfig] =
    for {
      onlineWindow ← config.get[Try[Duration]]("online-time-window")
      senderUserId ← config.get[Try[Int]]("sender-user-id")
      messageTemplate ← config.get[Try[String]]("message-template")
    } yield InternalActivationConfig(onlineWindow, senderUserId, messageTemplate)

  def load: Try[InternalActivationConfig] = {
    for {
      config ← Try(ActorConfig.load().getConfig("services.activation.internal"))
      internalConfig ← load(config)
    } yield internalConfig
  }
}