package im.actor.server.ilectro

import com.typesafe.config.Config

private[ilectro] case class ILectroConfig(baseUrl: String, authToken: String)

private[ilectro] object ILectroConfig {
  def apply(config: Config): ILectroConfig = {
    ILectroConfig(
      config.getString("api-url"),
      config.getString("auth-token")
    )
  }
}
