package im.actor.server.push

import scala.concurrent._
import scala.concurrent.duration._

import akka.actor._
import akka.util.Timeout

import im.actor.api.rpc.codecs.UpdateBoxCodec
import im.actor.api.rpc.{ UpdateBox => ProtoUpdateBox }
import im.actor.server.mtproto.protocol.UpdateBox

object UpdatesPusher {

  @SerialVersionUID(1L)
  private object SubscribeToSeq

  @SerialVersionUID(1L)
  private object SubscribeToWeak

  def props(seqUpdatesManagerRegion: ActorRef, weakUpdatesManagerRegion: ActorRef, authId: Long, session: ActorRef) =
    Props(classOf[UpdatesPusher], seqUpdatesManagerRegion, weakUpdatesManagerRegion, authId, session)
}

private[push] class UpdatesPusher(seqUpdatesManagerRegion: ActorRef,
                                  weakUpdatesManagerRegion: ActorRef,
                                  authId: Long,
                                  session: ActorRef) extends Actor with ActorLogging {

  import UpdatesPusher._
  import im.actor.server.session.SessionMessage._

  implicit val ec: ExecutionContext = context.dispatcher
  implicit val system: ActorSystem = context.system
  implicit val timeout: Timeout = Timeout(5.seconds) // TODO: configurable

  override def preStart(): Unit = {
    self ! SubscribeToSeq

    self ! SubscribeToWeak
  }

  def receive = {
    case SubscribeToSeq =>
      SeqUpdatesManager.subscribe(seqUpdatesManagerRegion, authId, self) onFailure {
        case e =>
          self ! SubscribeToSeq
          log.error(e, "Failed to subscribe to sequence updates")
      }
    case SubscribeToWeak =>
      WeakUpdatesManager.subscribe(weakUpdatesManagerRegion, authId, self) onFailure {
        case e =>
          self ! SubscribeToWeak
          log.error(e, "Failed to subscribe to weak updates")
      }
    case updateBox: ProtoUpdateBox =>
      val ub = UpdateBox(UpdateBoxCodec.encode(updateBox).require)
      session ! SendProtoMessage(ub)
  }
}