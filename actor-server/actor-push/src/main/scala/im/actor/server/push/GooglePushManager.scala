package im.actor.server.push

import scala.util.Try

import com.github.kxbmap.configs._
import com.google.android.gcm.server.Sender
import com.typesafe.config.Config

case class GooglePushKey(projectId: Long, key: String)

object GooglePushKey {
  def load(config: Config): Try[GooglePushKey] = {
    for {
      projectId ← config.get[Try[Long]]("project-id")
      key ← config.get[Try[String]]("key")
    } yield GooglePushKey(projectId, key)
  }
}

case class GooglePushManagerConfig(keys: List[GooglePushKey])

object GooglePushManagerConfig {
  def load(googlePushConfig: Config): Try[GooglePushManagerConfig] =
    for {
      keyConfigs ← googlePushConfig.get[Try[List[Config]]]("keys")
      keys ← Try(keyConfigs map (GooglePushKey.load(_).get))
    } yield GooglePushManagerConfig(keys)
}

class GooglePushManager(config: GooglePushManagerConfig) {
  private val senders: Map[Long, Sender] =
    (config.keys map {
      case GooglePushKey(projectId, key) ⇒
        val sender = new Sender(key)
        (projectId → sender)
    }).toMap

  def getInstance(key: Long): Option[Sender] =
    senders.get(key)
}