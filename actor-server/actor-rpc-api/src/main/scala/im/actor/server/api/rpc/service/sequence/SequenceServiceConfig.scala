package im.actor.server.api.rpc.service.sequence

import scala.util.Try

import com.typesafe.config.{ ConfigFactory, Config }

case class SequenceServiceConfig(maxUpdateSizeInBytes: Long)

object SequenceServiceConfig {
  def load(config: Config): Try[SequenceServiceConfig] =
    for {
      maxSize ← Try(config.getBytes("max-update-size"))
    } yield SequenceServiceConfig(maxSize)

  def load(): Try[SequenceServiceConfig] =
    load(ConfigFactory.load().getConfig("enabled-modules.sequence"))
}