package im.actor.server.group

import java.time.{ Instant, LocalDateTime, ZoneOffset }

import akka.actor.Status
import akka.pattern.pipe
import com.google.protobuf.ByteString
import im.actor.api.rpc.Update
import im.actor.api.rpc.groups._
import im.actor.api.rpc.messaging.ApiServiceMessage
import im.actor.api.rpc.misc.ApiExtension
import im.actor.api.rpc.peers.{ ApiPeer, ApiPeerType }
import im.actor.api.rpc.users.ApiSex
import im.actor.server.ApiConversions._
import im.actor.server.acl.ACLUtils
import im.actor.server.dialog.DialogExtension
import im.actor.server.model.{ Group, Peer, AvatarData, PeerType }
import im.actor.server.persist._
import im.actor.server.file.{ ImageUtils, Avatar }
import im.actor.server.group.GroupErrors._
import im.actor.server.office.PushTexts
import im.actor.server.sequence.{ PushData, PushRules, SeqState, SeqStateDate }
import im.actor.util.ThreadLocalSecureRandom
import ACLUtils._
import im.actor.util.misc.IdUtils._
import ImageUtils._
import org.joda.time.DateTime
import slick.driver.PostgresDriver.api._

import scala.concurrent.Future

private[group] trait GroupCommandHandlers extends GroupsImplicits with GroupCommandHelpers {
  this: GroupProcessor ⇒

  import GroupCommands._
  import GroupEvents._

  protected def createInternal(typ: GroupType, creatorUserId: Int, title: String, userIds: Seq[Int], isHidden: Option[Boolean], isHistoryShared: Option[Boolean], extensions: Seq[ApiExtension] = Seq.empty): Unit = {
    val accessHash = genAccessHash()

    val date = now()
    val created = GroupEvents.Created(date, groupId, Some(typ), creatorUserId, accessHash, title, (userIds.toSet + creatorUserId).toSeq, isHidden, isHistoryShared, extensions)
    val state = initState(created)

    persist(created) { _ ⇒
      context become working(state)

      val rng = ThreadLocalSecureRandom.current()

      // FIXME: invite other members

      val update = UpdateGroupInvite(groupId, creatorUserId, date.toEpochMilli, rng.nextLong())

      db.run(for {
        _ ← createInDb(state, rng.nextLong())
        _ ← GroupUserRepo.create(groupId, creatorUserId, creatorUserId, date, None, isAdmin = true)
        _ ← DBIO.from(userExt.broadcastUserUpdate(creatorUserId, update, pushText = None, isFat = true, reduceKey = None, deliveryId = Some(s"creategroup_${groupId}_${update.randomId}")))
      } yield CreateInternalAck(accessHash)) pipeTo sender() onFailure {
        case e ⇒
          log.error(e, "Failed to create group internally")
      }
    }
  }

  protected def create(groupId: Int, typ: GroupType, creatorUserId: Int, title: String, randomId: Long, userIds: Set[Int]): Unit = {
    val accessHash = genAccessHash()

    val rng = ThreadLocalSecureRandom.current()
    userIds.filterNot(_ == creatorUserId) foreach { userId ⇒
      val randomId = rng.nextLong()
      context.parent ! Invite(groupId, userId, creatorUserId, randomId)
    }

    val date = now()

    val created = GroupEvents.Created(date, groupId, Some(typ), creatorUserId, accessHash, title, Seq(creatorUserId), isHidden = Some(false), isHistoryShared = Some(false))
    val state = initState(created)

    persist(created) { _ ⇒
      context become working(state)

      val serviceMessage = GroupServiceMessages.groupCreated

      val update = UpdateGroupInvite(groupId = groupId, inviteUserId = creatorUserId, date = date.toEpochMilli, randomId = randomId)

      db.run(
        for {
          _ ← GroupRepo.create(
            Group(
              id = groupId,
              creatorUserId = state.creatorUserId,
              accessHash = state.accessHash,
              title = state.title,
              isPublic = state.typ == GroupType.Public,
              createdAt = state.createdAt,
              about = None,
              topic = None
            ),
            randomId,
            isHidden = false
          )
          _ ← GroupUserRepo.create(groupId, creatorUserId, creatorUserId, date, None, isAdmin = true)
          _ ← DBIO.from(Future.sequence((userIds + creatorUserId) map { uid ⇒
            dialogExt.writeMessageSelf(uid, ApiPeer(ApiPeerType.Group, state.id), creatorUserId, new DateTime(date.toEpochMilli), randomId, serviceMessage)
          }))
          seqstate ← if (isBot(state, creatorUserId)) DBIO.successful(SeqState(0, ByteString.EMPTY))
          else DBIO.from(seqUpdExt.deliverSingleUpdate(creatorUserId, update, PushRules(isFat = true), reduceKey = None, deliveryId = s"creategroup_${groupId}_$randomId"))
        } yield CreateAck(state.accessHash, seqstate, date.toEpochMilli)
      ) pipeTo sender() onFailure {
          case e ⇒
            log.error(e, "Failed to create a group")
        }
    }

    val botUserId = nextIntId(rng)
    val botToken = accessToken(rng)
    val botAdded = GroupEvents.BotAdded(now(), botUserId, botToken)

    persist(botAdded) { tsEvt ⇒
      context become working(updatedState(tsEvt, state))

      (for {
        _ ← userExt.create(botUserId, nextAccessSalt(ThreadLocalSecureRandom.current()), None, "Bot", "US", ApiSex.Unknown, isBot = true)
        _ ← db.run(GroupBotRepo.create(groupId, botUserId, botToken))
        _ ← integrationTokensKv.upsert(botToken, groupId)
      } yield ()) onFailure {
        case e ⇒
          log.error(e, "Failed to create group bot")
      }
    }
  }

  protected def invite(group: GroupState, userId: Int, inviterUserId: Int, randomId: Long, date: Instant): Future[SeqStateDate] = {
    val dateMillis = date.toEpochMilli
    val memberIds = group.members.keySet

    val inviteeUpdate = UpdateGroupInvite(groupId = groupId, randomId = randomId, inviteUserId = inviterUserId, date = dateMillis)

    val userAddedUpdate = UpdateGroupUserInvited(groupId = groupId, userId = userId, inviterUserId = inviterUserId, date = dateMillis, randomId = randomId)
    val serviceMessage = GroupServiceMessages.userInvited(userId)

    for {
      _ ← db.run(GroupUserRepo.create(groupId, userId, inviterUserId, date, None, isAdmin = false))
      _ ← userExt.broadcastUserUpdate(userId, inviteeUpdate, pushText = Some(PushTexts.Invited), isFat = true, reduceKey = None, deliveryId = Some(s"invite_${groupId}_${randomId}"))
      // TODO: #perf the following broadcasts do update serializing per each user
      _ ← Future.sequence(memberIds.toSeq.filterNot(_ == inviterUserId).map(userExt.broadcastUserUpdate(_, userAddedUpdate, Some(PushTexts.Added), isFat = true, reduceKey = None, deliveryId = Some(s"useradded_${groupId}_${randomId}")))) // use broadcastUsersUpdate maybe?
      seqstate ← seqUpdExt.deliverSingleUpdate(inviterUserId, userAddedUpdate, PushRules(isFat = true), reduceKey = None, deliveryId = s"useradded_${groupId}_$randomId")
      // TODO: Move to a History Writing subsystem
      _ ← dialogExt.writeMessage(
        ApiPeer(ApiPeerType.Group, groupId),
        inviterUserId,
        date,
        randomId,
        serviceMessage
      )
    } yield {
      SeqStateDate(seqstate.seq, seqstate.state, dateMillis)
    }
  }

  protected def setJoined(group: GroupState, joiningUserId: Int, joinintUserAuthSid: Int, invitingUserId: Int): Unit = {
    if (!hasMember(group, joiningUserId) || isInvited(group, joiningUserId)) {
      val replyTo = sender()

      persist(GroupEvents.UserJoined(now(), joiningUserId, invitingUserId)) { evt ⇒
        workWith(evt, group)

        val memberIds = group.members.keySet

        val action: DBIO[(SeqStateDate, Vector[Int], Long)] = {
          for {
            updates ← {
              val date = evt.ts
              val randomId = ThreadLocalSecureRandom.current().nextLong()
              for {
                exists ← GroupUserRepo.exists(groupId, joiningUserId)
                _ ← if (exists) DBIO.successful(()) else GroupUserRepo.create(groupId, joiningUserId, invitingUserId, date, Some(LocalDateTime.now(ZoneOffset.UTC)), isAdmin = false)
                seqstatedate ← DBIO.from(DialogExtension(system).sendMessage(
                  peer = ApiPeer(ApiPeerType.Group, groupId),
                  senderUserId = joiningUserId,
                  senderAuthSid = joinintUserAuthSid,
                  senderAuthId = None,
                  randomId = randomId,
                  message = GroupServiceMessages.userJoined,
                  isFat = true
                ))
              } yield (seqstatedate, memberIds.toVector :+ invitingUserId, randomId)
            }
          } yield updates
        }

        db.run(action) pipeTo replyTo
      }
    } else {
      sender() ! Status.Failure(GroupErrors.UserAlreadyInvited)
    }
  }

  protected def kick(group: GroupState, kickedUserId: Int, kickerUserId: Int, kickerAuthSid: Int, randomId: Long): Unit = {
    val replyTo = sender()
    val date = Instant.now()

    persist(GroupEvents.UserKicked(date, kickedUserId, kickerUserId)) { evt ⇒
      workWith(evt, group)

      val update = UpdateGroupUserKick(groupId, kickedUserId, kickerUserId, date.toEpochMilli, randomId)
      val serviceMessage = GroupServiceMessages.userKicked(kickedUserId)

      db.run(removeUser(
        initiatorId = kickerUserId,
        userId = kickedUserId,
        group.members.keySet,
        kickerAuthSid,
        serviceMessage,
        update,
        date,
        randomId
      )) pipeTo replyTo
    }
  }

  protected def leave(group: GroupState, userId: Int, authSid: Int, randomId: Long): Unit = {
    val replyTo = sender()

    persist(GroupEvents.UserLeft(now(), userId)) { evt ⇒
      workWith(evt, group)

      val update = UpdateGroupUserLeave(groupId, userId, evt.ts.toEpochMilli, randomId)
      val serviceMessage = GroupServiceMessages.userLeft(userId)
      db.run(removeUser(
        initiatorId = userId,
        userId = userId,
        group.members.keySet,
        authSid,
        serviceMessage,
        update,
        evt.ts,
        randomId
      )) pipeTo replyTo
    }
  }

  protected def updateAvatar(group: GroupState, clientUserId: Int, avatarOpt: Option[Avatar], randomId: Long): Unit = {
    persistStashingReply(AvatarUpdated(now(), avatarOpt), group) { evt ⇒
      val date = evt.ts
      val avatarData = avatarOpt map (getAvatarData(AvatarData.OfGroup, groupId, _)) getOrElse AvatarData.empty(AvatarData.OfGroup, groupId.toLong)

      val update = UpdateGroupAvatarChanged(groupId, clientUserId, avatarOpt, date.toEpochMilli, randomId)
      val serviceMessage = GroupServiceMessages.changedAvatar(avatarOpt)

      val memberIds = group.members.keySet

      for {
        _ ← db.run(AvatarDataRepo.createOrUpdate(avatarData))
        (seqstate, _) ← seqUpdExt.broadcastOwnSingleUpdate(clientUserId, memberIds, update)
      } yield {
        dialogExt.writeMessage(
          ApiPeer(ApiPeerType.Group, groupId),
          clientUserId,
          date,
          randomId,
          serviceMessage
        )

        UpdateAvatarAck(avatarOpt, SeqStateDate(seqstate.seq, seqstate.state, date.toEpochMilli))
      }
    }
  }

  protected def makePublic(group: GroupState, description: String): Unit = {
    persistStashingReply(Vector(BecamePublic(now()), AboutUpdated(now(), Some(description))), group) { _ ⇒
      db.run(DBIO.sequence(Seq(
        GroupRepo.makePublic(groupId),
        GroupRepo.updateAbout(groupId, Some(description))
      ))) map (_ ⇒ MakePublicAck())
    }
  }

  protected def updateTitle(group: GroupState, clientUserId: Int, title: String, randomId: Long): Unit = {
    val memberIds = group.members.keySet

    persistStashingReply(TitleUpdated(now(), title), group) { evt ⇒
      val date = evt.ts

      val update = UpdateGroupTitleChanged(groupId = groupId, userId = clientUserId, title = title, date = date.toEpochMilli, randomId = randomId)
      val serviceMessage = GroupServiceMessages.changedTitle(title)

      for {
        _ ← db.run(GroupRepo.updateTitle(groupId, title, clientUserId, randomId, date))
        _ ← dialogExt.writeMessage(
          ApiPeer(ApiPeerType.Group, groupId),
          clientUserId,
          date,
          randomId,
          serviceMessage
        )
        (seqstate, _) ← seqUpdExt.broadcastOwnSingleUpdate(
          clientUserId,
          memberIds,
          update,
          PushRules().withData(PushData().withText(PushTexts.TitleChanged))
        )
      } yield SeqStateDate(seqstate.seq, seqstate.state, date.toEpochMilli)
    }
  }

  protected def updateTopic(group: GroupState, clientUserId: Int, topic: Option[String], randomId: Long): Unit = {
    withGroupMember(group, clientUserId) { member ⇒
      val trimmed = topic.map(_.trim)
      if (trimmed.map(s ⇒ s.nonEmpty & s.length < 255).getOrElse(true)) {
        persistStashingReply(TopicUpdated(now(), trimmed), group) { evt ⇒
          val date = evt.ts
          val dateMillis = date.toEpochMilli
          val serviceMessage = GroupServiceMessages.changedTopic(trimmed)
          val update = UpdateGroupTopicChanged(groupId = groupId, randomId = randomId, userId = clientUserId, topic = trimmed, date = dateMillis)
          for {
            _ ← db.run(GroupRepo.updateTopic(groupId, trimmed))
            _ ← dialogExt.writeMessage(
              ApiPeer(ApiPeerType.Group, groupId),
              clientUserId,
              date,
              randomId,
              serviceMessage
            )
            (SeqState(seq, state), _) ← seqUpdExt.broadcastOwnSingleUpdate(
              clientUserId,
              group.members.keySet - clientUserId,
              update,
              PushRules().withData(PushData().withText(PushTexts.TopicChanged))
            )
          } yield SeqStateDate(seq, state, dateMillis)
        }
      } else {
        sender() ! Status.Failure(TopicTooLong)
      }
    }
  }

  protected def updateAbout(group: GroupState, clientUserId: Int, about: Option[String], randomId: Long): Unit = {
    withGroupAdmin(group, clientUserId) {
      val trimmed = about.map(_.trim)
      if (trimmed.map(s ⇒ s.nonEmpty & s.length < 255).getOrElse(true)) {
        persistStashingReply(AboutUpdated(now(), trimmed), group) { evt ⇒
          val date = evt.ts
          val dateMillis = date.toEpochMilli
          val update = UpdateGroupAboutChanged(groupId, trimmed)
          val serviceMessage = GroupServiceMessages.changedAbout(trimmed)
          db.run(for {
            _ ← GroupRepo.updateAbout(groupId, trimmed)
            _ ← DBIO.from(dialogExt.writeMessage(
              ApiPeer(ApiPeerType.Group, groupId),
              clientUserId,
              date,
              randomId,
              serviceMessage
            ))
            (SeqState(seq, state), _) ← DBIO.from(seqUpdExt.broadcastOwnSingleUpdate(
              clientUserId,
              group.members.keySet - clientUserId,
              update,
              PushRules().withData(PushData().withText(PushTexts.AboutChanged))
            ))
          } yield SeqStateDate(seq, state, dateMillis))
        }
      } else {
        sender() ! Status.Failure(AboutTooLong)
      }
    }
  }

  protected def makeUserAdmin(group: GroupState, clientUserId: Int, candidateId: Int): Unit = {
    withGroupAdmin(group, clientUserId) {
      withGroupMember(group, candidateId) { member ⇒
        persistStashingReply(UserBecameAdmin(now(), candidateId, clientUserId), group) { e ⇒
          val date = e.ts

          if (!member.isAdmin) {
            //we have current state, that does not updated by UserBecameAdmin event. That's why we update it manually
            val updated = group.members.updated(candidateId, group.members(candidateId).copy(isAdmin = true))
            val members = updated.values.map(_.asStruct).toVector
            for {
              _ ← db.run(GroupUserRepo.makeAdmin(groupId, candidateId))
              (seqState, _) ← seqUpdExt.broadcastOwnSingleUpdate(
                clientUserId,
                group.members.keySet - clientUserId,
                UpdateGroupMembersUpdate(groupId, members)
              )
            } yield (members, seqState)
          } else {
            Future.failed(UserAlreadyAdmin)
          }
        }
      }
    }
  }

  protected def revokeIntegrationToken(group: GroupState, userId: Int): Unit = {
    withGroupAdmin(group, userId) {
      val oldToken = group.bot.map(_.token)
      val newToken = accessToken(ThreadLocalSecureRandom.current())
      persistStashingReply(IntegrationTokenRevoked(now(), newToken), group) { _ ⇒
        for {
          _ ← db.run(GroupBotRepo.updateToken(groupId, newToken))
          _ ← integrationTokensKv.delete(oldToken.getOrElse(""))
          _ ← integrationTokensKv.upsert(newToken, groupId)
        } yield RevokeIntegrationTokenAck(newToken)
      }
    }
  }

  private def removeUser(initiatorId: Int, userId: Int, memberIds: Set[Int], clientAuthSid: Int, serviceMessage: ApiServiceMessage, update: Update, date: Instant, randomId: Long): DBIO[SeqStateDate] = {
    val groupPeer = Peer(PeerType.Group, groupId)
    for {
      _ ← GroupUserRepo.delete(groupId, userId)
      _ ← GroupInviteTokenRepo.revoke(groupId, userId)
      (SeqState(seq, state), _) ← DBIO.from(userExt.broadcastClientAndUsersUpdate(
        clientUserId = userId,
        clientAuthSid = clientAuthSid,
        userIds = memberIds - userId,
        update = update,
        pushText = Some(PushTexts.Left),
        isFat = false,
        deliveryId = None
      ))
      // TODO: Move to a History Writing subsystem
      _ ← dialog.DialogRepo.updateOwnerLastReadAt(userId, groupPeer, new DateTime(date.toEpochMilli))
      _ ← DBIO.from(dialogExt.writeMessage(
        ApiPeer(ApiPeerType.Group, groupId),
        initiatorId,
        date,
        randomId,
        serviceMessage
      ))
    } yield SeqStateDate(seq, state, date.toEpochMilli)
  }

  private def genAccessHash(): Long =
    ThreadLocalSecureRandom.current().nextLong()

  private def createInDb(state: GroupState, randomId: Long) =
    GroupRepo.create(
      Group(
        id = groupId,
        creatorUserId = state.creatorUserId,
        accessHash = state.accessHash,
        title = state.title,
        isPublic = state.typ == GroupType.Public,
        createdAt = state.createdAt,
        about = None,
        topic = None
      ),
      randomId,
      state.isHidden
    )
}
