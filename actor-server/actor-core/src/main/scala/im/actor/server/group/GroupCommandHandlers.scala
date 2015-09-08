package im.actor.server.group

import java.time.{ LocalDateTime, ZoneOffset }

import akka.actor.Status
import akka.pattern.pipe
import com.google.protobuf.ByteString
import im.actor.api.rpc.Update
import im.actor.api.rpc.groups._
import im.actor.api.rpc.messaging.ApiServiceMessage
import im.actor.api.rpc.misc.ApiExtension
import im.actor.api.rpc.users.ApiSex
import im.actor.server.ApiConversions._
import im.actor.server.acl.ACLUtils
import im.actor.server.history.HistoryUtils
import im.actor.server.{ persist ⇒ p, models }
import im.actor.server.event.TSEvent
import im.actor.server.file.{ ImageUtils, Avatar }
import im.actor.server.group.GroupErrors._
import im.actor.server.office.PushTexts
import im.actor.server.dialog.group.GroupDialogOperations
import im.actor.server.sequence.SeqUpdatesManager._
import im.actor.server.sequence.{ SeqState, SeqStateDate }
import im.actor.server.user.UserOffice
import ACLUtils._
import im.actor.util.misc.IdUtils._
import ImageUtils._
import org.joda.time.DateTime
import slick.driver.PostgresDriver.api._

import scala.concurrent.Future
import scala.concurrent.forkjoin.ThreadLocalRandom

private[group] trait GroupCommandHandlers extends GroupsImplicits with GroupCommandHelpers {
  this: GroupProcessor ⇒

  import GroupCommands._
  import GroupEvents._

  protected def createInternal(typ: GroupType, creatorUserId: Int, title: String, userIds: Seq[Int], isHidden: Option[Boolean], isHistoryShared: Option[Boolean], extensions: Seq[ApiExtension] = Seq.empty): Unit = {
    val accessHash = genAccessHash()

    val date = now()
    val created = GroupEvents.Created(groupId, Some(typ), creatorUserId, accessHash, title, (userIds.toSet + creatorUserId).toSeq, isHidden, isHistoryShared, extensions)
    val state = initState(date, created)

    persist(TSEvent(date, created)) { _ ⇒
      context become working(state)

      val rng = ThreadLocalRandom.current()

      // FIXME: invite other members

      val update = UpdateGroupInvite(groupId, creatorUserId, date.getMillis, rng.nextLong())

      db.run(for {
        _ ← createInDb(state, rng.nextLong())
        _ ← p.GroupUser.create(groupId, creatorUserId, creatorUserId, date, None, isAdmin = true)
        _ ← DBIO.from(UserOffice.broadcastUserUpdate(creatorUserId, update, pushText = None, isFat = true, deliveryId = Some(s"creategroup_${groupId}_${update.randomId}")))
      } yield CreateInternalAck(accessHash)) pipeTo sender() onFailure {
        case e ⇒
          log.error(e, "Failed to create group internally")
      }
    }
  }

  protected def create(groupId: Int, typ: GroupType, creatorUserId: Int, creatorAuthId: Long, title: String, randomId: Long, userIds: Set[Int]): Unit = {
    val accessHash = genAccessHash()

    val rng = ThreadLocalRandom.current()
    userIds.filterNot(_ == creatorUserId) foreach { userId ⇒
      val randomId = rng.nextLong()
      context.parent ! Invite(groupId, userId, creatorUserId, creatorAuthId, randomId)
    }

    val date = now()

    val created = GroupEvents.Created(groupId, Some(typ), creatorUserId, accessHash, title, Seq(creatorUserId), isHidden = Some(false), isHistoryShared = Some(false))
    val state = initState(date, created)

    persist(TSEvent(date, created)) { _ ⇒
      context become working(state)

      val serviceMessage = GroupServiceMessages.groupCreated

      val update = UpdateGroupInvite(groupId = groupId, inviteUserId = creatorUserId, date = date.getMillis, randomId = randomId)

      db.run(
        for {
          _ ← p.Group.create(
            models.Group(
              id = groupId,
              creatorUserId = state.creatorUserId,
              accessHash = state.accessHash,
              title = state.title,
              isPublic = (state.typ == GroupType.Public),
              createdAt = state.createdAt,
              about = None,
              topic = None
            ),
            randomId
          )
          _ ← p.GroupUser.create(groupId, creatorUserId, creatorUserId, date, None, isAdmin = true)
          _ ← HistoryUtils.writeHistoryMessage(
            models.Peer.privat(creatorUserId),
            models.Peer.group(state.id),
            date,
            randomId,
            serviceMessage.header,
            serviceMessage.toByteArray
          )
          seqstate ← if (isBot(state, creatorUserId)) DBIO.successful(SeqState(0, ByteString.EMPTY))
          else DBIO.from(UserOffice.broadcastClientUpdate(creatorUserId, creatorAuthId, update, pushText = None, isFat = true, deliveryId = Some(s"creategroup_${groupId}_${randomId}")))
        } yield CreateAck(state.accessHash, seqstate, date.getMillis)
      ) pipeTo sender() onFailure {
          case e ⇒
            log.error(e, "Failed to create a group")
        }
    }

    val botUserId = nextIntId(rng)
    val botToken = accessToken(rng)
    val botAdded = GroupEvents.BotAdded(botUserId, botToken)

    persist(TSEvent(now(), botAdded)) { tsEvt ⇒
      context become working(updatedState(tsEvt, state))

      (for {
        _ ← UserOffice.create(botUserId, nextAccessSalt(ThreadLocalRandom.current()), "Bot", "US", ApiSex.Unknown, isBot = true)
        _ ← db.run(p.GroupBot.create(groupId, botUserId, botToken))
        _ ← integrationTokensKv.upsert(botToken, groupId)
      } yield ()) onFailure {
        case e ⇒
          log.error(e, "Failed to create group bot")
      }
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
      _ ← UserOffice.broadcastUserUpdate(userId, inviteeUpdate, pushText = Some(PushTexts.Invited), isFat = true, deliveryId = Some(s"invite_${groupId}_${randomId}"))
      // TODO: #perf the following broadcasts do update serializing per each user
      _ ← Future.sequence(memberIds.toSeq.filterNot(_ == inviterUserId).map(UserOffice.broadcastUserUpdate(_, userAddedUpdate, Some(PushTexts.Added), isFat = true, deliveryId = Some(s"useradded_${groupId}_${randomId}")))) // use broadcastUsersUpdate maybe?
      seqstate ← UserOffice.broadcastClientUpdate(inviterUserId, inviterAuthId, userAddedUpdate, pushText = None, isFat = true, deliveryId = Some(s"useradded_${groupId}_${randomId}"))
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

  protected def setJoined(group: Group, joiningUserId: Int, joiningUserAuthId: Long, invitingUserId: Int): Unit = {
    if (!hasMember(group, joiningUserId) || isInvited(group, joiningUserId)) {
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
                exists ← p.GroupUser.exists(groupId, joiningUserId)
                _ ← if (exists) DBIO.successful(()) else p.GroupUser.create(groupId, joiningUserId, invitingUserId, date, Some(LocalDateTime.now(ZoneOffset.UTC)), isAdmin = false)
                seqstatedate ← DBIO.from(GroupDialogOperations.sendMessage(groupId, joiningUserId, joiningUserAuthId, randomId, GroupServiceMessages.userJoined, isFat = true))
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

      db.run(removeUser(kickedUserId, group.members.keySet, kickerAuthId, serviceMessage, update, date, randomId)) pipeTo replyTo onFailure {
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
      db.run(removeUser(userId, group.members.keySet, authId, serviceMessage, update, date, randomId)) pipeTo replyTo onFailure {
        case e ⇒ replyTo ! Status.Failure(e)
      }
    }
  }

  protected def updateAvatar(group: Group, clientUserId: Int, clientAuthId: Long, avatarOpt: Option[Avatar], randomId: Long): Unit = {
    persistStashingReply(TSEvent(now(), AvatarUpdated(avatarOpt)), group) { evt ⇒
      val date = new DateTime
      val avatarData = avatarOpt map (getAvatarData(models.AvatarData.OfGroup, groupId, _)) getOrElse models.AvatarData.empty(models.AvatarData.OfGroup, groupId.toLong)

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
      val oldToken = group.bot.map(_.token)
      val newToken = accessToken(ThreadLocalRandom.current())
      persistStashingReply(TSEvent(now(), IntegrationTokenRevoked(newToken)), group) { _ ⇒
        for {
          _ ← db.run(p.GroupBot.updateToken(groupId, newToken))
          _ ← integrationTokensKv.delete(oldToken.getOrElse(""))
          _ ← integrationTokensKv.upsert(newToken, groupId)
        } yield RevokeIntegrationTokenAck(newToken)
      }
    }
  }

  private def removeUser(userId: Int, memberIds: Set[Int], clientAuthId: Long, serviceMessage: ApiServiceMessage, update: Update, date: DateTime, randomId: Long): DBIO[SeqStateDate] = {
    val groupPeer = models.Peer.group(groupId)
    for {
      _ ← p.GroupUser.delete(groupId, userId)
      _ ← p.GroupInviteToken.revoke(groupId, userId)
      (SeqState(seq, state), _) ← broadcastClientAndUsersUpdate(userId, clientAuthId, memberIds - userId, update, Some(PushTexts.Left), isFat = false)
      // TODO: Move to a History Writing subsystem
      _ ← p.Dialog.updateLastReadAt(userId, groupPeer, date)
      _ ← p.Dialog.updateOwnerLastReadAt(userId, groupPeer, date)
      _ ← HistoryUtils.writeHistoryMessage(
        models.Peer.privat(userId),
        groupPeer,
        date,
        randomId,
        serviceMessage.header,
        serviceMessage.toByteArray
      )
    } yield SeqStateDate(seq, state, date.getMillis)
  }

  private def genAccessHash(): Long =
    ThreadLocalRandom.current().nextLong()

  private def createInDb(state: Group, randomId: Long) =
    p.Group.create(
      models.Group(
        id = groupId,
        creatorUserId = state.creatorUserId,
        accessHash = state.accessHash,
        title = state.title,
        isPublic = (state.typ == GroupType.Public),
        createdAt = state.createdAt,
        about = None,
        topic = None
      ),
      randomId
    )
}
