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
import im.actor.server.group.GroupExtension
import im.actor.server.mtproto.protocol.UpdateBox
import im.actor.server.persist
import im.actor.server.presences._
import im.actor.server.user.UserExtension
import org.joda.time.DateTime

import scala.concurrent._
import scala.concurrent.duration._

final case class NewUpdate(ub: UpdateBox, reduceKey: Option[String])

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
  def props(authId: Long, session: ActorRef) =
    Props(classOf[UpdatesConsumer], authId, session)
}

private[sequence] class UpdatesConsumer(authId: Long, subscriber: ActorRef) extends Actor with ActorLogging with Stash {

  import Presences._
  import UpdatesConsumerMessage._

  implicit val ec: ExecutionContext = context.dispatcher
  implicit val system: ActorSystem = context.system
  implicit val timeout: Timeout = Timeout(5.seconds) // TODO: configurable
  private val presenceExt = PresenceExtension(system)
  private val groupRresenceExt = GroupPresenceExtension(system)
  private val weakUpdatesExt = WeakUpdatesExtension(system)

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
      weakUpdatesExt.subscribe(authId, self) onFailure {
        case e ⇒
          self ! SubscribeToWeak
          log.error(e, "Failed to subscribe to weak updates")
      }
    case cmd @ SubscribeToUserPresences(userIds) ⇒
      userIds foreach { userId ⇒
        presenceExt.subscribe(userId, self) onFailure {
          case e ⇒
            self ! cmd
            log.error(e, "Failed to subscribe to user presences")
        }
      }
    case cmd @ UnsubscribeFromUserPresences(userIds) ⇒
      userIds foreach { userId ⇒
        presenceExt.unsubscribe(userId, self) onFailure {
          case e ⇒
            self ! cmd
            log.error(e, "Failed to subscribe from user presences")
        }
      }
    case cmd @ SubscribeToGroupPresences(groupIds) ⇒
      groupIds foreach { groupId ⇒
        groupRresenceExt.subscribe(groupId, self) onFailure {
          case e ⇒
            self ! cmd
            log.error(e, "Failed to subscribe to group presences")
        }
      }
    case cmd @ UnsubscribeFromGroupPresences(groupIds) ⇒
      groupIds foreach { groupId ⇒
        groupRresenceExt.unsubscribe(groupId, self) onFailure {
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
            DbExtension(context.system).db.run(persist.AuthIdRepo.findUserId(authId)) flatMap {
              case Some(userId) ⇒
                for {
                  (users, groups) ← getFatData(userId, userIds, groupIds)
                } yield {
                  FatSeqUpdate(updateBox.seq, updateBox.state, updateBox.updateHeader, updateBox.update, users.toVector, groups.toVector)
                }
              case None ⇒
                throw new Exception(s"Cannot find userId for authId ${authId}")
            }
        }) foreach (sendUpdateBox(_, None))
      }
    case WeakUpdatesManager.UpdateReceived(updateBox, reduceKey) ⇒
      sendUpdateBox(updateBox, reduceKey)
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
      val reduceKey = weakUpdatesExt.reduceKeyUser(update.header, userId)
      sendUpdateBox(updateBox, Some(reduceKey))
    case GroupPresenceState(groupId, onlineCount) ⇒
      val update = UpdateGroupOnline(groupId, onlineCount)

      log.debug("Pushing presence {}", update)

      val updateBox = WeakUpdate(nextDateTime().getMillis, update.header, update.toByteArray)
      val reduceKey = weakUpdatesExt.reduceKeyGroup(update.header, groupId)
      sendUpdateBox(updateBox, Some(reduceKey))
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

  private def sendUpdateBox(updateBox: ProtoUpdateBox, reduceKey: Option[String]): Unit =
    subscriber ! NewUpdate(UpdateBox(UpdateBoxCodec.encode(updateBox).require), reduceKey)

  private def getFatData(
    userId:      Int,
    fatUserIds:  Seq[Int],
    fatGroupIds: Seq[Int]
  )(implicit ec: ExecutionContext): Future[(Seq[ApiUser], Seq[ApiGroup])] = {
    for {
      groups ← Future.sequence(fatGroupIds map (GroupExtension(system).getApiStruct(_, userId)))
      groupMemberIds = groups.view.map(_.members.map(_.userId)).flatten
      users ← Future.sequence((fatUserIds ++ groupMemberIds).distinct map (UserExtension(system).getApiStruct(_, userId, authId)))
    } yield (users, groups)
  }
}