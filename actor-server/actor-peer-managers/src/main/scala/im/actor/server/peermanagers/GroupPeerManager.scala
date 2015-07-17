package im.actor.server.peermanagers

import java.time._

import scala.concurrent.duration._
import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.concurrent.{ ExecutionContext, Future }

import akka.actor._
import akka.contrib.pattern.{ ClusterSharding, ShardRegion }
import akka.pattern.pipe
import org.joda.time.DateTime
import slick.dbio.DBIO
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc.messaging.{ Message ⇒ ApiMessage }
import im.actor.api.rpc.groups._
import im.actor.api.rpc.messaging._
import im.actor.api.rpc.peers.{ Peer, PeerType }
import im.actor.server.push.{ SeqUpdatesManager, SeqUpdatesManagerRegion }
import im.actor.server.util.{ GroupServiceMessages, HistoryUtils, UserUtils }
import im.actor.server.{ models, persist }

case class GroupPeerManagerRegion(ref: ActorRef)

object GroupPeerManager extends GroupOperations {

  sealed trait MemberOperation
  private case class JoinedUser(user: Int) extends MemberOperation
  private case class InvitedUser(user: Int) extends MemberOperation
  private case class RemovedUser(userId: Int) extends MemberOperation
  private case object OperationFailure extends MemberOperation

  import PeerManager._

  private case class Initialized(groupUsersIds: Set[Int], invitedUsersIds: Set[Int], isPublic: Boolean)

  private val idExtractor: ShardRegion.IdExtractor = {
    case Envelope(groupId, payload) ⇒ (groupId.toString, payload)
  }

  private val shardResolver: ShardRegion.ShardResolver = msg ⇒ msg match {
    case Envelope(groupId, _) ⇒ (groupId % 100).toString // TODO: configurable
  }

  private def startRegion(props: Option[Props])(implicit system: ActorSystem): GroupPeerManagerRegion =
    GroupPeerManagerRegion(ClusterSharding(system).start(
      typeName = "GroupPeerManager",
      entryProps = props,
      idExtractor = idExtractor,
      shardResolver = shardResolver
    ))

  def startRegion()(
    implicit
    system:              ActorSystem,
    db:                  Database,
    seqUpdManagerRegion: SeqUpdatesManagerRegion
  ): GroupPeerManagerRegion =
    startRegion(Some(props))

  def startRegionProxy()(implicit system: ActorSystem): GroupPeerManagerRegion =
    startRegion(None)

  def props(
    implicit
    db:                  Database,
    seqUpdManagerRegion: SeqUpdatesManagerRegion
  ): Props =
    Props(classOf[GroupPeerManager], db, seqUpdManagerRegion)

}

class GroupPeerManager(
  implicit
  db:                  Database,
  seqUpdManagerRegion: SeqUpdatesManagerRegion
) extends PeerManager with Stash with GroupsImplicits {

  import GroupPeerManager._
  import HistoryUtils._
  import PeerManager._
  import SeqUpdatesManager._
  import UserUtils._

  implicit private[this] val system: ActorSystem = context.system
  implicit private[this] val ec: ExecutionContext = context.dispatcher

  private[this] val groupId = self.path.name.toInt
  private[this] val groupPeer = Peer(PeerType.Group, groupId)

  private[this] var lastReceivedDate: Option[Long] = None
  private[this] var lastReadDate: Option[Long] = None
  private[this] var lastMessageSenderId: Option[Int] = None

  context.setReceiveTimeout(15.minutes)

  initialize() pipeTo self onFailure {
    case e ⇒
      log.error(e, "Failed to initialize")
      self ! Kill
  }

  def receive = initializing

  def initializing: Receive = {
    case Initialized(joinedUserIds, invitedUsersIds, isPublic) ⇒
      context.become(initialized(joinedUserIds, invitedUsersIds, isPublic))
      unstashAll()
    case msg ⇒ stash()
  }

  /**
   * When somebody invites user, we put his id in `invitedUsersIds` and `groupUsersIds`.
   * When he reads message in this group at first time, he's being removed from `invitedUsersIds`
   * and set joinedAt to current time
   * @param groupUsersIds members of this group
   * @param invitedUsersIds invited users, who haven't opened group dialog yet
   * @param isPublic
   */
  private def initialized(groupUsersIds: Set[Int], invitedUsersIds: Set[Int], isPublic: Boolean): Receive = {
    case SendMessage(senderUserId, senderAuthId, randomId, date, message, isFat) ⇒
      val replyTo = sender()
      sendMessage(senderUserId, senderAuthId, groupUsersIds, randomId, date, message, isFat) pipeTo replyTo onFailure {
        case e ⇒
          replyTo ! Status.Failure(e)
          log.error(e, "Failed to send message")
      }
    case MessageReceived(receiverUserId, _, date, receivedDate) ⇒
      if (!lastReceivedDate.exists(_ >= date) && !lastMessageSenderId.contains(receiverUserId)) {
        lastReceivedDate = Some(date)
        val update = UpdateMessageReceived(groupPeer, date, receivedDate)

        db.run(for {
          otherAuthIds ← persist.AuthId.findIdByUserIds(groupUsersIds - receiverUserId)
          _ ← persistAndPushUpdates(otherAuthIds.toSet, update, None)
        } yield {
          // TODO: Move to a History Writing subsystem
          db.run(markMessagesReceived(models.Peer.privat(receiverUserId), models.Peer.group(groupId), new DateTime(date)))
        }) onFailure {
          case e ⇒
            log.error(e, "Failed to mark messages received")
        }
      }
    case MessageRead(readerUserId, readerAuthId, date, readDate) ⇒
      if (!lastReadDate.exists(_ >= date) && !lastMessageSenderId.contains(readerUserId)) {
        lastReadDate = Some(date)
        val update = UpdateMessageRead(groupPeer, date, readDate)
        val readerUpdate = UpdateMessageReadByMe(groupPeer, date)

        if (invitedUsersIds.contains(readerUserId)) {
          context.become(initialized(groupUsersIds, invitedUsersIds - readerUserId, isPublic))

          db.run(for (_ ← persist.GroupUser.setJoined(groupId, readerUserId, LocalDateTime.now(ZoneOffset.UTC))) yield {
            val randomId = ThreadLocalRandom.current().nextLong()
            self ! SendMessage(readerUserId, readerAuthId, randomId, new DateTime, GroupServiceMessages.userJoined)
          })
        }

        db.run(for {
          otherAuthIds ← persist.AuthId.findIdByUserIds(groupUsersIds - readerUserId)
          _ ← persistAndPushUpdates(otherAuthIds.toSet, update, None)
          _ ← broadcastUserUpdate(readerUserId, readerUpdate, None)
        } yield {
          // TODO: report errors
          // TODO: Move to a History Writing subsystem
          db.run(markMessagesRead(models.Peer.privat(readerUserId), models.Peer.group(groupId), new DateTime(date)))
        }) onFailure {
          case e ⇒
            log.error(e, "Failed to mark messages read")
        }
      }
    case JoinGroup(joiningUserId, joiningUserAuthId, invitingUserId) ⇒
      val replyTo = sender()
      val action: DBIO[(SequenceState, Vector[Sequence], Long, Long)] = {
        val isMember = groupUsersIds.contains(joiningUserId)
        for {
          updates ← if (isMember) {
            DBIO.failed(UserAlreadyJoined)
          } else {
            val date = new DateTime
            val dateMillis = date.getMillis
            val randomId = ThreadLocalRandom.current().nextLong()
            for {
              _ ← persist.GroupUser.create(groupId, joiningUserId, invitingUserId, date, Some(LocalDateTime.now(ZoneOffset.UTC)))
              seqstate ← DBIO.from(sendMessage(joiningUserId, joiningUserAuthId, groupUsersIds, randomId, date, GroupServiceMessages.userJoined, isFat = true))
            } yield (seqstate, groupUsersIds.toVector ++ Seq(joiningUserId, invitingUserId), dateMillis, randomId)
          }
        } yield {
          self ! JoinedUser(joiningUserId)
          updates
        }
      }
      db.run(action) pipeTo replyTo onFailure {
        case e ⇒ replyTo ! Status.Failure(e)
      }
    case InviteToGroup(group, inviteeUserId, client, randomId) ⇒
      implicit val ac = client
      val replyTo = sender()

      val action: DBIO[SequenceStateDate] = {
        persist.GroupUser.find(groupId).flatMap { groupUsers ⇒
          val userIds = groupUsers.map(_.userId)

          if (!userIds.contains(inviteeUserId)) {
            val date = new DateTime
            val dateMillis = date.getMillis

            val newGroupMembers = groupUsers.map(_.toMember) :+ Member(inviteeUserId, client.userId, dateMillis)

            val inviteeUserUpdates = Seq(
              UpdateGroupInvite(groupId = groupId, randomId = randomId, inviteUserId = client.userId, date = dateMillis),
              UpdateGroupTitleChanged(groupId = groupId, randomId = group.titleChangeRandomId, userId = group.titleChangerUserId, title = group.title, date = dateMillis),
              // TODO: put avatar here
              UpdateGroupAvatarChanged(groupId = groupId, randomId = group.avatarChangeRandomId, userId = group.avatarChangerUserId, avatar = None, date = dateMillis),
              UpdateGroupMembersUpdate(groupId = groupId, members = newGroupMembers.toVector)
            )

            val userAddedUpdate = UpdateGroupUserInvited(groupId = groupId, userId = inviteeUserId, inviterUserId = client.userId, date = dateMillis, randomId = randomId)
            val serviceMessage = GroupServiceMessages.userInvited(inviteeUserId)

            for {
              _ ← persist.GroupUser.create(groupId, inviteeUserId, client.userId, date, None)

              _ ← DBIO.sequence(inviteeUserUpdates map (broadcastUserUpdate(inviteeUserId, _, Some(PushTexts.Invited))))
              // TODO: #perf the following broadcasts do update serializing per each user
              _ ← DBIO.sequence(userIds.filterNot(_ == client.userId).map(broadcastUserUpdate(_, userAddedUpdate, Some(PushTexts.Added)))) // use broadcastUsersUpdate maybe?
              // TODO: Move to a History Writing subsystem
              seqstate ← broadcastClientUpdate(userAddedUpdate, None)
              _ ← HistoryUtils.writeHistoryMessage(
                models.Peer.privat(client.userId),
                models.Peer.group(groupId),
                date,
                randomId,
                serviceMessage.header,
                serviceMessage.toByteArray
              )
            } yield {
              self ! InvitedUser(inviteeUserId)
              (seqstate, dateMillis)
            }
          } else { DBIO.failed(UserAlreadyInvited) }
        }
      }
      db.run(action) pipeTo replyTo onFailure {
        case e ⇒ replyTo ! Status.Failure(e)
      }
    case KickUser(kickedUserId, client, randomId) ⇒
      implicit val ac = client
      val replyTo = sender()

      val date = new DateTime

      val update = UpdateGroupUserKick(groupId, kickedUserId, client.userId, date.getMillis, randomId)
      val serviceMessage = GroupServiceMessages.userKicked(kickedUserId)

      val action: DBIO[SequenceStateDate] = {
        for {
          _ ← persist.GroupUser.delete(groupId, kickedUserId)
          _ ← persist.GroupInviteToken.revoke(groupId, kickedUserId) //TODO: move to cleanup helper, with all cleanup code and use in kick/leave
          (seqstate, _) ← broadcastClientAndUsersUpdate(groupUsersIds - kickedUserId, update, Some(PushTexts.Kicked))
          // TODO: Move to a History Writing subsystem
          _ ← HistoryUtils.writeHistoryMessage(
            models.Peer.privat(client.userId),
            models.Peer.group(groupId),
            date,
            randomId,
            serviceMessage.header,
            serviceMessage.toByteArray
          )
        } yield {
          self ! RemovedUser(kickedUserId)
          (seqstate, date.getMillis)
        }
      }
      db.run(action) pipeTo replyTo onFailure {
        case e ⇒ replyTo ! Status.Failure(e)
      }
    case LeaveGroup(client, randomId) ⇒
      implicit val ac = client
      val replyTo = sender()
      val date = new DateTime

      val update = UpdateGroupUserLeave(groupId, client.userId, date.getMillis, randomId)
      val serviceMessage = GroupServiceMessages.userLeft(client.userId)

      val action: DBIO[SequenceStateDate] = {
        for {
          _ ← persist.GroupUser.delete(groupId, client.userId)
          _ ← persist.GroupInviteToken.revoke(groupId, client.userId) //TODO: move to cleanup helper, with all cleanup code and use in kick/leave
          (seqstate, _) ← broadcastClientAndUsersUpdate(groupUsersIds - client.userId, update, Some(PushTexts.Left))
          // TODO: Move to a History Writing subsystem
          _ ← HistoryUtils.writeHistoryMessage(
            models.Peer.privat(client.userId),
            models.Peer.group(groupId),
            date,
            randomId,
            serviceMessage.header,
            serviceMessage.toByteArray
          )
        } yield {
          self ! RemovedUser(client.userId)
          (seqstate, date.getMillis)
        }
      }
      db.run(action) pipeTo replyTo onFailure {
        case e ⇒ replyTo ! Status.Failure(e)
      }
    case RemovedUser(user) ⇒
      context become initialized(groupUsersIds - user, invitedUsersIds - user, isPublic) //remove from invitedUsersIds to be sure
    case JoinedUser(user) ⇒
      context become initialized(groupUsersIds + user, invitedUsersIds, isPublic)
    case InvitedUser(user) ⇒
      context become initialized(groupUsersIds + user, invitedUsersIds + user, isPublic)
    case ReceiveTimeout ⇒ context.parent ! ShardRegion.Passivate(stopMessage = PoisonPill)
  }

  private def sendMessage(senderUserId: Int, senderAuthId: Long, groupUsersIds: Set[Int], randomId: Long, date: DateTime, message: ApiMessage, isFat: Boolean): Future[SequenceState] = {
    lastMessageSenderId = Some(senderUserId)
    val outUpdate = UpdateMessage(
      peer = groupPeer,
      senderUserId = senderUserId,
      date = date.getMillis,
      randomId = randomId,
      message = message
    )
    val clientUpdate = UpdateMessageSent(groupPeer, randomId, date.getMillis)
    db.run {
      for {
        _ ← broadcastGroupMessage(senderUserId, senderAuthId, groupUsersIds, outUpdate, isFat)
        seqstate ← persistAndPushUpdate(senderAuthId, clientUpdate, None, isFat)
      } yield {
        db.run(writeHistoryMessage(models.Peer.privat(senderUserId), models.Peer.group(groupPeer.id), date, randomId, message.header, message.toByteArray))
        seqstate
      }
    }
  }

  private def initialize(): Future[Initialized] = {
    db.run(for {
      groupOpt ← persist.Group.find(groupId)
      groupUsers ← persist.GroupUser.find(groupId)
    } yield {
      val (groupUsersIds, invitedUsersIds) = groupUsers.foldLeft((Set.empty[Int], Set.empty[Int])) {
        case ((group, invited), groupUser) ⇒
          groupUser.joinedAt match {
            case Some(_) ⇒ (group + groupUser.userId, invited)
            case None    ⇒ (group + groupUser.userId, invited + groupUser.userId)
          }
      }

      groupOpt match {
        case Some(group) ⇒
          Initialized(groupUsersIds, invitedUsersIds, group.isPublic)
        case None ⇒
          throw new Exception(s"Cannot find group")
      }
    })
  }

  private def broadcastGroupMessage(senderUserId: Int, senderAuthId: Long, groupUsersIds: Set[Int], update: UpdateMessage, isFat: Boolean) = {
    val updateHeader = update.header
    val updateData = update.toByteArray
    val (updateUserIds, updateGroupIds) = updateRefs(update)

    for {
      clientUser ← getUserUnsafe(senderUserId)
      seqstates ← DBIO.sequence((groupUsersIds - senderUserId).toSeq map { userId ⇒
        for {
          pushText ← getPushText(update.message, clientUser, userId)
          seqstates ← broadcastUserUpdate(userId, updateHeader, updateData, updateUserIds, updateGroupIds, Some(pushText), Some(groupPeer), isFat)
        } yield seqstates
      }) map (_.flatten)
      selfseqstates ← notifyUserUpdate(senderUserId, senderAuthId, updateHeader, updateData, updateUserIds, updateGroupIds, None, None, isFat)
    } yield seqstates ++ selfseqstates
  }

}