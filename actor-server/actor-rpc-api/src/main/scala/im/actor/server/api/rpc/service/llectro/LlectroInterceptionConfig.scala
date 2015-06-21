package im.actor.server.api.rpc.service.llectro

import com.typesafe.config.Config

object LlectroInterceptionConfig {
  def fromConfig(config: Config): LlectroInterceptionConfig =
    LlectroInterceptionConfig(config.getInt("messages-between-ads"))
}

case class LlectroInterceptionConfig(messagesBetweenAds: Int)