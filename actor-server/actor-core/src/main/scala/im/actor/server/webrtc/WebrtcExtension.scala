package im.actor.server.webrtc

import akka.actor._
import akka.cluster.sharding.ShardRegion.{ ExtractEntityId, ExtractShardId }
import akka.cluster.sharding.{ ClusterSharding, ClusterShardingSettings }
import akka.pattern.ask
import akka.util.Timeout
import com.github.kxbmap.configs.syntax._
import com.typesafe.config.Config
import im.actor.config.ActorConfig
import im.actor.server.acl.ACLUtils
import im.actor.server.eventbus.EventBus
import im.actor.server.model.Peer
import im.actor.types._

import scala.concurrent.Future
import scala.util.Try

object Webrtc {
  private[webrtc] val SyncedSetName = "messaging.calls"
}

private[webrtc] case class ICEServer(url: String, username: Option[String], credential: Option[String])

private[webrtc] case class WebrtcConfig(iceServers: Seq[ICEServer])

private[webrtc] object WebrtcConfig {
  def load(config: Config): Try[WebrtcConfig] = Try(config.get[WebrtcConfig]("modules.webrtc"))
}

final class WebrtcExtension(system: ActorSystem) extends Extension {

  import im.actor.server.webrtc.WebrtcCallMessages._
  import system.dispatcher

  private implicit val defaultTimeout: Timeout = Timeout(ActorConfig.defaultTimeout)

  private val extractEntityId: ExtractEntityId = {
    case WebrtcCallEnvelope(id, message) ⇒ (id.toString, message)
  }

  private val extractShardId: ExtractShardId = {
    case WebrtcCallEnvelope(id, _) ⇒ (id % 100).toString
  }

  private val region =
    ClusterSharding(system)
      .start("WebrtcCall", WebrtcCallActor.props, ClusterShardingSettings(system), extractEntityId, extractShardId)

  private[webrtc] val config = WebrtcConfig.load(system.settings.config).get

  def doCall(callerUserId: UserId, callerAuthId: AuthId, peer: Peer, timeout: Option[Long]): Future[(Long, String, EventBus.DeviceId)] = {
    val callId = ACLUtils.randomLong()

    (region ? WebrtcCallEnvelope(
      callId,
      StartCall(callerUserId, callerAuthId, peer, timeout)
    )).mapTo[StartCallAck] map (ack ⇒ (callId, ack.eventBusId, ack.callerDeviceId))
  }

  def joinCall(calleeUserId: Int, calleeAuthId: Long, callId: Long): Future[Unit] =
    region ? WebrtcCallEnvelope(callId, JoinCall(calleeUserId, calleeAuthId)) map (_ ⇒ ())

  def rejectCall(calleeUserId: Int, calleeAuthId: Long, callId: Long): Future[Unit] =
    region ? WebrtcCallEnvelope(callId, RejectCall(calleeUserId, calleeAuthId)) map (_ ⇒ ())

  def getInfo(callId: Long): Future[(String, Peer, Seq[UserId])] =
    (region ? WebrtcCallEnvelope(callId, GetInfo)).mapTo[GetInfoAck] map (_.tupled)
}

object WebrtcExtension extends ExtensionIdProvider with ExtensionId[WebrtcExtension] {
  override def lookup(): ExtensionId[_ <: Extension] = WebrtcExtension

  override def createExtension(system: ExtendedActorSystem): WebrtcExtension = new WebrtcExtension(system)
}
