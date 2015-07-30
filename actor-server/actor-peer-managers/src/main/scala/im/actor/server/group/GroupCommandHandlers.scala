package im.actor.server.group

import java.time.{ LocalDateTime, ZoneOffset }

import scala.concurrent.Future
import scala.concurrent.forkjoin.ThreadLocalRandom

import akka.actor.Status
import akka.pattern.pipe
import akka.util.Timeout
import com.trueaccord.scalapb.GeneratedMessage
import org.joda.time.DateTime
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc.groups._
import im.actor.api.rpc.messaging.{ Message ⇒ ApiMessage, UpdateMessageRead, UpdateMessageReadByMe, UpdateMessageReceived }
import im.actor.server.api.ApiConversions._
import im.actor.server.file.Avatar
import im.actor.server.models.UserState.Registered
import im.actor.server.office.PushTexts
import im.actor.server.push.SeqUpdatesManager._
import im.actor.server.push.SeqUpdatesManagerRegion
import im.actor.server.sequence.{ SeqState, SeqStateDate }
import im.actor.server.user.{ UserOffice, UserOfficeRegion }
import im.actor.server.util.ACLUtils._
import im.actor.server.util.HistoryUtils._
import im.actor.server.util.IdUtils._
import im.actor.server.util.ImageUtils._
import im.actor.server.util.{ FileStorageAdapter, GroupServiceMessages, HistoryUtils }
import im.actor.server.{ models, persist ⇒ p }
import im.actor.utils.cache.CacheHelpers._

private[group] trait GroupCommandHandlers {
  this: GroupOfficeActor ⇒

  import GroupCommands._
  import GroupEvents._

  protected def create(groupId: Int, creatorUserId: Int, creatorAuthId: Long, title: String, randomId: Long, userIds: Set[Int]): Unit = {
    val date = new DateTime

    val rng = ThreadLocalRandom.current()

    val accessHash = rng.nextLong()
    val botUserId = nextIntId(rng)
    val botToken = accessToken(rng)

    val events = Vector(
      GroupEvents.Created(creatorUserId, accessHash, title),
      GroupEvents.BotAdded(botUserId, botToken)
    )

    userIds.filterNot(_ == creatorUserId) foreach { userId ⇒
      val randomId = rng.nextLong()
      context.parent ! Invite(groupId, userId, creatorUserId, creatorAuthId, randomId)
    }

    var stateMaybe: Option[Group] = None

    persist[GeneratedMessage](events) {
      case evt: GroupEvents.Created ⇒
        val group = initState(evt)

        stateMaybe = Some(group)

        val serviceMessage = GroupServiceMessages.groupCreated

        val update = UpdateGroupInvite(groupId = groupId, inviteUserId = creatorUserId, date = date.getMillis, randomId = randomId)

        db.run(
          for {
            _ ← p.Group.create(
              models.Group(groupId, group.creatorUserId, group.accessHash, group.title, group.isPublic, group.createdAt, ""),
              randomId
            )
            _ ← p.GroupUser.create(groupId, creatorUserId, creatorUserId, date, None)
            _ ← HistoryUtils.writeHistoryMessage(
              models.Peer.privat(creatorUserId),
              models.Peer.group(group.id),
              date,
              randomId,
              serviceMessage.header,
              serviceMessage.toByteArray
            )
            SeqState(seq, state) ← broadcastClientUpdate(creatorUserId, creatorAuthId, update, None, false)
          } yield CreateAck(group.accessHash, seq, state, date.getMillis)
        ) pipeTo sender()

      case evt @ GroupEvents.BotAdded(userId, token) ⇒
        stateMaybe = stateMaybe map { state ⇒
          val newState = updateState(evt, state)
          context become working(newState)
          newState
        }

        val rng = ThreadLocalRandom.current()

        val bot = models.User(
          id = userId,
          accessSalt = nextAccessSalt(rng),
          name = "Bot",
          countryCode = "US",
          sex = models.NoSex,
          state = Registered,
          createdAt = LocalDateTime.now(ZoneOffset.UTC),
          isBot = true
        )

        db.run(DBIO.sequence(Seq(
          p.User.create(bot),
          p.GroupBot.create(groupId, bot.id, token)
        )))
    }
  }

  protected def sendMessage(group: Group, senderUserId: Int, senderAuthId: Long, groupUsersIds: Set[Int], randomId: Long, date: DateTime, message: ApiMessage, isFat: Boolean): Future[SeqStateDate] = {
    val groupPeer = groupPeerStruct(groupId)
    val memberIds = group.members.keySet

    withCachedFuture[AuthIdRandomId, SeqStateDate](senderAuthId → randomId) { () ⇒
      memberIds foreach { userId ⇒
        if (userId != senderUserId) {
          UserOffice.deliverMessage(userId, groupPeer, senderUserId, randomId, date, message, isFat)
        }
      }

      for {
        SeqState(seq, state) ← UserOffice.deliverOwnMessage(senderUserId, groupPeer, senderAuthId, randomId, date, message, isFat)
      } yield {
        db.run(writeHistoryMessage(models.Peer.privat(senderUserId), models.Peer.group(groupPeer.id), date, randomId, message.header, message.toByteArray))
        SeqStateDate(seq, state, date.getMillis)
      }
    }
  }

  protected def messageReceived(group: Group, receiverUserId: Int, date: Long, receivedDate: Long): Unit = {
    if (!group.lastReceivedDate.exists(_.getMillis >= date) && !group.lastSenderId.contains(receiverUserId)) {
      persist(GroupEvents.MessageReceived(date)) { evt ⇒
        context become working(updateState(evt, group))

        val update = UpdateMessageReceived(groupPeerStruct(groupId), date, receivedDate)

        db.run(for {
          otherAuthIds ← p.AuthId.findIdByUserIds(group.members.keySet - receiverUserId)
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
  }

  protected def messageRead(group: Group, readerUserId: Int, readerAuthId: Long, date: Long, readDate: Long): Unit = {
    db.run(broadcastOtherDevicesUpdate(readerUserId, readerAuthId, UpdateMessageReadByMe(groupPeerStruct(groupId), date), None))

    if (!group.lastReadDate.exists(_.getMillis >= date) && !group.lastSenderId.contains(readerUserId)) {
      persist(GroupEvents.MessageRead(readerUserId, date)) { evt ⇒
        context become working(updateState(evt, group))

        if (group.invitedUserIds.contains(readerUserId)) {

          db.run(for (_ ← p.GroupUser.setJoined(groupId, readerUserId, LocalDateTime.now(ZoneOffset.UTC))) yield {
            val randomId = ThreadLocalRandom.current().nextLong()
            self ! SendMessage(groupId, readerUserId, readerAuthId, group.accessHash, randomId, GroupServiceMessages.userJoined)
          })
        }

        val update = UpdateMessageRead(groupPeerStruct(groupId), date, readDate)
        val memberIds = group.members.keySet

        db.run(p.AuthId.findIdByUserIds(memberIds) flatMap { authIds ⇒
          persistAndPushUpdates(authIds.toSet, update, None) andThen
            markMessagesRead(models.Peer.privat(readerUserId), models.Peer.group(groupId), new DateTime(date))
        }) onFailure {
          case e ⇒
            log.error(e, "Failed to mark messages read")
        }
      }
    }
  }

  protected def invite(group: Group, userId: Int, inviterUserId: Int, inviterAuthId: Long, randomId: Long, date: DateTime): Future[SeqStateDate] = {
    val dateMillis = date.getMillis
    val memberIds = group.members.keySet

    db.run {
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
        SeqStateDate(seqstate.seq, seqstate.state, dateMillis)
      }
    }
  }

  protected def join(group: Group, joiningUserId: Int, joiningUserAuthId: Long, invitingUserId: Int): Unit = {
    if (!hasMember(group, joiningUserId)) {
      val replyTo = sender()
      val date = System.currentTimeMillis()
      persist(GroupEvents.UserJoined(joiningUserId, invitingUserId, date)) { evt ⇒
        context become working(updateState(evt, group))

        val memberIds = group.members.keySet

        val action: DBIO[(SeqStateDate, Vector[Int], Long)] = {
          for {
            updates ← {
              val date = new DateTime
              val randomId = ThreadLocalRandom.current().nextLong()
              for {
                _ ← p.GroupUser.create(groupId, joiningUserId, invitingUserId, date, Some(LocalDateTime.now(ZoneOffset.UTC)))
                seqstatedate ← DBIO.from(sendMessage(group, joiningUserId, joiningUserAuthId, memberIds, randomId, date, GroupServiceMessages.userJoined, isFat = true))
              } yield (seqstatedate, memberIds.toVector :+ invitingUserId, randomId)
            }
          } yield updates
        }

        db.run(action) pipeTo replyTo onFailure {
          case e ⇒
            replyTo ! Status.Failure(e)
        }
      }
    } else {
      sender() ! Status.Failure(GroupErrors.UserAlreadyInvited)
    }
  }

  protected def kick(group: Group, kickedUserId: Int, kickerUserId: Int, kickerAuthId: Long, randomId: Long): Unit = {
    val replyTo = sender()
    val date = new DateTime

    persist(GroupEvents.UserKicked(kickedUserId, kickerUserId, date.getMillis)) { evt ⇒
      context become working(updateState(evt, group))

      val update = UpdateGroupUserKick(groupId, kickedUserId, kickerUserId, date.getMillis, randomId)
      val serviceMessage = GroupServiceMessages.userKicked(kickedUserId)

      val action: DBIO[SeqStateDate] = {
        for {
          _ ← p.GroupUser.delete(groupId, kickedUserId)
          _ ← p.GroupInviteToken.revoke(groupId, kickedUserId) //TODO: move to cleanup helper, with all cleanup code and use in kick/leave
          (SeqState(seq, state), _) ← broadcastClientAndUsersUpdate(kickerUserId, kickerAuthId, group.members.keySet - kickedUserId, update, Some(PushTexts.Kicked), isFat = false)
          // TODO: Move to a History Writing subsystem
          _ ← HistoryUtils.writeHistoryMessage(
            models.Peer.privat(kickerUserId),
            models.Peer.group(groupId),
            date,
            randomId,
            serviceMessage.header,
            serviceMessage.toByteArray
          )
        } yield SeqStateDate(seq, state, date.getMillis)
      }
      db.run(action) pipeTo replyTo onFailure {
        case e ⇒ replyTo ! Status.Failure(e)
      }
    }
  }

  protected def leave(group: Group, userId: Int, authId: Long, randomId: Long): Unit = {
    val replyTo = sender()
    val date = new DateTime

    persist(GroupEvents.UserLeft(userId, date.getMillis)) { evt ⇒
      context become working(updateState(evt, group))

      val update = UpdateGroupUserLeave(groupId, userId, date.getMillis, randomId)
      val serviceMessage = GroupServiceMessages.userLeft(userId)
      val memberIds = group.members.keySet

      val action: DBIO[SeqStateDate] = {
        for {
          _ ← p.GroupUser.delete(groupId, userId)
          _ ← p.GroupInviteToken.revoke(groupId, userId) //TODO: move to cleanup helper, with all cleanup code and use in kick/leave
          (SeqState(seq, state), _) ← broadcastClientAndUsersUpdate(userId, authId, memberIds - userId, update, Some(PushTexts.Left), isFat = false)
          // TODO: Move to a History Writing subsystem
          _ ← HistoryUtils.writeHistoryMessage(
            models.Peer.privat(userId),
            models.Peer.group(groupId),
            date,
            randomId,
            serviceMessage.header,
            serviceMessage.toByteArray
          )
        } yield SeqStateDate(seq, state, date.getMillis)
      }
      db.run(action) pipeTo replyTo onFailure {
        case e ⇒ replyTo ! Status.Failure(e)
      }
    }
  }

  protected def updateAvatar(group: Group, clientUserId: Int, clientAuthId: Long, avatarOpt: Option[Avatar], randomId: Long): Unit = {
    persistStashingReply(AvatarUpdated(avatarOpt))(workWith(_, group)) { evt ⇒
      val date = new DateTime
      val avatarData = avatarOpt map (getAvatarData(models.AvatarData.OfGroup, groupId, _)) getOrElse (models.AvatarData.empty(models.AvatarData.OfGroup, groupId.toLong))

      val update = UpdateGroupAvatarChanged(groupId, clientUserId, avatarOpt, date.getMillis, randomId)
      val serviceMessage = GroupServiceMessages.changedAvatar(avatarOpt)

      val memberIds = group.members.keySet

      db.run(for {
        _ ← p.AvatarData.createOrUpdate(avatarData)
        (seqstate, _) ← broadcastClientAndUsersUpdate(clientUserId, clientAuthId, memberIds, update, None, isFat = false)
      } yield {
        db.run(HistoryUtils.writeHistoryMessage(
          models.Peer.privat(clientUserId),
          models.Peer.group(groupId),
          date,
          randomId,
          serviceMessage.header,
          serviceMessage.toByteArray
        ))

        UpdateAvatarResponse(avatarOpt, SeqStateDate(seqstate.seq, seqstate.state, date.getMillis))
      })
    }
  }

  protected def makePublic(group: Group, description: String): Unit = {
    persistStashingReply(Vector(BecamePublic(), DescriptionUpdated(description)))(workWith(_, group)) { _ ⇒
      db.run(DBIO.sequence(Seq(
        p.Group.makePublic(groupId),
        p.Group.updateDescription(groupId, description)
      ))) map (_ ⇒ MakePublicAck())
    }
  }

  protected def updateTitle(group: Group, clientUserId: Int, clientAuthId: Long, title: String, randomId: Long): Unit = {
    val memberIds = group.members.keySet

    persistStashingReply(TitleUpdated(title))(workWith(_, group)) { _ ⇒
      val date = new DateTime

      val update = UpdateGroupTitleChanged(groupId = groupId, userId = clientUserId, title = title, date = date.getMillis, randomId = randomId)
      val serviceMessage = GroupServiceMessages.changedTitle(title)

      db.run(for {
        _ ← p.Group.updateTitle(groupId, title, clientUserId, randomId, date)
        _ ← HistoryUtils.writeHistoryMessage(
          models.Peer.privat(clientUserId),
          models.Peer.group(groupId),
          date,
          randomId,
          serviceMessage.header,
          serviceMessage.toByteArray
        )
        (seqstate, _) ← broadcastClientAndUsersUpdate(clientUserId, clientAuthId, memberIds, update, Some(PushTexts.TitleChanged), isFat = false)
      } yield SeqStateDate(seqstate.seq, seqstate.state, date.getMillis))
    }
  }
}
