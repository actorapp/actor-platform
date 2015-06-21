package im.actor.server.push

import scala.concurrent._
import scala.concurrent.duration._

import akka.actor._
import akka.util.Timeout
import org.joda.time.DateTime

import im.actor.api.rpc.codecs.UpdateBoxCodec
import im.actor.api.rpc.messaging.UpdateMessageSent
import im.actor.api.rpc.sequence.{ FatSeqUpdate, SeqUpdate, WeakUpdate }
import im.actor.api.rpc.weak.{ UpdateGroupOnline, UpdateUserLastSeen, UpdateUserOffline, UpdateUserOnline }
import im.actor.api.rpc.{ Update, UpdateBox ⇒ ProtoUpdateBox }
import im.actor.server.mtproto.protocol.UpdateBox
import im.actor.server.presences._

trait UpdatesConsumerMessage

object UpdatesConsumerMessage {
  @SerialVersionUID(1L)
  case object SubscribeToSeq extends UpdatesConsumerMessage

  @SerialVersionUID(1L)
  case object SubscribeToWeak extends UpdatesConsumerMessage

  @SerialVersionUID(1L)
  case class SubscribeToUserPresences(userIds: Set[Int]) extends UpdatesConsumerMessage

  @SerialVersionUID(1L)
  case class UnsubscribeFromUserPresences(userIds: Set[Int]) extends UpdatesConsumerMessage

  @SerialVersionUID(1L)
  case class SubscribeToGroupPresences(groupIds: Set[Int]) extends UpdatesConsumerMessage

  @SerialVersionUID(1L)
  case class UnsubscribeFromGroupPresences(groupIds: Set[Int]) extends UpdatesConsumerMessage

}

object UpdatesConsumer {
  def props(authId: Long, session: ActorRef)(
    implicit
    seqUpdatesManagerRegion:    SeqUpdatesManagerRegion,
    weakUpdatesManagerRegion:   WeakUpdatesManagerRegion,
    presenceManagerRegion:      PresenceManagerRegion,
    groupPresenceManagerRegion: GroupPresenceManagerRegion
  ) =
    Props(
      classOf[UpdatesConsumer],
      authId,
      session,
      seqUpdatesManagerRegion,
      weakUpdatesManagerRegion,
      presenceManagerRegion,
      groupPresenceManagerRegion
    )
}

private[push] class UpdatesConsumer(
  authId:     Long,
  subscriber: ActorRef
)(
  implicit
  seqUpdatesManagerRegion:    SeqUpdatesManagerRegion,
  weakUpdatesManagerRegion:   WeakUpdatesManagerRegion,
  presenceManagerRegion:      PresenceManagerRegion,
  groupPresenceManagerRegion: GroupPresenceManagerRegion
)
  extends Actor with ActorLogging with Stash {

  import Presences._
  import UpdatesConsumerMessage._

  implicit val ec: ExecutionContext = context.dispatcher
  implicit val system: ActorSystem = context.system
  implicit val timeout: Timeout = Timeout(5.seconds) // TODO: configurable

  override def preStart(): Unit = {
    self ! SubscribeToSeq

    self ! SubscribeToWeak
  }

  def receive = {
    case SubscribeToSeq ⇒
      SeqUpdatesManager.subscribe(authId, self) onFailure {
        case e ⇒
          self ! SubscribeToSeq
          log.error(e, "Failed to subscribe to sequence updates")
      }
    case SubscribeToWeak ⇒
      WeakUpdatesManager.subscribe(authId, self) onFailure {
        case e ⇒
          self ! SubscribeToWeak
          log.error(e, "Failed to subscribe to weak updates")
      }
    case cmd @ SubscribeToUserPresences(userIds) ⇒
      userIds foreach { userId ⇒
        PresenceManager.subscribe(userId, self) onFailure {
          case e ⇒
            self ! cmd
            log.error(e, "Failed to subscribe to user presences")
        }
      }
    case cmd @ UnsubscribeFromUserPresences(userIds) ⇒
      userIds foreach { userId ⇒
        PresenceManager.unsubscribe(userId, self) onFailure {
          case e ⇒
            self ! cmd
            log.error(e, "Failed to subscribe from user presences")
        }
      }
    case cmd @ SubscribeToGroupPresences(groupIds) ⇒
      groupIds foreach { groupId ⇒
        GroupPresenceManager.subscribe(groupId, self) onFailure {
          case e ⇒
            self ! cmd
            log.error(e, "Failed to subscribe to group presences")
        }
      }
    case cmd @ UnsubscribeFromGroupPresences(groupIds) ⇒
      groupIds foreach { groupId ⇒
        GroupPresenceManager.unsubscribe(groupId, self) onFailure {
          case e ⇒
            self ! cmd
            log.error(e, "Failed to unsubscribe from group presences")
        }
      }
    case SeqUpdatesManager.UpdateReceived(updateBox) ⇒
      val ignore = updateBox match {
        case u: SeqUpdate if u.updateHeader == UpdateMessageSent.header ⇒ true
        case u: FatSeqUpdate if u.updateHeader == UpdateMessageSent.header ⇒ true
        case _ ⇒ false
      }

      if (!ignore) {
        sendUpdateBox(updateBox)
      }

    case WeakUpdatesManager.UpdateReceived(updateBox) ⇒
      sendUpdateBox(updateBox)
    case PresenceState(userId, presence, lastSeenAt) ⇒
      val update: Update =
        presence match {
          case Online ⇒
            UpdateUserOnline(userId)
          case Offline ⇒
            lastSeenAt match {
              case Some(date) ⇒
                UpdateUserLastSeen(userId, date.getMillis / 1000)
              case None ⇒
                UpdateUserOffline(userId)
            }
        }

      val updateBox = WeakUpdate((new DateTime).getMillis, update.header, update.toByteArray)
      sendUpdateBox(updateBox)
    case GroupPresenceState(groupId, onlineCount) ⇒
      val update = UpdateGroupOnline(groupId, onlineCount)
      val updateBox = WeakUpdate((new DateTime).getMillis, update.header, update.toByteArray)
      sendUpdateBox(updateBox)
  }

  private def sendUpdateBox(updateBox: ProtoUpdateBox): Unit = {
    subscriber ! UpdateBox(UpdateBoxCodec.encode(updateBox).require)
  }
}