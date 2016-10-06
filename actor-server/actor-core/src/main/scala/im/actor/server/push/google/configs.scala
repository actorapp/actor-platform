package im.actor.server.push.google

import com.typesafe.config.{ Config, ConfigException }
import com.github.kxbmap.configs.syntax._
import im.actor.config.ActorConfig

import scala.util.Try

private final case class GooglePushKey(projectId: Long, key: String)

private[google] final case class GooglePushManagerConfig(keys: List[GooglePushKey]) {
  def keyMap: Map[Long, String] =
    (keys map {
      case GooglePushKey(projectId, key) ⇒ projectId → key
    }).toMap
}

private[google] object GooglePushManagerConfig {

  def loadGCM: Try[GooglePushManagerConfig] = {
    val config = ActorConfig.load()
    load(config.getConfig("services.google.gcm")) recoverWith {
      case e: ConfigException.Missing ⇒ load(config.getConfig("services.google.push")) // legacy conf, before firebase
    }
  }

  def loadFirebase: Try[GooglePushManagerConfig] =
    load(ActorConfig.load().getConfig("services.google.firebase"))

  private def load(config: Config) = Try(config.extract[GooglePushManagerConfig])
}
