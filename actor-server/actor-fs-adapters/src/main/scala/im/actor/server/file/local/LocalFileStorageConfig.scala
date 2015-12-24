package im.actor.server.file.local

import com.github.kxbmap.configs._
import com.typesafe.config.Config
import im.actor.config.ActorConfig

import scala.util.Try

case class LocalFileStorageConfig(location: String)

object LocalFileStorageConfig {
  def load(config: Config): Try[LocalFileStorageConfig] = {
    for {
      location ← config.get[Try[String]]("location")
    } yield LocalFileStorageConfig(location)
  }

  def load: Try[LocalFileStorageConfig] =
    for {
      config ← Try(ActorConfig.load().getConfig("services.filestorage"))
      result ← load(config)
    } yield result
}
