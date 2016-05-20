package im.actor.server.sequence

import com.typesafe.config.Config
import com.github.kxbmap.configs.syntax._

import scala.collection.JavaConversions._

final case class ApplePushConfig(certs: List[ApnsCert])

object ApplePushConfig {
  def load(config: Config): ApplePushConfig =
    ApplePushConfig(
      certs = config.getConfigList("certs").toList map ApnsCert.fromConfig
    )
}

final case class ApnsCert(
  key:       Option[Int],
  bundleId:  Option[String],
  path:      String,
  password:  String,
  isSandbox: Boolean,
  isVoip:    Boolean
)

object ApnsCert {
  def fromConfig(config: Config): ApnsCert = {
    ApnsCert(
      key = config.getOpt[Int]("key"),
      bundleId = config.getOpt[String]("bundleId"),
      path = config.get[String]("path"),
      password = config.get[String]("password"),
      isSandbox = config.getOrElse[Boolean]("sandbox", false),
      isVoip = config.getOrElse[Boolean]("voip", false)
    )
  }
}