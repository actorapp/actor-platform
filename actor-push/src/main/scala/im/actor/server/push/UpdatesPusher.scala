package im.actor.server.push

import scala.concurrent._
import scala.concurrent.duration._

import akka.actor._
import akka.util.Timeout
import org.joda.time.DateTime

import im.actor.api.rpc.codecs.UpdateBoxCodec
import im.actor.api.rpc.sequence.WeakUpdate
import im.actor.api.rpc.weak.{ UpdateUserOffline, UpdateUserOnline }
import im.actor.api.rpc.{ UpdateBox => ProtoUpdateBox, Update }
import im.actor.server.mtproto.protocol.UpdateBox
import im.actor.server.presences.{ PresenceManagerRegion, PresenceManager }

object UpdatesPusher {

  @SerialVersionUID(1L)
  private object SubscribeToSeq

  @SerialVersionUID(1L)
  private object SubscribeToWeak

  @SerialVersionUID(1L)
  case class SubscribeToUserPresences(userIds: Set[Int])

  def props(seqUpdatesManagerRegion: SeqUpdatesManagerRegion,
            weakUpdatesManagerRegion: WeakUpdatesManagerRegion,
            presenceManagerRegion: PresenceManagerRegion,
            authId: Long,
            session: ActorRef) =
    Props(classOf[UpdatesPusher],
      seqUpdatesManagerRegion,
      weakUpdatesManagerRegion,
      presenceManagerRegion,
      authId,
      session)
}

private[push] class UpdatesPusher(seqUpdatesManagerRegion: SeqUpdatesManagerRegion,
                                  weakUpdatesManagerRegion: WeakUpdatesManagerRegion,
                                  presenceManagerRegion: PresenceManagerRegion,
                                  authId: Long,
                                  session: ActorRef) extends Actor with ActorLogging {

  import UpdatesPusher._
  import im.actor.server.session.SessionMessage._
  import PresenceManager._

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
    case SubscribeToUserPresences(userIds) =>
      userIds foreach { userId =>
        PresenceManager.subscribe(presenceManagerRegion, userId, self) onFailure {
          case e =>
            self ! SubscribeToUserPresences(userIds)
            log.error(e, "Failed to subscribe to presences")
        }
      }
    case updateBox: ProtoUpdateBox =>
      sendUpdateBox(updateBox)
    case PresenceState(userId, presence, lastSeenAt) =>
      val update: Update =
        presence match {
          case Online =>
            UpdateUserOnline(userId)
          case Offline =>
            UpdateUserOffline(userId)
        }

      val updateBox = WeakUpdate((new DateTime).getMillis, update.header, update.toByteArray)
      sendUpdateBox(updateBox)
  }

  private def sendUpdateBox(updateBox: ProtoUpdateBox): Unit = {
    val ub = UpdateBox(UpdateBoxCodec.encode(updateBox).require)
    session ! SendProtoMessage(ub)
  }
}