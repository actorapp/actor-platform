package im.actor.server.group

import java.time._
import scala.concurrent.duration._
import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

import akka.actor._
import akka.contrib.pattern.{ ClusterSharding, ShardRegion }
import akka.pattern.pipe
import akka.persistence.PersistentView
import akka.util.Timeout
import com.google.protobuf.ByteString
import org.joda.time.DateTime
import slick.dbio.DBIO
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc.groups._
import im.actor.api.rpc.messaging.{ Message ⇒ ApiMessage, _ }
import im.actor.api.rpc.peers.{ Peer, PeerType }
import im.actor.server.commons.serialization.ActorSerializer
import im.actor.server.models.UserState.Registered
import im.actor.server.office.group.{ GroupEnvelope, GroupEvents }
import im.actor.server.office.user.UserEvent
import im.actor.server.office.{ PubSub, PeerOffice, PushTexts }
import im.actor.server.push.{ SeqUpdatesManager, SeqUpdatesManagerRegion }
import im.actor.server.sequence.SeqState
import im.actor.server.user.{ UserOfficeRegion, UserOffice }
import im.actor.server.util.ACLUtils._
import im.actor.server.util.IdUtils._
import im.actor.server.util.{ GroupServiceMessages, HistoryUtils, UserUtils }
import im.actor.server.{ models, persist ⇒ p }

case class GroupOfficeRegion(ref: ActorRef)

object GroupOffice extends GroupOperations {

  sealed trait MemberOperation

  private case class JoinedUser(user: Int) extends MemberOperation

  private case class InvitedUser(user: Int) extends MemberOperation

  private case class RemovedUser(userId: Int) extends MemberOperation

  private case object OperationFailure extends MemberOperation

  ActorSerializer.register(4001, classOf[GroupEvents.MessageRead])
  ActorSerializer.register(4002, classOf[GroupEvents.MessageReceived])
  ActorSerializer.register(4003, classOf[GroupEvents.UserInvited])
  ActorSerializer.register(4004, classOf[GroupEvents.UserJoined])
  ActorSerializer.register(4005, classOf[GroupEvents.Created])

  private case class Initialized(groupUsersIds: Set[Int], invitedUsersIds: Set[Int], isPublic: Boolean)

  private val idExtractor: ShardRegion.IdExtractor = {
    case GroupEnvelope(groupId, payload) ⇒ (groupId.toString, payload)
  }

  private val shardResolver: ShardRegion.ShardResolver = msg ⇒ msg match {
    case GroupEnvelope(groupId, _) ⇒ (groupId % 100).toString // TODO: configurable
  }

  private def startRegion(props: Option[Props])(implicit system: ActorSystem): GroupOfficeRegion =
    GroupOfficeRegion(ClusterSharding(system).start(
      typeName = "GroupOffice",
      entryProps = props,
      idExtractor = idExtractor,
      shardResolver = shardResolver
    ))

  def startRegion()(
    implicit
    system:              ActorSystem,
    db:                  Database,
    seqUpdManagerRegion: SeqUpdatesManagerRegion,
    userOfficeRegion:    UserOfficeRegion
  ): GroupOfficeRegion =
    startRegion(Some(props))

  def startRegionProxy()(implicit system: ActorSystem): GroupOfficeRegion =
    startRegion(None)

  def props(
    implicit
    db:                  Database,
    seqUpdManagerRegion: SeqUpdatesManagerRegion,
    userOfficeRegion:    UserOfficeRegion
  ): Props =
    Props(classOf[GroupOffice], db, seqUpdManagerRegion, userOfficeRegion)

  def topicFor(groupId: Int): String = s"group_${groupId}"
}

class GroupOffice(
  implicit
  db:                  Database,
  seqUpdManagerRegion: SeqUpdatesManagerRegion,
  userOfficeRegion:    UserOfficeRegion
) extends PeerOffice with ActorLogging with Stash with GroupsImplicits {

  import GroupEnvelope._
  import GroupOffice._
  import HistoryUtils._
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

      persist(Seq(GroupEvents.Created(creatorUserId, title)) ++ userIds.map(GroupEvents.UserInvited(_, creatorUserId, date.getMillis))) { _ ⇒
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
          accessHash = rng.nextLong(),
          title = title,
          isPublic = false,
          createdAt = date,
          description = ""
        )

        this.members = (userIds :+ creatorUserId).map(id ⇒ (id → Member(id, creatorUserId, date))).toMap

        val update = UpdateGroupInvite(groupId = groupId, inviteUserId = creatorUserId, date = date.getMillis, randomId = randomId)
        val serviceMessage = GroupServiceMessages.groupCreated

        val action =
          for {
            _ ← p.Group.create(group, randomId)
            _ ← p.GroupUser.create(group.id, this.members.keySet, creatorUserId, date, None)
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
            _ ← DBIO.sequence(userIds.map(userId ⇒ broadcastUserUpdate(userId, update, Some("You are invited to a group"))))
            seqstate ← broadcastClientUpdate(creatorUserId, creatorAuthId, update, None, isFat = true)
          } yield CreateResponse(group.accessHash, seqstate._1, ByteString.copyFrom(seqstate._2), date.getMillis)

        db.run(action) pipeTo sender()
      }
    case Payload.SendMessage(SendMessage(senderUserId, senderAuthId, randomId, date, message, isFat)) ⇒
      context.become {
        case MessageSentComplete ⇒
          unstashAll()
          context become receiveCommand
        case msg ⇒ stash()
      }
      val sendFuture = sendMessage(senderUserId, senderAuthId, members.keySet, randomId, new DateTime(date), message, isFat)
      val replyTo = sender()
      sendFuture onComplete {
        case Success(seqstate) ⇒
          replyTo ! seqstate
          self ! MessageSentComplete
        case Failure(e) ⇒
          replyTo ! Status.Failure(e)
          log.error(e, "Failed to send message")
          self ! MessageSentComplete
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
            db.run(markMessagesReceived(models.Peer.privat(receiverUserId), models.Peer.group(groupId), new DateTime(date)))
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
              self ! Payload.SendMessage(SendMessage(readerUserId, readerAuthId, randomId, System.currentTimeMillis, GroupServiceMessages.userJoined))
            })
          }

          val update = UpdateMessageRead(groupPeer, date, readDate)
          db.run(for {
            authIds ← p.AuthId.findIdByUserIds(members.keySet)
            _ ← persistAndPushUpdates(authIds.toSet, update, None)

          } yield {
            // TODO: report errors
            // TODO: Move to a History Writing subsystem
            db.run(markMessagesRead(models.Peer.privat(readerUserId), models.Peer.group(groupId), new DateTime(date)))
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

        val action: DBIO[SequenceStateDate] = {
          val memberIds = members.keySet

          if (!memberIds.contains(inviteeUserId)) {
            val inviteeUpdate = UpdateGroupInvite(groupId = groupId, randomId = randomId, inviteUserId = inviterUserId, date = dateMillis)

            val userAddedUpdate = UpdateGroupUserInvited(groupId = groupId, userId = inviteeUserId, inviterUserId = inviterUserId, date = dateMillis, randomId = randomId)
            val serviceMessage = GroupServiceMessages.userInvited(inviteeUserId)

            for {
              _ ← p.GroupUser.create(groupId, inviteeUserId, inviterUserId, date, None)
              _ ← broadcastUserUpdate(inviteeUserId, inviteeUpdate, pushText = Some(PushTexts.Invited), isFat = true)
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
        db.run(action) pipeTo replyTo onFailure {
          case e ⇒ replyTo ! Status.Failure(e)
        }

        addMember(inviteeUserId, inviterUserId, date)
        addInvitedUser(inviteeUserId)
      }
    case Payload.Join(Join(joiningUserId, joiningUserAuthId, invitingUserId)) ⇒
      val date = System.currentTimeMillis()

      persist(GroupEvents.UserJoined(joiningUserId, invitingUserId, date)) { _ ⇒
        val replyTo = sender()
        val action: DBIO[(SeqState, Vector[Sequence], Long, Long)] = {
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
                seqstate ← DBIO.from(sendMessage(joiningUserId, joiningUserAuthId, members.keySet, randomId, date, GroupServiceMessages.userJoined, isFat = true))
              } yield (seqstate, members.keySet.toVector :+ invitingUserId, dateMillis, randomId)
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
    case GroupEvents.Created(creatorUserId, title) ⇒
      this.creatorUserId = creatorUserId
      this.title = title
    case GroupEvents.MessageReceived(date) ⇒
      lastReceivedDate = Some(date)
    case GroupEvents.MessageRead(userId, date) ⇒
      lastReadDate = Some(date)
      invitedUserIds -= userId
    case GroupEvents.UserInvited(userId, inviterUserId, invitedAt) ⇒
      addMember(userId, inviterUserId, new DateTime(invitedAt))
    case GroupEvents.UserJoined(userId, inviterUserId, invitedAt) ⇒
      addMember(userId, inviterUserId, new DateTime(invitedAt))
  }

  private def addMember(userId: Int, inviterUserId: Int, invitedAt: DateTime): Member =
    addMember(Member(userId, inviterUserId, invitedAt))

  private def addMember(m: Member): Member = {
    members += (m.userId → m)
    m
  }

  private def addInvitedUser(userId: Int): Unit = {
    invitedUserIds += userId
  }

  private def removeMember(userId: Int): Unit = {
    members -= userId
    invitedUserIds -= userId
  }

  private def sendMessage(senderUserId: Int, senderAuthId: Long, groupUsersIds: Set[Int], randomId: Long, date: DateTime, message: ApiMessage, isFat: Boolean): Future[SeqState] = {
    members.keySet foreach { userId ⇒
      if (userId != senderUserId) {
        UserOffice.deliverGroupMessage(userId, groupId, senderUserId, randomId, date, message, isFat)
      }
    }

    UserOffice.deliverOwnGroupMessage(senderUserId, groupId, senderAuthId, randomId, date, message, isFat)
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