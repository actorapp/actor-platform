package im.actor.server.webrtc

import akka.actor._
import akka.pattern.ask
import akka.cluster.sharding.ShardRegion.{ ExtractShardId, ExtractEntityId }
import akka.cluster.sharding.{ ClusterShardingSettings, ClusterSharding }
import akka.util.Timeout
import im.actor.config.ActorConfig

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

  def doCall(callerUserId: Int, receiverUserId: Int): Future[Long] = {
    val callId = ThreadLocalRandom.current().nextLong()

    region ? WebrtcCallEnvelope(callId, StartCall(callerUserId, receiverUserId)) map (_ ⇒ callId)
  }

  def endCall(userId: Int, callId: Long): Future[Unit] =
    region ? WebrtcCallEnvelope(callId, EndCall(userId)) map (_ ⇒ ())

  def sendCallSignal(userId: Int, callId: Long, content: Array[Byte]): Future[Unit] =
    region ? WebrtcCallEnvelope(callId, CallSignal(userId, content)) map (_ ⇒ ())

  def sendCallInProgress(userId: Int, callId: Long, ptimeout: Int): Future[Unit] =
    region ? WebrtcCallEnvelope(callId, CallInProgress(userId, ptimeout)) map (_ ⇒ ())
}

object WebrtcExtension extends ExtensionIdProvider with ExtensionId[WebrtcExtension] {
  override def lookup(): ExtensionId[_ <: Extension] = WebrtcExtension

  override def createExtension(system: ExtendedActorSystem): WebrtcExtension = new WebrtcExtension(system)
}
