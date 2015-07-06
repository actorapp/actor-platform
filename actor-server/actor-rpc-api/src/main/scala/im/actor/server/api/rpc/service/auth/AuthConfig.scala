package im.actor.server.api.rpc.service.auth

import scala.concurrent.duration._
import scala.util.Try

import com.github.kxbmap.configs._
import com.typesafe.config.{ Config, ConfigFactory }

case class AuthConfig(expiration: Duration, attempts: Int)

object AuthConfig {
  def load(config: Config): Try[AuthConfig] =
    for {
      exp ← config.get[Try[Duration]]("code-expiration")
      att ← config.get[Try[Int]]("code-attempts")
    } yield AuthConfig(exp, att)

  def load: Try[AuthConfig] = {
    load(ConfigFactory.load().getConfig("enabled-modules.auth"))
  }
}