package im.actor.server.api.http

import scala.util.{ Success, Try }

import com.github.kxbmap.configs._
import com.typesafe.config.Config

case class HttpApiConfig(interface: String, port: Int, scheme: String, host: String, staticFiles: String, keystore: Option[String])

object HttpApiConfig {
  def load(config: Config): Try[HttpApiConfig] =
    for {
      interface ← config.get[Try[String]]("interface")
      port ← config.get[Try[Int]]("port")
      scheme ← config.get[Try[String]]("scheme")
      host ← config.get[Try[String]]("host")
      staticFiles ← config.get[Try[String]]("static-files-directory")
      keystore ← Success(config.opt[String]("keystore"))
    } yield HttpApiConfig(
      interface, port, scheme, host, staticFiles, keystore
    )
}
