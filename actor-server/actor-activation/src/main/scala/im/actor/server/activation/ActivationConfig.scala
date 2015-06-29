package im.actor.server.activation

import java.util.concurrent.TimeUnit

import scala.concurrent.duration._

import com.typesafe.config.Config

object ActivationConfig {
  def fromConfig(config: Config): ActivationConfig =
    ActivationConfig(config.getDuration("wait-interval", TimeUnit.MILLISECONDS).millis)
}

case class ActivationConfig(waitInterval: FiniteDuration)
