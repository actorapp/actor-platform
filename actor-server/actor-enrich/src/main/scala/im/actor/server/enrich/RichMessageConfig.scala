package im.actor.server.enrich

import scala.util.Try

import com.typesafe.config.{ ConfigFactory, Config }

case class RichMessageConfig(maxSize: Long)

object RichMessageConfig {
  def load(config: Config): Try[RichMessageConfig] =
    for {
      maxSize ← Try(config.getBytes("max-preview-size"))
    } yield RichMessageConfig(maxSize)

  def load(): Try[RichMessageConfig] =
    load(ConfigFactory.load().getConfig("enabled-modules.enricher"))
}