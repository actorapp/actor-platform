package im.actor.server.api.rpc.service.sequence

import scala.util.Try

import com.typesafe.config.{ ConfigFactory, Config }

case class SequenceServiceConfig(maxDifferenceSize: Long)

object SequenceServiceConfig {
  def load(config: Config): Try[SequenceServiceConfig] =
    for {
      maxDifferenceSize ← Try(config.getBytes("max-difference-size"))
    } yield SequenceServiceConfig(maxDifferenceSize)

  def load(): Try[SequenceServiceConfig] =
    load(ConfigFactory.load().getConfig("modules.sequence"))
}