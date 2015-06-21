package im.actor.server.llectro

import com.typesafe.config.Config

private[llectro] case class LlectroConfig(baseUrl: String, authToken: String)

private[llectro] object LlectroConfig {
  def apply(config: Config): LlectroConfig = {
    LlectroConfig(
      config.getString("api-url"),
      config.getString("auth-token")
    )
  }
}
