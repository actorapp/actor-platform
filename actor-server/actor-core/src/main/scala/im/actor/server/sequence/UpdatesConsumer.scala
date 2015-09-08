package im.actor.server.sequence

import akka.actor._
import akka.util.Timeout
import im.actor.api.rpc.codecs.UpdateBoxCodec
import im.actor.api.rpc.groups.ApiGroup
import im.actor.api.rpc.messaging.UpdateMessageSent
import im.actor.api.rpc.sequence.{ FatSeqUpdate, WeakUpdate }
import im.actor.api.rpc.users.ApiUser
import im.actor.api.rpc.weak.{ UpdateGroupOnline, UpdateUserLastSeen, UpdateUserOffline, UpdateUserOnline }
import im.actor.api.rpc.{ Update, UpdateBox ⇒ ProtoUpdateBox }
import im.actor.server.db.DbExtension
import im.actor.server.group.{ GroupExtension, GroupOffice, GroupViewRegion }
import im.actor.server.mtproto.protocol.UpdateBox
import im.actor.server.persist
import im.actor.server.presences._
import im.actor.server.user.{ UserExtension, UserOffice, UserViewRegion }
import org.joda.time.DateTime

import scala.concurrent._
import scala.concurrent.duration._

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
    weakUpdatesManagerRegion:   WeakUpdatesManagerRegion,
    presenceManagerRegion:      PresenceManagerRegion,
    groupPresenceManagerRegion: GroupPresenceManagerRegion
  ) =
    Props(
      classOf[UpdatesConsumer],
      authId,
      session,
      weakUpdatesManagerRegion,
      presenceManagerRegion,
      groupPresenceManagerRegion
    )
}

private[sequence] class UpdatesConsumer(
  authId:     Long,
  subscriber: ActorRef
)(
  implicit
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

  private implicit val seqUpdExt: SeqUpdatesExtension = SeqUpdatesExtension(context.system)
  private var lastDateTime = new DateTime

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
    case UpdateReceived(updateBox, fatRefsOpt) ⇒
      if (updateBox.updateHeader != UpdateMessageSent.header) {
        (fatRefsOpt match {
          case None ⇒ Future.successful(updateBox)
          case Some(UpdateRefs(userIds, groupIds)) ⇒
            // FIXME: #perf cache userId
            DbExtension(context.system).db.run(persist.AuthId.findUserId(authId)) flatMap {
              case Some(userId) ⇒
                for {
                  (users, groups) ← getFatData(userId, userIds, groupIds)
                } yield {
                  FatSeqUpdate(updateBox.seq, updateBox.state, updateBox.updateHeader, updateBox.update, users.toVector, groups.toVector)
                }
              case None ⇒
                throw new Exception(s"Cannot find userId for authId ${authId}")
            }
        }) foreach (sendUpdateBox)
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

      log.debug("Pushing presence {}", update)

      val updateBox = WeakUpdate(nextDateTime().getMillis, update.header, update.toByteArray)
      sendUpdateBox(updateBox)
    case GroupPresenceState(groupId, onlineCount) ⇒
      val update = UpdateGroupOnline(groupId, onlineCount)

      log.debug("Pushing presence {}", update)

      val updateBox = WeakUpdate(nextDateTime().getMillis, update.header, update.toByteArray)
      sendUpdateBox(updateBox)
  }

  private def nextDateTime(): DateTime = {
    val now = new DateTime

    this.lastDateTime =
      if (now == this.lastDateTime)
        now.plus(1L)
      else
        now

    this.lastDateTime
  }

  private def sendUpdateBox(updateBox: ProtoUpdateBox): Unit = {
    subscriber ! UpdateBox(UpdateBoxCodec.encode(updateBox).require)
  }

  private def getFatData(
    userId:      Int,
    fatUserIds:  Seq[Int],
    fatGroupIds: Seq[Int]
  )(
    implicit
    ec: ExecutionContext
  ): Future[(Seq[ApiUser], Seq[ApiGroup])] = {
    implicit lazy val userViewRegion: UserViewRegion = UserExtension(system).viewRegion
    implicit lazy val groupViewRegion: GroupViewRegion = GroupExtension(system).viewRegion

    for {
      groups ← Future.sequence(fatGroupIds map (GroupOffice.getApiStruct(_, userId)))
      groupMemberIds = groups.view.map(_.members.map(_.userId)).flatten
      users ← Future.sequence((fatUserIds ++ groupMemberIds).distinct map (UserOffice.getApiStruct(_, userId, authId)))
    } yield (users, groups)
  }
}