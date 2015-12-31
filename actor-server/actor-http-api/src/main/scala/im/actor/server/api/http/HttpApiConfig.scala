package im.actor.server.api.http

import im.actor.config.ActorConfig

import scala.util.{ Success, Try }

import com.github.kxbmap.configs._
import com.typesafe.config.Config

case class HttpApiConfig(interface: String, port: Int, baseUri: String, staticFiles: String, keystore: Option[String])

object HttpApiConfig {
  def load(config: Config): Try[HttpApiConfig] =
    for {
      interface ← config.get[Try[String]]("interface")
      port ← config.get[Try[Int]]("port")
      baseUri ← config.get[Try[String]]("base-uri")
      staticFiles ← config.get[Try[String]]("static-files-directory")
      keystore ← Success(config.opt[String]("keystore"))
    } yield HttpApiConfig(interface, port, baseUri, staticFiles, keystore)

  def load: Try[HttpApiConfig] = load(ActorConfig.load().getConfig("http"))
}
