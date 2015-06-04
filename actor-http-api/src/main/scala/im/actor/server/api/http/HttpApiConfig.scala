package im.actor.server.api.http

import com.typesafe.config.Config

case class HttpApiConfig(protocol: String, interface: String, port: Int)

object HttpApiConfig {
  def fromConfig(config: Config): HttpApiConfig =
    HttpApiConfig(
      config.getString("protocol"),
      config.getString("interface"),
      config.getInt("port")
    )
}
