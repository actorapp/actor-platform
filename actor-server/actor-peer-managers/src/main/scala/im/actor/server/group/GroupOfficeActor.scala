package im.actor.server.group

import java.time.{ LocalDateTime, ZoneOffset }

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration._
import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.util.{ Failure, Success }

import akka.actor._
import akka.contrib.pattern.ShardRegion
import akka.pattern.pipe
import akka.persistence.{ RecoveryCompleted, RecoveryFailure }
import akka.util.Timeout
import com.google.protobuf.ByteString
import com.trueaccord.scalapb.GeneratedMessage
import org.joda.time.DateTime
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc.groups.{ UpdateGroupInvite, UpdateGroupUserInvited, UpdateGroupUserKick, UpdateGroupUserLeave }
import im.actor.api.rpc.messaging.{ Message ⇒ ApiMessage, UpdateMessage, UpdateMessageRead, UpdateMessageReadByMe, UpdateMessageReceived }
import im.actor.api.rpc.peers.{ Peer, PeerType }
import im.actor.server.models.UserState.Registered
import im.actor.server.office.group.{ GroupEnvelope, GroupEvents }
import im.actor.server.office.{ PeerOffice, PushTexts }
import im.actor.server.push.{ SeqUpdatesManager, SeqUpdatesManagerRegion }
import im.actor.server.sequence.{ SeqState, SeqStateDate }
import im.actor.server.user.{ UserOffice, UserOfficeRegion }
import im.actor.server.util.ACLUtils._
import im.actor.server.util.IdUtils._
import im.actor.server.util.{ GroupServiceMessages, HistoryUtils, UserUtils }
import im.actor.server.{ models, persist ⇒ p }

object GroupOfficeActor {

  private case class Initialized(groupUsersIds: Set[Int], invitedUsersIds: Set[Int], isPublic: Boolean)

  def props(
    implicit
    db:                  Database,
    seqUpdManagerRegion: SeqUpdatesManagerRegion,
    userOfficeRegion:    UserOfficeRegion
  ): Props =
    Props(classOf[GroupOfficeActor], db, seqUpdManagerRegion, userOfficeRegion)
}

class GroupOfficeActor(
  implicit
  db:                  Database,
  seqUpdManagerRegion: SeqUpdatesManagerRegion,
  userOfficeRegion:    UserOfficeRegion
) extends PeerOffice with ActorLogging with Stash with GroupsImplicits {

  import GroupEnvelope._
  import GroupErrors._
  import GroupOfficeActor._
  import SeqUpdatesManager._
  import UserUtils._

  implicit private val system: ActorSystem = context.system
  implicit private val ec: ExecutionContext = context.dispatcher

  implicit private val timeout: Timeout = Timeout(10.seconds)

  private val groupId = self.path.name.toInt

  private val groupPeer = Peer(PeerType.Group, groupId)

  private var lastReceivedDate: Option[Long] = None
  private var lastReadDate: Option[Long] = None
  private var lastMessageSenderId: Option[Int] = None

  /**
   * When somebody invites user, we put his id in `invitedUsersIds` and `groupUsersIds`.
   * When he reads message in this group at first time, he's being removed from `invitedUsersIds`
   * and set joinedAt to current time
   */

  case class Member(userId: Int, inviterUserId: Int, invitedAt: DateTime) {
    lazy val asStruct = im.actor.api.rpc.groups.Member(userId, inviterUserId, invitedAt.getMillis)
  }

  private var members = Map.empty[Int, Member]
  private var invitedUserIds = Set.empty[Int]
  private var isPublic = false
  private var title = ""
  private var creatorUserId = 0
  private var accessHash = 0L

  override def persistenceId = s"group_${groupId}"

  context.setReceiveTimeout(15.minutes)

  /*
  TODO: turn into migration
  initialize() pipeTo self onFailure {
    case e ⇒
      log.error(e, "Failed to initialize")
      self ! Kill
  }

  def initializing: Receive = {
    case Initialized(joinedUserIds, invitedUsersIds, isPublic) ⇒
      context.become(initialized(joinedUserIds, invitedUsersIds, isPublic))
      unstashAll()
    case msg ⇒ stash()
  }
  */

  def receiveCommand: Receive = {
    //private def initialized(groupUsersIds: Set[Int], invitedUsersIds: Set[Int], isPublic: Boolean): Receive = {
    case Payload.Create(Create(creatorUserId, creatorAuthId, title, randomId, userIds)) ⇒
      val date = new DateTime
      val accessHash = ThreadLocalRandom.current().nextLong()

      val events = Vector(
        GroupEvents.Created(creatorUserId, accessHash, title)
      ) ++
        userIds.filterNot(_ == creatorUserId).map(GroupEvents.UserInvited(_, creatorUserId, date.getMillis))

      persist[GeneratedMessage](events) {
        case _: GroupEvents.Created ⇒
          this.title = title
          this.creatorUserId = creatorUserId

          val rng = ThreadLocalRandom.current()

          val bot = models.User(
            id = nextIntId(rng),
            accessSalt = nextAccessSalt(rng),
            name = "Bot",
            countryCode = "US",
            sex = models.NoSex,
            state = Registered,
            createdAt = LocalDateTime.now(ZoneOffset.UTC),
            isBot = true
          )
          val botToken = accessToken(rng)

          val group = models.Group(
            id = groupId,
            creatorUserId = creatorUserId,
            accessHash = accessHash,
            title = title,
            isPublic = false,
            createdAt = date,
            description = ""
          )

          addMember(creatorUserId, creatorUserId, date)

          val serviceMessage = GroupServiceMessages.groupCreated

          val update = UpdateGroupInvite(groupId = groupId, inviteUserId = creatorUserId, date = date.getMillis, randomId = randomId)

          db.run(
            for {
              _ ← p.Group.create(group, randomId)
              _ ← p.GroupUser.create(groupId, creatorUserId, creatorUserId, date, None)
              _ ← p.User.create(bot)
              _ ← p.GroupBot.create(group.id, bot.id, botToken)
              _ ← HistoryUtils.writeHistoryMessage(
                models.Peer.privat(creatorUserId),
                models.Peer.group(group.id),
                date,
                randomId,
                serviceMessage.header,
                serviceMessage.toByteArray
              )
              (seq, state) ← broadcastClientUpdate(creatorUserId, creatorAuthId, update, None, false)
            } yield CreateResponse(group.accessHash, seq, ByteString.copyFrom(state), date.getMillis)
          ) pipeTo sender()

        case GroupEvents.UserInvited(userId, _, _) ⇒
          invite(userId, creatorUserId, creatorAuthId, randomId, date)
      }
    case Payload.SendMessage(SendMessage(senderUserId, senderAuthId, accessHash, randomId, message, isFat)) ⇒
      if (hasMember(senderUserId)) {
        context.become {
          case MessageSentComplete ⇒
            unstashAll()
            context become receiveCommand
          case msg ⇒ stash()
        }

        val date = new DateTime
        val replyTo = sender()

        sendMessage(senderUserId, senderAuthId, members.keySet, randomId, date, message, isFat) onComplete {
          case Success(seqstatedate) ⇒
            replyTo ! seqstatedate
            self ! MessageSentComplete
          case Failure(e) ⇒
            replyTo ! Status.Failure(e)
            log.error(e, "Failed to send message")
            self ! MessageSentComplete
        }
      } else {
        sender() ! Status.Failure(NotAMember)
      }
    case Payload.MessageReceived(e @ MessageReceived(receiverUserId, _, date, receivedDate)) ⇒
      if (!lastReceivedDate.exists(_ >= date) && !lastMessageSenderId.contains(receiverUserId)) {
        persist(GroupEvents.MessageReceived(date)) { _ ⇒
          lastReceivedDate = Some(date)
          val update = UpdateMessageReceived(groupPeer, date, receivedDate)

          db.run(for {
            otherAuthIds ← p.AuthId.findIdByUserIds(members.keySet - receiverUserId)
            _ ← persistAndPushUpdates(otherAuthIds.toSet, update, None)
          } yield {
            // TODO: Move to a History Writing subsystem
            // db.run(markMessagesReceived(models.Peer.privat(receiverUserId), models.Peer.group(groupId), new DateTime(date)))
          }) onFailure {
            case e ⇒
              log.error(e, "Failed to mark messages received")
          }
        }
      }
    case Payload.MessageRead(e @ MessageRead(readerUserId, readerAuthId, date, readDate)) ⇒
      db.run(broadcastOtherDevicesUpdate(readerUserId, readerAuthId, UpdateMessageReadByMe(groupPeer, date), None))

      if (!lastReadDate.exists(_ >= date) && !lastMessageSenderId.contains(readerUserId)) {
        persist(GroupEvents.MessageRead(readerUserId, date)) { _ ⇒
          lastReadDate = Some(date)

          if (invitedUserIds.contains(readerUserId)) {
            invitedUserIds -= readerUserId

            db.run(for (_ ← p.GroupUser.setJoined(groupId, readerUserId, LocalDateTime.now(ZoneOffset.UTC))) yield {
              val randomId = ThreadLocalRandom.current().nextLong()
              self ! Payload.SendMessage(SendMessage(readerUserId, readerAuthId, accessHash, randomId, GroupServiceMessages.userJoined))
            })
          }

          val update = UpdateMessageRead(groupPeer, date, readDate)
          db.run(for {
            authIds ← p.AuthId.findIdByUserIds(members.keySet)
            _ ← persistAndPushUpdates(authIds.toSet, update, None)

          } yield {
            // TODO: report errors
            // TODO: Move to a History Writing subsystem
            // db.run(markMessagesRead(models.Peer.privat(readerUserId), models.Peer.group(groupId), new DateTime(date)))
          }) onFailure {
            case e ⇒
              log.error(e, "Failed to mark messages read")
          }
        }
      }
    case Payload.Invite(e @ Invite(inviteeUserId, inviterUserId, inviterAuthId, randomId)) ⇒
      val dateMillis = System.currentTimeMillis()

      persist(GroupEvents.UserInvited(inviteeUserId, inviterUserId, dateMillis)) { _ ⇒
        val replyTo = sender()
        val date = new DateTime(dateMillis)

        invite(inviteeUserId, inviterUserId, inviterAuthId, randomId, date) pipeTo replyTo onFailure {
          case e ⇒ replyTo ! Status.Failure(e)
        }
      }
    case Payload.Join(Join(joiningUserId, joiningUserAuthId, invitingUserId)) ⇒
      val date = System.currentTimeMillis()

      persist(GroupEvents.UserJoined(joiningUserId, invitingUserId, date)) { _ ⇒
        val replyTo = sender()
        val action: DBIO[(SeqStateDate, Vector[Sequence], Long)] = {
          val isMember = members.contains(joiningUserId)

          // TODO: Move to view
          for {
            updates ← if (isMember) {
              DBIO.failed(UserAlreadyJoined)
            } else {
              val date = new DateTime
              val dateMillis = date.getMillis
              val randomId = ThreadLocalRandom.current().nextLong()
              for {
                _ ← p.GroupUser.create(groupId, joiningUserId, invitingUserId, date, Some(LocalDateTime.now(ZoneOffset.UTC)))
                seqstatedate ← DBIO.from(sendMessage(joiningUserId, joiningUserAuthId, members.keySet, randomId, date, GroupServiceMessages.userJoined, isFat = true))
              } yield (seqstatedate, members.keySet.toVector :+ invitingUserId, randomId)
            }
          } yield {
            updates
          }
        }
        db.run(action) pipeTo replyTo onFailure {
          case e ⇒ replyTo ! Status.Failure(e)
        }

        addMember(joiningUserId, invitingUserId, new DateTime(date))
      }
    case Payload.Kick(Kick(kickedUserId, kickerUserId, kickerAuthId, randomId)) ⇒
      val replyTo = sender()

      val date = new DateTime

      val update = UpdateGroupUserKick(groupId, kickedUserId, kickerUserId, date.getMillis, randomId)
      val serviceMessage = GroupServiceMessages.userKicked(kickedUserId)

      val action: DBIO[SequenceStateDate] = {
        for {
          _ ← p.GroupUser.delete(groupId, kickedUserId)
          _ ← p.GroupInviteToken.revoke(groupId, kickedUserId) //TODO: move to cleanup helper, with all cleanup code and use in kick/leave
          (seqstate, _) ← broadcastClientAndUsersUpdate(kickerUserId, kickerAuthId, members.keySet - kickedUserId, update, Some(PushTexts.Kicked), isFat = false)
          // TODO: Move to a History Writing subsystem
          _ ← HistoryUtils.writeHistoryMessage(
            models.Peer.privat(kickerUserId),
            models.Peer.group(groupId),
            date,
            randomId,
            serviceMessage.header,
            serviceMessage.toByteArray
          )
        } yield {
          (seqstate, date.getMillis)
        }
      }
      db.run(action) pipeTo replyTo onFailure {
        case e ⇒ replyTo ! Status.Failure(e)
      }

      removeMember(kickedUserId)
    case Payload.Leave(Leave(userId, authId, randomId)) ⇒
      val replyTo = sender()
      val date = new DateTime

      val update = UpdateGroupUserLeave(groupId, userId, date.getMillis, randomId)
      val serviceMessage = GroupServiceMessages.userLeft(userId)

      val action: DBIO[SequenceStateDate] = {
        for {
          _ ← p.GroupUser.delete(groupId, userId)
          _ ← p.GroupInviteToken.revoke(groupId, userId) //TODO: move to cleanup helper, with all cleanup code and use in kick/leave
          (seqstate, _) ← broadcastClientAndUsersUpdate(userId, authId, (members.keySet - userId), update, Some(PushTexts.Left), isFat = false)
          // TODO: Move to a History Writing subsystem
          _ ← HistoryUtils.writeHistoryMessage(
            models.Peer.privat(userId),
            models.Peer.group(groupId),
            date,
            randomId,
            serviceMessage.header,
            serviceMessage.toByteArray
          )
        } yield {
          (seqstate, date.getMillis)
        }
      }
      db.run(action) pipeTo replyTo onFailure {
        case e ⇒ replyTo ! Status.Failure(e)
      }

      removeMember(userId)
    case ReceiveTimeout ⇒ context.parent ! ShardRegion.Passivate(stopMessage = PoisonPill)
  }

  override def receiveRecover = {
    case GroupEvents.Created(creatorUserId, accessHash, title) ⇒
      this.creatorUserId = creatorUserId
      this.title = title
      this.accessHash = accessHash
    case GroupEvents.MessageReceived(date) ⇒
      lastReceivedDate = Some(date)
    case GroupEvents.MessageRead(userId, date) ⇒
      lastReadDate = Some(date)
      invitedUserIds -= userId
    case e @ GroupEvents.UserInvited(userId, inviterUserId, invitedAt) ⇒
      log.warning("Recover: {}", e)
      addMember(userId, inviterUserId, new DateTime(invitedAt))
    case e @ GroupEvents.UserJoined(userId, inviterUserId, invitedAt) ⇒
      log.warning("Recover: {}", e)
      addMember(userId, inviterUserId, new DateTime(invitedAt))
    case RecoveryFailure(e) ⇒
      log.error(e, "Failed to recover")
    case RecoveryCompleted ⇒
    case unmatched ⇒
      log.error("Unmatched recovery event {}", unmatched)
  }

  private def addMember(userId: Int, inviterUserId: Int, invitedAt: DateTime): Member =
    addMember(Member(userId, inviterUserId, invitedAt))

  private def addMember(m: Member): Member = {
    members += (m.userId → m)
    m
  }

  private def hasMember(userId: Int): Boolean = members.keySet.contains(userId)

  private def addInvitedUser(userId: Int): Unit = {
    invitedUserIds += userId
  }

  private def removeMember(userId: Int): Unit = {
    members -= userId
    invitedUserIds -= userId
  }

  private def sendMessage(senderUserId: Int, senderAuthId: Long, groupUsersIds: Set[Int], randomId: Long, date: DateTime, message: ApiMessage, isFat: Boolean): Future[SeqStateDate] = {
    log.warning(s"Sending message to {}", members)

    members.keySet foreach { userId ⇒
      if (userId != senderUserId) {
        UserOffice.deliverGroupMessage(userId, groupId, senderUserId, randomId, date, message, isFat)
      }
    }

    UserOffice.deliverOwnGroupMessage(senderUserId, groupId, senderAuthId, randomId, date, message, isFat) map {
      case SeqState(seq, state) ⇒ SeqStateDate(seq, state, date.getMillis)
    }
  }

  private def invite(userId: Int, inviterUserId: Int, inviterAuthId: Long, randomId: Long, date: DateTime): Future[SequenceStateDate] = {
    val dateMillis = date.getMillis
    val memberIds = members.keySet
    addMember(userId, inviterUserId, date)
    addInvitedUser(userId)

    db.run {
      if (!memberIds.contains(userId)) {
        val inviteeUpdate = UpdateGroupInvite(groupId = groupId, randomId = randomId, inviteUserId = inviterUserId, date = dateMillis)

        val userAddedUpdate = UpdateGroupUserInvited(groupId = groupId, userId = userId, inviterUserId = inviterUserId, date = dateMillis, randomId = randomId)
        val serviceMessage = GroupServiceMessages.userInvited(userId)

        for {
          _ ← p.GroupUser.create(groupId, userId, inviterUserId, date, None)
          _ ← broadcastUserUpdate(userId, inviteeUpdate, pushText = Some(PushTexts.Invited), isFat = true)
          // TODO: #perf the following broadcasts do update serializing per each user
          _ ← DBIO.sequence(memberIds.toSeq.filterNot(_ == inviterUserId).map(broadcastUserUpdate(_, userAddedUpdate, Some(PushTexts.Added), isFat = true))) // use broadcastUsersUpdate maybe?
          seqstate ← broadcastClientUpdate(inviterUserId, inviterAuthId, userAddedUpdate, pushText = None, isFat = true)
          // TODO: Move to a History Writing subsystem
          _ ← HistoryUtils.writeHistoryMessage(
            models.Peer.privat(inviterUserId),
            models.Peer.group(groupId),
            date,
            randomId,
            serviceMessage.header,
            serviceMessage.toByteArray
          )
        } yield {
          (seqstate, dateMillis)
        }
      } else {
        DBIO.failed(UserAlreadyInvited)
      }
    }
  }

  private def initialize(): Future[Initialized] = {
    db.run(for {
      groupOpt ← p.Group.find(groupId)
      groupUsers ← p.GroupUser.find(groupId)
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