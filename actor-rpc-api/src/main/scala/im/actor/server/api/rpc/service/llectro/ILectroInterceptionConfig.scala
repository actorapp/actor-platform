package im.actor.server.api.rpc.service.llectro

import com.typesafe.config.Config

object ILectroInterceptionConfig {
  def fromConfig(config: Config): ILectroInterceptionConfig =
    ILectroInterceptionConfig(config.getInt("messages-between-ads"))
}

case class ILectroInterceptionConfig(messagesBetweenAds: Int)