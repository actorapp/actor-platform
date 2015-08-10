package im.actor.server.group

import java.time.{ LocalDateTime, ZoneOffset }

import im.actor.server.event.TSEvent

import scala.concurrent.Future
import scala.concurrent.forkjoin.ThreadLocalRandom

import akka.actor.Status
import akka.pattern.pipe
import com.google.protobuf.ByteString
import com.trueaccord.scalapb.GeneratedMessage
import org.joda.time.DateTime
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc.groups._
import im.actor.api.rpc.messaging.{ Message ⇒ ApiMessage, UpdateMessageRead, UpdateMessageReadByMe, UpdateMessageReceived }
import im.actor.api.rpc.users.Sex
import im.actor.server.api.ApiConversions._
import im.actor.server.file.Avatar
import im.actor.server.group.GroupErrors._
import im.actor.server.office.PushTexts
import im.actor.server.push.SeqUpdatesManager._
import im.actor.server.sequence.{ SeqState, SeqStateDate }
import im.actor.server.user.UserOffice
import im.actor.server.util.ACLUtils._
import im.actor.server.util.HistoryUtils._
import im.actor.server.util.IdUtils._
import im.actor.server.util.ImageUtils._
import im.actor.server.util.{ ACLUtils, GroupServiceMessages, HistoryUtils }
import im.actor.server.{ models, persist ⇒ p }
import im.actor.utils.cache.CacheHelpers._

private[group] trait GroupCommandHandlers extends GroupsImplicits with GroupCommandHelpers {
  this: GroupProcessor ⇒

  import GroupCommands._
  import GroupEvents._

  protected def create(groupId: Int, creatorUserId: Int, creatorAuthId: Long, title: String, randomId: Long, userIds: Set[Int]): Unit = {
    val date = new DateTime

    val rng = ThreadLocalRandom.current()

    val accessHash = rng.nextLong()
    val botUserId = nextIntId(rng)
    val botToken = accessToken(rng)

    val events = Vector(
      TSEvent(now(), GroupEvents.Created(groupId, creatorUserId, accessHash, title)),
      TSEvent(now(), GroupEvents.BotAdded(botUserId, botToken))
    )

    userIds.filterNot(_ == creatorUserId) foreach { userId ⇒
      val randomId = rng.nextLong()
      context.parent ! Invite(groupId, userId, creatorUserId, creatorAuthId, randomId)
    }

    var stateMaybe: Option[Group] = None

    persist[GeneratedMessage](events) {
      case TSEvent(ts, evt: GroupEvents.Created) ⇒
        val group = initState(ts, evt)

        stateMaybe = Some(group)

        val serviceMessage = GroupServiceMessages.groupCreated

        val update = UpdateGroupInvite(groupId = groupId, inviteUserId = creatorUserId, date = date.getMillis, randomId = randomId)

        db.run(
          for {
            _ ← p.Group.create(
              models.Group(
                id = groupId,
                creatorUserId = group.creatorUserId,
                accessHash = group.accessHash,
                title = group.title,
                isPublic = group.isPublic,
                createdAt = group.createdAt,
                about = None,
                topic = None
              ),
              randomId
            )
            _ ← p.GroupUser.create(groupId, creatorUserId, creatorUserId, date, None, isAdmin = true)
            _ ← HistoryUtils.writeHistoryMessage(
              models.Peer.privat(creatorUserId),
              models.Peer.group(group.id),
              date,
              randomId,
              serviceMessage.header,
              serviceMessage.toByteArray
            )
            SeqState(seq, state) ← if (isBot(group, creatorUserId)) DBIO.successful(SeqState(0, ByteString.EMPTY))
            else DBIO.from(UserOffice.broadcastClientUpdate(creatorUserId, creatorAuthId, update, pushText = None, isFat = true))
          } yield CreateAck(group.accessHash, seq, state, date.getMillis)
        ) pipeTo sender() onFailure {
            case e ⇒
              log.error(e, "Failed to create a group")
          }

      case evt @ TSEvent(_, GroupEvents.BotAdded(userId, token)) ⇒
        stateMaybe = stateMaybe map { state ⇒
          val newState = updatedState(evt, state)
          context become working(newState)
          newState
        }

        val rng = ThreadLocalRandom.current()

        UserOffice.create(userId, nextAccessSalt(rng), "Bot", "US", Sex.Unknown, isBot = true)
          .flatMap(_ ⇒ db.run(p.GroupBot.create(groupId, userId, token))) onFailure {
            case e ⇒
              log.error(e, "Failed to create group bot")
          }
    }
  }

  protected def sendMessage(group: Group, senderUserId: Int, senderAuthId: Long, groupUsersIds: Set[Int], randomId: Long, date: DateTime, message: ApiMessage, isFat: Boolean): Future[SeqStateDate] = {
    val groupPeer = groupPeerStruct(groupId)
    val memberIds = group.members.keySet

    withCachedFuture[AuthIdRandomId, SeqStateDate](senderAuthId → randomId) { () ⇒
      this.lastSenderId = Some(senderUserId)

      memberIds foreach { userId ⇒
        if (userId != senderUserId) {
          UserOffice.deliverMessage(userId, groupPeer, senderUserId, randomId, date, message, isFat)
        }
      }

      for {
        SeqState(seq, state) ← if (isBot(group, senderUserId))
          Future.successful(SeqState(0, ByteString.EMPTY))
        else
          UserOffice.deliverOwnMessage(senderUserId, groupPeer, senderAuthId, randomId, date, message, isFat)
        _ ← db.run(writeHistoryMessage(models.Peer.privat(senderUserId), models.Peer.group(groupPeer.id), date, randomId, message.header, message.toByteArray))
      } yield SeqStateDate(seq, state, date.getMillis)
    }
  }

  protected def messageReceived(group: Group, receiverUserId: Int, date: Long): Unit = {
    val receiveFuture: Future[MessageReceivedAck] =
      if (!this.lastReceiveDate.exists(_ >= date) && !this.lastSenderId.contains(receiverUserId)) {
        this.lastReceiveDate = Some(date)
        val now = System.currentTimeMillis

        val update = UpdateMessageReceived(groupPeerStruct(groupId), date, now)

        val memberIds = group.members.keySet

        val authIdsF = Future.sequence(memberIds.filterNot(_ == receiverUserId) map UserOffice.getAuthIds) map (_.flatten.toSet)

        for {
          _ ← db.run(markMessagesReceived(models.Peer.privat(receiverUserId), models.Peer.group(groupId), new DateTime(date)))
          authIds ← authIdsF
          _ ← db.run(persistAndPushUpdates(authIds.toSet, update, None, isFat = false))
        } yield MessageReceivedAck()
      } else Future.successful(MessageReceivedAck())
    val replyTo = sender()
    receiveFuture pipeTo replyTo onFailure {
      case e ⇒
        replyTo ! Status.Failure(ReceiveFailed)
        log.error(e, "Failed to mark messages received")
    }
  }

  protected def messageRead(group: Group, readerUserId: Int, readerAuthId: Long, date: Long): Unit = {
    val readFuture: Future[MessageReadAck] =
      if (!this.lastSenderId.exists(_ == readerUserId)) {
        db.run(markMessagesRead(models.Peer.privat(readerUserId), models.Peer.group(groupId), new DateTime(date))) foreach { _ ⇒
          UserOffice.getAuthIds(readerUserId) map { authIds ⇒
            val authIdsSet = authIds.toSet
            for {
              counterUpdate ← db.run(getUpdateCountersChanged(readerUserId))
              _ ← persistAndPushUpdatesF(authIdsSet, UpdateMessageReadByMe(groupPeerStruct(groupId), date), None, isFat = false)
              _ ← persistAndPushUpdatesF(authIdsSet, counterUpdate, None, isFat = false)
            } yield ()
          }
        }

        val newState: Group = if (group.invitedUserIds.contains(readerUserId)) {
          val joinEvent = TSEvent(now(), GroupEvents.UserJoined(readerUserId, group.creatorUserId))
          val result = workWith(joinEvent, group)
          persistAsync(joinEvent) { e ⇒
            db.run(for (_ ← p.GroupUser.setJoined(groupId, readerUserId, LocalDateTime.now(ZoneOffset.UTC))) yield {
              val randomId = ThreadLocalRandom.current().nextLong()
              self ! SendMessage(groupId, readerUserId, readerAuthId, group.accessHash, randomId, GroupServiceMessages.userJoined)
            })
          }
          result
        } else {
          group
        }

        if (!this.lastReadDate.exists(_ >= date) && !this.lastSenderId.contains(readerUserId)) {
          this.lastReadDate = Some(date)
          val now = System.currentTimeMillis()

          val groupPeer = groupPeerStruct(groupId)
          val memberIds = newState.members.keys.toSeq

          val authIdsF = Future.sequence(memberIds.filterNot(_ == readerUserId) map UserOffice.getAuthIds) map (_.flatten.toSet)

          for {
            authIds ← authIdsF
            _ ← db.run(markMessagesRead(models.Peer.privat(readerUserId), models.Peer.group(groupId), new DateTime(date)))
            _ ← persistAndPushUpdatesF(authIds, UpdateMessageRead(groupPeer, date, now), None, isFat = false)
          } yield MessageReadAck()
        } else Future.successful(MessageReadAck())
      } else Future.successful(MessageReadAck())

    val replyTo = sender()

    readFuture pipeTo replyTo onFailure {
      case e ⇒
        replyTo ! Status.Failure(ReadFailed)
        log.error(e, "Failed to mark messages read")
    }

  }

  protected def invite(group: Group, userId: Int, inviterUserId: Int, inviterAuthId: Long, randomId: Long, date: DateTime): Future[SeqStateDate] = {
    val dateMillis = date.getMillis
    val memberIds = group.members.keySet

    val inviteeUpdate = UpdateGroupInvite(groupId = groupId, randomId = randomId, inviteUserId = inviterUserId, date = dateMillis)

    val userAddedUpdate = UpdateGroupUserInvited(groupId = groupId, userId = userId, inviterUserId = inviterUserId, date = dateMillis, randomId = randomId)
    val serviceMessage = GroupServiceMessages.userInvited(userId)

    for {
      _ ← db.run(p.GroupUser.create(groupId, userId, inviterUserId, date, None, isAdmin = false))
      _ ← UserOffice.broadcastUserUpdate(userId, inviteeUpdate, pushText = Some(PushTexts.Invited), isFat = true)
      // TODO: #perf the following broadcasts do update serializing per each user
      _ ← Future.sequence(memberIds.toSeq.filterNot(_ == inviterUserId).map(UserOffice.broadcastUserUpdate(_, userAddedUpdate, Some(PushTexts.Added), isFat = true))) // use broadcastUsersUpdate maybe?
      seqstate ← UserOffice.broadcastClientUpdate(inviterUserId, inviterAuthId, userAddedUpdate, pushText = None, isFat = true)
      // TODO: Move to a History Writing subsystem
      _ ← db.run(HistoryUtils.writeHistoryMessage(
        models.Peer.privat(inviterUserId),
        models.Peer.group(groupId),
        date,
        randomId,
        serviceMessage.header,
        serviceMessage.toByteArray
      ))
    } yield {
      SeqStateDate(seqstate.seq, seqstate.state, dateMillis)
    }
  }

  protected def join(group: Group, joiningUserId: Int, joiningUserAuthId: Long, invitingUserId: Int): Unit = {
    if (!hasMember(group, joiningUserId)) {
      val replyTo = sender()

      persist(TSEvent(now(), GroupEvents.UserJoined(joiningUserId, invitingUserId))) { evt ⇒
        val newState = workWith(evt, group)

        val memberIds = group.members.keySet

        val action: DBIO[(SeqStateDate, Vector[Int], Long)] = {
          for {
            updates ← {
              val date = new DateTime
              val randomId = ThreadLocalRandom.current().nextLong()
              for {
                _ ← p.GroupUser.create(groupId, joiningUserId, invitingUserId, date, Some(LocalDateTime.now(ZoneOffset.UTC)), isAdmin = false)
                seqstatedate ← DBIO.from(sendMessage(newState, joiningUserId, joiningUserAuthId, memberIds, randomId, date, GroupServiceMessages.userJoined, isFat = true))
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

    persist(TSEvent(now(), GroupEvents.UserKicked(kickedUserId, kickerUserId, date.getMillis))) { evt ⇒
      workWith(evt, group)

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

    persist(TSEvent(now(), GroupEvents.UserLeft(userId, date.getMillis))) { evt ⇒
      workWith(evt, group)

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
    persistStashingReply(TSEvent(now(), AvatarUpdated(avatarOpt)), group) { evt ⇒
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

        UpdateAvatarAck(avatarOpt, SeqStateDate(seqstate.seq, seqstate.state, date.getMillis))
      })
    }
  }

  protected def makePublic(group: Group, description: String): Unit = {
    persistStashingReply(Vector(TSEvent(now(), BecamePublic()), TSEvent(now(), AboutUpdated(Some(description)))), group) { _ ⇒
      db.run(DBIO.sequence(Seq(
        p.Group.makePublic(groupId),
        p.Group.updateAbout(groupId, Some(description))
      ))) map (_ ⇒ MakePublicAck())
    }
  }

  protected def updateTitle(group: Group, clientUserId: Int, clientAuthId: Long, title: String, randomId: Long): Unit = {
    val memberIds = group.members.keySet

    persistStashingReply(TSEvent(now(), TitleUpdated(title)), group) { _ ⇒
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

  protected def updateTopic(group: Group, clientUserId: Int, clientAuthId: Long, topic: Option[String], randomId: Long): Unit = {
    withGroupMember(group, clientUserId) { member ⇒
      val trimmed = topic.map(_.trim)
      if (trimmed.map(s ⇒ s.nonEmpty & s.length < 255).getOrElse(true)) {
        persistStashingReply(TSEvent(now(), TopicUpdated(trimmed)), group) { _ ⇒
          val date = new DateTime
          val dateMillis = date.getMillis
          val serviceMessage = GroupServiceMessages.changedTopic(trimmed)
          val update = UpdateGroupTopicChanged(groupId = groupId, randomId = randomId, userId = clientUserId, topic = trimmed, date = dateMillis)
          db.run(for {
            _ ← p.Group.updateTopic(groupId, trimmed)
            _ ← HistoryUtils.writeHistoryMessage(
              models.Peer.privat(clientUserId),
              models.Peer.group(groupId),
              date,
              randomId,
              serviceMessage.header,
              serviceMessage.toByteArray
            )
            (SeqState(seq, state), _) ← broadcastClientAndUsersUpdate(
              clientUserId = clientUserId,
              clientAuthId = clientAuthId,
              userIds = group.members.keySet - clientUserId,
              update = update,
              pushText = Some(PushTexts.TopicChanged),
              isFat = false
            )
          } yield SeqStateDate(seq, state, dateMillis))
        }
      } else {
        sender() ! Status.Failure(TopicTooLong)
      }
    }
  }

  protected def updateAbout(group: Group, clientUserId: Int, clientAuthId: Long, about: Option[String], randomId: Long): Unit = {
    withGroupAdmin(group, clientUserId) {
      val trimmed = about.map(_.trim)
      if (trimmed.map(s ⇒ s.nonEmpty & s.length < 255).getOrElse(true)) {
        persistStashingReply(TSEvent(now(), AboutUpdated(trimmed)), group) { _ ⇒
          val date = new DateTime
          val dateMillis = date.getMillis
          val update = UpdateGroupAboutChanged(groupId, trimmed)
          val serviceMessage = GroupServiceMessages.changedAbout(trimmed)
          db.run(for {
            _ ← p.Group.updateAbout(groupId, trimmed)
            _ ← HistoryUtils.writeHistoryMessage(
              models.Peer.privat(clientUserId),
              models.Peer.group(groupId),
              date,
              randomId,
              serviceMessage.header,
              serviceMessage.toByteArray
            )
            (SeqState(seq, state), _) ← broadcastClientAndUsersUpdate(
              clientUserId = clientUserId,
              clientAuthId = clientAuthId,
              userIds = group.members.keySet - clientUserId,
              update = update,
              pushText = Some(PushTexts.AboutChanged),
              isFat = false
            )
          } yield SeqStateDate(seq, state, dateMillis))
        }
      } else {
        sender() ! Status.Failure(AboutTooLong)
      }
    }
  }

  protected def makeUserAdmin(group: Group, clientUserId: Int, clientAuthId: Long, candidateId: Int): Unit = {
    withGroupAdmin(group, clientUserId) {
      withGroupMember(group, candidateId) { member ⇒
        persistStashingReply(TSEvent(now(), UserBecameAdmin(candidateId, clientUserId)), group) { e ⇒
          val date = e.ts

          if (!member.isAdmin) {
            //we have current state, that does not updated by UserBecameAdmin event. That's why we update it manually
            val updated = group.members.updated(candidateId, group.members(candidateId).copy(isAdmin = true))
            val members = updated.values.map(_.asStruct).toVector
            db.run(for {
              _ ← p.GroupUser.makeAdmin(groupId, candidateId)
              (seqState, _) ← broadcastClientAndUsersUpdate(
                clientUserId = clientUserId,
                clientAuthId = clientAuthId,
                userIds = group.members.keySet - clientUserId,
                update = UpdateGroupMembersUpdate(groupId, members),
                pushText = None,
                isFat = false
              )
            } yield (members, seqState))
          } else {
            Future.failed(UserAlreadyAdmin)
          }
        }
      }
    }
  }

  protected def revokeIntegrationToken(group: Group, userId: Int): Unit = {
    withGroupAdmin(group, userId) {
      val newToken = ACLUtils.accessToken(ThreadLocalRandom.current())
      persistStashingReply(TSEvent(now(), IntegrationTokenRevoked(newToken)), group) { _ ⇒
        db.run(for {
          _ ← p.GroupBot.updateToken(groupId, newToken)
        } yield RevokeIntegrationTokenAck(newToken))
      }
    }
  }

}
