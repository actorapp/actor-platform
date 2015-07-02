package im.actor.server.activation

import scala.concurrent.duration._
import scala.util.Try

import com.github.kxbmap.configs._
import com.typesafe.config.Config

object ActivationConfig {
  def fromConfig(config: Config): Try[ActivationConfig] =
    for {
      repeatLimit ← config.get[Try[Duration]]("repeat-limit")
    } yield ActivationConfig(repeatLimit)
}

case class ActivationConfig(repeatLimit: Duration)
