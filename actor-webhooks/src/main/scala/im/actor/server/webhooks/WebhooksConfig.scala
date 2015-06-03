package im.actor.server.webhooks

import com.typesafe.config.Config

case class WebhooksConfig(protocol: String, interface: String, port: Int, path: String)

object WebhooksConfig {
  def fromConfig(config: Config): WebhooksConfig =
    WebhooksConfig(
      config.getString("protocol"),
      config.getString("interface"),
      config.getInt("port"),
      config.getString("path")
    )
}
