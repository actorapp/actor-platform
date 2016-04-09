package im.actor.server.sequence

import com.typesafe.config.Config

import scala.collection.JavaConversions._
import scala.util.Try

final case class ApplePushConfig(certs: List[ApnsCert])

object ApplePushConfig {
  def load(config: Config): ApplePushConfig =
    ApplePushConfig(
      certs = config.getConfigList("certs").toList map ApnsCert.fromConfig
    )
}

final case class ApnsCert(key: Int, path: String, password: String, isSandbox: Boolean, isVoip: Boolean)

object ApnsCert {
  def fromConfig(config: Config): ApnsCert = {
    ApnsCert(
      config.getInt("key"),
      config.getString("path"),
      config.getString("password"),
      Try(config.getBoolean("voip")).getOrElse(false),
      Try(config.getBoolean("sandbox")).getOrElse(false)
    )
  }
}