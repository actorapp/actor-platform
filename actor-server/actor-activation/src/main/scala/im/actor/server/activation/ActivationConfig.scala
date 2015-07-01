package im.actor.server.activation

import scala.concurrent.duration._
import scala.util.Try

import com.github.kxbmap.configs._
import com.typesafe.config.Config

object ActivationConfig {
  def fromConfig(config: Config): Try[ActivationConfig] =
    for {
      waitInterval ← config.get[Try[Duration]]("code-send-interval")
    } yield ActivationConfig(waitInterval)
}

case class ActivationConfig(waitInterval: Duration)
