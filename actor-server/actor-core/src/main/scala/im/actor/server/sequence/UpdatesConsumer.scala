package im.actor.server.sequence

import akka.actor._
import akka.http.scaladsl.util.FastFuture
import akka.pattern.pipe
import akka.util.Timeout
import im.actor.api.rpc.codecs.UpdateBoxCodec
import im.actor.api.rpc.groups.ApiGroup
import im.actor.api.rpc.sequence.{ SeqUpdate, FatSeqUpdate, WeakUpdate }
import im.actor.api.rpc.users.ApiUser
import im.actor.api.rpc.weak.{ UpdateGroupOnline, UpdateUserLastSeen, UpdateUserOffline, UpdateUserOnline }
import im.actor.api.rpc.{ Update, UpdateBox ⇒ ProtoUpdateBox }
import im.actor.server.db.DbExtension
import im.actor.server.group.GroupExtension
import im.actor.server.model.configs.Parameter
import im.actor.server.mtproto.protocol.ProtoPush
import im.actor.server.persist.configs.ParameterRepo
import im.actor.server.persist.contact.UserContactRepo
import im.actor.server.presences._
import im.actor.server.user.UserExtension
import org.joda.time.DateTime
import slick.dbio.DBIO

import scala.concurrent._
import scala.concurrent.duration._

final case class NewUpdate(ub: ProtoUpdateBox, reduceKey: Option[String])

sealed trait UpdatesConsumerMessage

object UpdatesConsumerMessage {

  @SerialVersionUID(1L)
  case object SubscribeToSeq extends UpdatesConsumerMessage

  @SerialVersionUID(1L)
  final case class SubscribeToWeak(group: Option[String]) extends UpdatesConsumerMessage

  @SerialVersionUID(1L)
  final case class SubscribeToUserPresences(userIds: Set[Int]) extends UpdatesConsumerMessage

  @SerialVersionUID(1L)
  final case class UnsubscribeFromUserPresences(userIds: Set[Int]) extends UpdatesConsumerMessage

  @SerialVersionUID(1L)
  final case class SubscribeToGroupPresences(groupIds: Set[Int]) extends UpdatesConsumerMessage

  @SerialVersionUID(1L)
  final case class UnsubscribeFromGroupPresences(groupIds: Set[Int]) extends UpdatesConsumerMessage

}

object UpdatesConsumer {
  def props(userId: Int, authId: Long, authSid: Int, session: ActorRef) =
    Props(classOf[UpdatesConsumer], userId, authId, authSid, session)
}

private[sequence] class UpdatesConsumer(userId: Int, authId: Long, authSid: Int, subscriber: ActorRef) extends Actor with ActorLogging with Stash {

  import Presences._
  import UpdatesConsumerMessage._

  implicit val ec: ExecutionContext = context.dispatcher
  implicit val system: ActorSystem = context.system
  implicit val timeout: Timeout = Timeout(5.seconds) // TODO: configurable
  private val presenceExt = PresenceExtension(system)
  private val groupRresenceExt = GroupPresenceExtension(system)
  private val weakUpdatesExt = WeakUpdatesExtension(system)
  private val db = DbExtension(system).db

  private implicit val seqUpdExt: SeqUpdatesExtension = SeqUpdatesExtension(context.system)
  private var lastDateTime = new DateTime
  private var subscribedToSeq: Boolean = false

  override def preStart(): Unit = {
    self ! SubscribeToWeak(None)
  }

  def receive = {
    case SubscribeToSeq ⇒
      if (!subscribedToSeq) {
        context become {
          case () ⇒
            this.subscribedToSeq = true
            unstashAll()
            context become receive
          case Status.Failure(e) ⇒
            log.error(e, "Failed to subscribe to seq")
            self ! SubscribeToSeq
            unstashAll()
            context become receive
          case msg ⇒ stash()
        }

        seqUpdExt.subscribe(userId, self) pipeTo self
      }
    case SubscribeToWeak(None) ⇒
      weakUpdatesExt.subscribe(authId, self, None) onFailure {
        case e ⇒
          self ! SubscribeToWeak(None)
          log.error(e, "Failed to subscribe to weak updates")
      }
    case SubscribeToWeak(Some(group)) ⇒
      weakUpdatesExt.subscribe(authId, self, Some(group)) onFailure {
        case e ⇒
          self ! SubscribeToWeak(Some(group))
          log.error(e, "Failed to subscribe to weak updates, group: {}", group)
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
    case UserSequenceEvents.NewUpdate(Some(seqUpd), pushRulesOpt, reduceKey, state) ⇒
      val pushRules = pushRulesOpt.getOrElse(PushRules())

      if (!pushRules.excludeAuthSids.contains(authSid)) {
        val upd = seqUpd.getMapping.custom.getOrElse(authSid, seqUpd.getMapping.getDefault)

        val boxFuture =
          if (pushRules.isFat) {
            for {
              (users, groups) ← getFatData(userId, upd.userIds, upd.groupIds)
            } yield FatSeqUpdate(seqUpd.seq, state.toByteArray, upd.header, upd.body.toByteArray, users.toVector, groups.toVector)
          } else Future.successful(SeqUpdate(seqUpd.seq, state.toByteArray, upd.header, upd.body.toByteArray))

        val msgBase = "Pushing SeqUpdate, seq: {}, header: {}"

        // TODO: unify for all UpdateBoxes
        boxFuture foreach {
          case FatSeqUpdate(seq, _, _, _, users, groups) ⇒
            log.debug(s"$msgBase, users: {}, groups: {}", seqUpd.seq, upd.header, users, groups)
          case SeqUpdate(seq, _, _, _) ⇒
            log.debug(msgBase, seqUpd.seq, upd)
          case _ ⇒ // should never happen
            log.error("Improper seq update box")
        }

        boxFuture foreach (sendUpdateBox(_, reduceKey map (_.value)))

        boxFuture onFailure {
          case e: Throwable ⇒ log.error(e, "Failed to push update")
        }
      }
    case WeakUpdatesManager.UpdateReceived(updateBox, reduceKey) ⇒
      sendUpdateBox(updateBox, reduceKey)
    case p @ PresenceState(_, presence, lastSeenAt) ⇒
      val updateFuture: Future[Update] =
        presence match {
          case Online ⇒
            FastFuture.successful(UpdateUserOnline(p.userId))
          case Offline ⇒
            lastSeenAt match {
              case Some(date) ⇒
                lastSeenOrOffline(p.userId, date.getMillis / 1000)
              case None ⇒
                FastFuture.successful(UpdateUserOffline(p.userId))
            }
        }

      updateFuture foreach { update ⇒
        log.debug("Pushing presence {}", update)

        val updateBox = WeakUpdate(nextDateTime().getMillis, update.header, update.toByteArray)
        val reduceKey = weakUpdatesExt.reduceKeyUser(update.header, p.userId)
        sendUpdateBox(updateBox, Some(reduceKey))
      }
    case GroupPresenceState(groupId, onlineCount) ⇒
      val update = UpdateGroupOnline(groupId, onlineCount)

      log.debug("Pushing presence {}", update)

      val updateBox = WeakUpdate(nextDateTime().getMillis, update.header, update.toByteArray)
      val reduceKey = weakUpdatesExt.reduceKeyGroup(update.header, groupId)
      sendUpdateBox(updateBox, Some(reduceKey))
  }

  private def lastSeenOrOffline(presenceUserId: Int, tsSeconds: Long): Future[Update] = {
    db.run {
      for {
        selfCanLastSeen ← ParameterRepo.findValue(userId, Parameter.Keys.Privacy.LastSeen, Parameter.Values.Privacy.LastSeen.Always.value)
        userCanLastSeen ← ParameterRepo.findValue(presenceUserId, Parameter.Keys.Privacy.LastSeen, Parameter.Values.Privacy.LastSeen.Always.value)
        update ← if (selfCanLastSeen == Parameter.Values.Privacy.LastSeen.None.value ||
          userCanLastSeen == Parameter.Values.Privacy.LastSeen.None.value) {
          DBIO.successful(UpdateUserOffline(presenceUserId))
        } else if (selfCanLastSeen == Parameter.Values.Privacy.LastSeen.Contacts.value ||
          userCanLastSeen == Parameter.Values.Privacy.LastSeen.Contacts.value) {
          for {
            isInContacts ← UserContactRepo.exists(presenceUserId, userId)
          } yield {
            if (isInContacts)
              UpdateUserLastSeen(presenceUserId, tsSeconds)
            else
              UpdateUserOffline(presenceUserId)
          }
        } else DBIO.successful(UpdateUserLastSeen(presenceUserId, tsSeconds))
      } yield update
    }
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
    subscriber ! NewUpdate(updateBox, reduceKey)

  private def getFatData(
    userId:      Int,
    fatUserIds:  Seq[Int],
    fatGroupIds: Seq[Int]
  )(implicit ec: ExecutionContext): Future[(Seq[ApiUser], Seq[ApiGroup])] = {
    for {
      groups ← Future.sequence(fatGroupIds map (GroupExtension(system).getApiStruct(_, userId)))
      groupMemberIds = groups.view.flatMap(_.members.map(_.userId))
      users ← Future.sequence((fatUserIds ++ groupMemberIds).distinct map (UserExtension(system).getApiStruct(_, userId, authId)))
    } yield (users, groups)
  }
}