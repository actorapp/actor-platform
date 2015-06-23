package im.actor.server.api.rpc.service.auth

import java.util.concurrent.TimeUnit

import scala.concurrent.duration._

import com.typesafe.config.Config

case class AuthSmsConfig(expiration: FiniteDuration)

object AuthSmsConfig {
  def fromConfig(config: Config): AuthSmsConfig =
    AuthSmsConfig(config.getDuration("sms-expiration", TimeUnit.MILLISECONDS).millis)
}