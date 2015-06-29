package im.actor.server.api.rpc.service.auth

import java.util.concurrent.TimeUnit

import scala.concurrent.duration._

import com.typesafe.config.Config

case class AuthConfig(expiration: FiniteDuration)

object AuthConfig {
  def fromConfig(config: Config): AuthConfig =
    AuthConfig(config.getDuration("code-expiration", TimeUnit.MILLISECONDS).millis)
}