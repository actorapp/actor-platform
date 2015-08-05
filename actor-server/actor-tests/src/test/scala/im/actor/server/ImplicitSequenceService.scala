package im.actor.server

import akka.actor.ActorSystem
import akka.stream.Materializer
import im.actor.api.rpc.sequence.SequenceService
import im.actor.server.api.rpc.service.sequence.{ SequenceServiceConfig, SequenceServiceImpl }

trait ImplicitSequenceService extends ImplicitSessionRegionProxy with ImplicitSeqUpdatesManagerRegion {
  protected implicit val materializer: Materializer
  protected implicit val system: ActorSystem

  private val sequenceConfig = SequenceServiceConfig.load().get

  protected implicit lazy val sequenceService: SequenceService = new SequenceServiceImpl(sequenceConfig)

}
