package im.actor.server.webrtc

import akka.actor._
import akka.pattern.ask
import akka.cluster.sharding.ShardRegion.{ ExtractShardId, ExtractEntityId }
import akka.cluster.sharding.{ ClusterShardingSettings, ClusterSharding }
import akka.util.Timeout
import im.actor.config.ActorConfig
import im.actor.types._

import scala.concurrent.Future
import scala.concurrent.forkjoin.ThreadLocalRandom

object Webrtc {
  val WeakGroup = "webrtc_calls"
}

final class WebrtcExtension(system: ActorSystem) extends Extension {
  import im.actor.server.webrtc.WebrtcCallMessages._
  import system.dispatcher

  private implicit val timeout: Timeout = Timeout(ActorConfig.defaultTimeout)

  private val extractEntityId: ExtractEntityId = {
    case WebrtcCallEnvelope(id, message) ⇒ (id.toString, message)
  }

  private val extractShardId: ExtractShardId = {
    case WebrtcCallEnvelope(id, _) ⇒ (id % 100).toString
  }

  private val region =
    ClusterSharding(system)
      .start("WebrtcCall", WebrtcCallActor.props, ClusterShardingSettings(system), extractEntityId, extractShardId)

  def doCall(callerUserId: Int, receiverUserId: Int, eventBusId: String): Future[Long] = {
    val callId = ThreadLocalRandom.current().nextLong()

    region ? WebrtcCallEnvelope(callId, StartCall(callerUserId, receiverUserId, eventBusId)) map (_ ⇒ callId)
  }

  def getInfo(callId: Long): Future[(String, UserId, Seq[UserId])] =
    (region ? WebrtcCallEnvelope(callId, GetInfo)).mapTo[GetInfoAck] map (_.tupled)
}

object WebrtcExtension extends ExtensionIdProvider with ExtensionId[WebrtcExtension] {
  override def lookup(): ExtensionId[_ <: Extension] = WebrtcExtension

  override def createExtension(system: ExtendedActorSystem): WebrtcExtension = new WebrtcExtension(system)
}
