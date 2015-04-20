package im.actor.server.push

import scala.concurrent._
import scala.concurrent.duration._

import akka.actor._
import akka.util.Timeout
import org.joda.time.DateTime

import im.actor.api.rpc.codecs.UpdateBoxCodec
import im.actor.api.rpc.sequence.WeakUpdate
import im.actor.api.rpc.weak.{ UpdateUserLastSeen, UpdateUserOffline, UpdateUserOnline }
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

  @SerialVersionUID(1L)
  case class UnsubscribeFromUserPresences(userIds: Set[Int])

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
    case cmd @ SubscribeToUserPresences(userIds) =>
      userIds foreach { userId =>
        PresenceManager.subscribe(presenceManagerRegion, userId, self) onFailure {
          case e =>
            self ! cmd
            log.error(e, "Failed to subscribe to presences")
        }
      }
    case cmd @ UnsubscribeFromUserPresences(userIds) =>
      userIds foreach { userId =>
        PresenceManager.unsubscribe(presenceManagerRegion, userId, self) onFailure {
          case e =>
            self ! cmd
            log.error(e, "Failed to subscribe from presences")
        }
      }
    case updateBox: ProtoUpdateBox =>
      sendUpdateBox(updateBox)
    case PresenceState(userId, presence, lastSeenAt) =>
      log.debug("presence: {}, lastSeenAt {}", presence, lastSeenAt)

      val update: Update =
        presence match {
          case Online =>
            UpdateUserOnline(userId)
          case Offline =>
            lastSeenAt match {
              case Some(date) =>
                UpdateUserLastSeen(userId, date.getMillis)
              case None =>
                UpdateUserOffline(userId)
            }
        }

      log.debug("Formed update: {}", update)

      val updateBox = WeakUpdate((new DateTime).getMillis, update.header, update.toByteArray)
      sendUpdateBox(updateBox)
  }

  private def sendUpdateBox(updateBox: ProtoUpdateBox): Unit = {
    val ub = UpdateBox(UpdateBoxCodec.encode(updateBox).require)
    session ! SendProtoMessage(ub)
  }
}