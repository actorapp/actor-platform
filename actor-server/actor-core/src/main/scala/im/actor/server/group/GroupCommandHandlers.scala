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
import im.actor.server.dialog.{ DialogExtension, UserAcl }
import im.actor.server.model.{ AvatarData, Group, Peer, PeerType }
import im.actor.server.persist._
import im.actor.server.file.{ Avatar, ImageUtils }
import im.actor.server.group.GroupErrors._
import im.actor.server.office.PushTexts
import im.actor.server.sequence.{ PushData, PushRules, SeqState, SeqStateDate }
import im.actor.util.ThreadLocalSecureRandom
import ACLUtils._
import im.actor.util.misc.IdUtils._
import ImageUtils._
import akka.http.scaladsl.util.FastFuture
import im.actor.concurrent.FutureExt
import im.actor.server.CommonErrors
import org.joda.time.DateTime
import slick.driver.PostgresDriver.api._

import scala.concurrent.Future

private[group] trait GroupCommandHandlers extends GroupsImplicits with GroupCommandHelpers with UserAcl {
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

      // FIXME: invite other members

      val update = UpdateGroupInviteObsolete(groupId, creatorUserId, date.toEpochMilli, rng.nextLong())

      db.run(for {
        _ ← createInDb(state, rng.nextLong())
        _ ← GroupUserRepo.create(groupId, creatorUserId, creatorUserId, date, None, isAdmin = true)
        _ ← DBIO.from(seqUpdExt.deliverUserUpdate(
          creatorUserId,
          update,
          pushRules = seqUpdExt.pushRules(isFat = true, None),
          deliveryId = s"creategroup_${groupId}_${update.randomId}"
        ))
      } yield CreateInternalAck(accessHash)) pipeTo sender() onFailure {
        case e ⇒
          log.error(e, "Failed to create group internally")
      }
    }
  }

  protected def create(groupId: Int, typ: GroupType, creatorUserId: Int, creatorAuthId: Long, title: String, randomId: Long, userIds: Set[Int]): Unit = {
    val accessHash = genAccessHash()

    val rng = ThreadLocalSecureRandom.current()

    // exclude ids of users, who blocked group creator
    val resolvedUserIds = FutureExt.ftraverse((userIds - creatorUserId).toSeq) { userId ⇒
      withNonBlockedUser(creatorUserId, userId)(
        default = FastFuture.successful(Some(userId)),
        failed = FastFuture.successful(None)
      )
    } map (_.flatten)

    // send invites to all users, that creator can invite
    for {
      userIds ← resolvedUserIds
      _ = userIds foreach (u ⇒ context.parent ! Invite(groupId, u, creatorUserId, rng.nextLong()))
    } yield ()

    val date = now()

    val created = GroupEvents.Created(date, groupId, Some(typ), creatorUserId, accessHash, title, Seq(creatorUserId), isHidden = Some(false), isHistoryShared = Some(false))
    val state = initState(created)

    persist(created) { _ ⇒
      context become working(state)

      val update = UpdateGroupInviteObsolete(groupId = groupId, inviteUserId = creatorUserId, date = date.toEpochMilli, randomId = randomId)

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
          memeberIds ← DBIO.from(resolvedUserIds)
          _ ← DBIO.from(Future.sequence((memeberIds :+ creatorUserId) map { uid ⇒
            dialogExt.writeMessageSelf(
              userId = uid,
              peer = ApiPeer(ApiPeerType.Group, state.id),
              senderUserId = creatorUserId,
              date = new DateTime(date.toEpochMilli),
              randomId = randomId,
              message = GroupServiceMessages.groupCreated
            )
          }))
          seqState ← if (isBot(state, creatorUserId)) DBIO.successful(SeqState(0, ByteString.EMPTY))
          else DBIO.from(seqUpdExt.deliverClientUpdate(
            userId = creatorUserId,
            authId = creatorAuthId,
            update = update,
            pushRules = PushRules(isFat = true, excludeAuthIds = Seq(creatorAuthId)),
            reduceKey = None,
            deliveryId = s"creategroup_${groupId}_$randomId"
          ))
        } yield CreateAck(state.accessHash, seqState, date.toEpochMilli)
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

  protected def invite(group: GroupState, userId: Int, inviterUserId: Int, inviterAuthId: Long, randomId: Long, date: Instant): Future[SeqStateDate] = {
    val dateMillis = date.toEpochMilli
    val memberIds = group.members.keySet

    val inviteeUpdate = UpdateGroupInviteObsolete(groupId = groupId, randomId = randomId, inviteUserId = inviterUserId, date = dateMillis)

    val userAddedUpdate = UpdateGroupUserInvitedObsolete(groupId = groupId, userId = userId, inviterUserId = inviterUserId, date = dateMillis, randomId = randomId)
    val serviceMessage = GroupServiceMessages.userInvited(userId)

    for {
      _ ← db.run(GroupUserRepo.create(groupId, userId, inviterUserId, date, None, isAdmin = false))

      // push "Invited" to invitee
      _ ← seqUpdExt.deliverUserUpdate(
        userId,
        inviteeUpdate,
        pushRules = seqUpdExt.pushRules(isFat = true, Some(PushTexts.Invited)),
        deliveryId = s"invite_${groupId}_${randomId}"
      )

      // push "User added" to all group members except `inviterUserId`
      _ ← seqUpdExt.broadcastPeopleUpdate(
        memberIds - inviterUserId,
        userAddedUpdate,
        pushRules = seqUpdExt.pushRules(isFat = true, Some(PushTexts.Added)),
        deliveryId = s"useradded_${groupId}_${randomId}"
      )

      // push "User added" to `inviterUserId` and return SeqState
      SeqState(seq, state) ← seqUpdExt.deliverClientUpdate(
        inviterUserId,
        inviterAuthId,
        userAddedUpdate,
        pushRules = PushRules(isFat = true),
        deliveryId = s"useradded_${groupId}_$randomId"
      )
      _ ← dialogExt.writeMessage(
        ApiPeer(ApiPeerType.Group, groupId),
        inviterUserId,
        date,
        randomId,
        serviceMessage
      )
    } yield SeqStateDate(seq, state, dateMillis)
  }

  protected def setJoined(group: GroupState, joiningUserId: Int, joiningUserAuthId: Long, invitingUserId: Int): Unit = {
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
                _ ← if (exists)
                  DBIO.successful(())
                else GroupUserRepo.create(groupId, joiningUserId, invitingUserId, date, Some(LocalDateTime.now(ZoneOffset.UTC)), isAdmin = false)
                seqStateDate ← DBIO.from(DialogExtension(system).sendMessage(
                  peer = ApiPeer(ApiPeerType.Group, groupId),
                  senderUserId = joiningUserId,
                  senderAuthId = joiningUserAuthId,
                  randomId = randomId,
                  message = GroupServiceMessages.userJoined,
                  accessHash = group.accessHash,
                  isFat = true
                ))
              } yield (seqStateDate, memberIds.toVector :+ invitingUserId, randomId)
            }
          } yield updates
        }

        db.run(action) pipeTo replyTo
      }
    } else {
      sender() ! Status.Failure(GroupErrors.UserAlreadyInvited)
    }
  }

  protected def kick(group: GroupState, kickedUserId: Int, kickerUserId: Int, kickerAuthId: Long, randomId: Long): Unit = {
    val replyTo = sender()
    val date = Instant.now()

    persist(GroupEvents.UserKicked(date, kickedUserId, kickerUserId)) { evt ⇒
      workWith(evt, group)
      db.run(removeUser(
        initiatorId = kickerUserId,
        userId = kickedUserId,
        group.members.keySet,
        kickerAuthId,
        serviceMessage = GroupServiceMessages.userKicked(kickedUserId),
        update = UpdateGroupUserKickObsolete(groupId, kickedUserId, kickerUserId, date.toEpochMilli, randomId),
        date,
        randomId
      )) pipeTo replyTo
    }
  }

  protected def leave(group: GroupState, userId: Int, authId: Long, randomId: Long): Unit = {
    val replyTo = sender()

    persist(GroupEvents.UserLeft(now(), userId)) { evt ⇒
      workWith(evt, group)

      db.run(removeUser(
        initiatorId = userId,
        userId = userId,
        group.members.keySet,
        authId,
        serviceMessage = GroupServiceMessages.userLeft(userId),
        update = UpdateGroupUserLeaveObsolete(groupId, userId, evt.ts.toEpochMilli, randomId),
        evt.ts,
        randomId
      )) pipeTo replyTo
    }
  }

  protected def updateAvatar(group: GroupState, clientUserId: Int, clientAuthId: Long, avatarOpt: Option[Avatar], randomId: Long): Unit = {
    persistStashingReply(AvatarUpdated(now(), avatarOpt), group) { evt ⇒
      val date = evt.ts
      val avatarData = avatarOpt map (getAvatarData(AvatarData.OfGroup, groupId, _)) getOrElse AvatarData.empty(AvatarData.OfGroup, groupId.toLong)

      val update = UpdateGroupAvatarChangedObsolete(groupId, clientUserId, avatarOpt, date.toEpochMilli, randomId)
      val serviceMessage = GroupServiceMessages.changedAvatar(avatarOpt)

      val memberIds = group.members.keySet

      for {
        _ ← db.run(AvatarDataRepo.createOrUpdate(avatarData))
        SeqState(seq, state) ← seqUpdExt.broadcastClientUpdate(clientUserId, clientAuthId, memberIds, update)
      } yield {
        dialogExt.writeMessage(
          ApiPeer(ApiPeerType.Group, groupId),
          clientUserId,
          date,
          randomId,
          serviceMessage
        )

        UpdateAvatarAck(avatarOpt, SeqStateDate(seq, state, date.toEpochMilli))
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

  protected def updateTitle(group: GroupState, clientUserId: Int, clientAuthId: Long, title: String, randomId: Long): Unit = {
    val memberIds = group.members.keySet

    persistStashingReply(TitleUpdated(now(), title), group) { evt ⇒
      val date = evt.ts

      val update = UpdateGroupTitleChangedObsolete(groupId = groupId, userId = clientUserId, title = title, date = date.toEpochMilli, randomId = randomId)
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
        SeqState(seq, state) ← seqUpdExt.broadcastClientUpdate(
          clientUserId,
          clientAuthId,
          memberIds,
          update,
          PushRules().withData(PushData().withText(PushTexts.TitleChanged))
        )
      } yield SeqStateDate(seq, state, date.toEpochMilli)
    }
  }

  protected def updateTopic(group: GroupState, clientUserId: Int, clientAuthId: Long, topic: Option[String], randomId: Long): Unit = {
    withGroupMember(group, clientUserId) { member ⇒
      val trimmed = topic.map(_.trim)
      if (trimmed.forall(s ⇒ s.nonEmpty & s.length < 255)) {
        persistStashingReply(TopicUpdated(now(), trimmed), group) { evt ⇒
          val date = evt.ts
          val dateMillis = date.toEpochMilli
          val serviceMessage = GroupServiceMessages.changedTopic(trimmed)
          val update = UpdateGroupTopicChangedObsolete(groupId = groupId, randomId = randomId, userId = clientUserId, topic = trimmed, date = dateMillis)
          for {
            _ ← db.run(GroupRepo.updateTopic(groupId, trimmed))
            _ ← dialogExt.writeMessage(
              ApiPeer(ApiPeerType.Group, groupId),
              clientUserId,
              date,
              randomId,
              serviceMessage
            )
            SeqState(seq, state) ← seqUpdExt.broadcastClientUpdate(
              clientUserId,
              clientAuthId,
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

  protected def updateAbout(group: GroupState, clientUserId: Int, clientAuthId: Long, about: Option[String], randomId: Long): Unit = {
    withGroupAdmin(group, clientUserId) {
      val trimmed = about.map(_.trim)
      if (trimmed.forall(s ⇒ s.nonEmpty & s.length < 255)) {
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
            SeqState(seq, state) ← DBIO.from(seqUpdExt.broadcastClientUpdate(
              clientUserId,
              clientAuthId,
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

  protected def makeUserAdmin(group: GroupState, clientUserId: Int, clientAuthId: Long, candidateId: Int): Unit = {
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
              SeqState(seq, state) ← seqUpdExt.broadcastClientUpdate(
                clientUserId,
                clientAuthId,
                group.members.keySet - clientUserId,
                UpdateGroupMembersUpdateObsolete(groupId, members)
              )
            } yield (members, SeqStateDate(seq, state, date.toEpochMilli))
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

  protected def transferOwnership(group: GroupState, clientUserId: Int, clientAuthId: Long, userId: Int): Unit = {
    if (group.ownerUserId == clientUserId) {
      persistReply(OwnerChanged(Instant.now, userId), group) { _ ⇒
        for {
          seqState ← seqUpdExt.broadcastClientUpdate(
            userId = clientUserId,
            authId = clientAuthId,
            bcastUserIds = group.members.keySet.filterNot(_ == clientUserId),
            update = UpdateGroupOwnerChanged(group.id, userId),
            pushRules = PushRules().withExcludeAuthIds(Seq(clientAuthId))
          )
        } yield seqState
      }
    } else sender() ! Status.Failure(CommonErrors.Forbidden)
  }

  private def removeUser(initiatorId: Int, userId: Int, memberIds: Set[Int], clientAuthId: Long, serviceMessage: ApiServiceMessage, update: Update, date: Instant, randomId: Long): DBIO[SeqStateDate] = {
    val groupPeer = ApiPeer(ApiPeerType.Group, groupId)
    for {
      _ ← GroupUserRepo.delete(groupId, userId)
      _ ← GroupInviteTokenRepo.revoke(groupId, userId)
      SeqState(seq, state) ← DBIO.from(
        for {
          seqState ← seqUpdExt.broadcastClientUpdate(
            userId = initiatorId,
            authId = clientAuthId,
            bcastUserIds = memberIds,
            update = update,
            pushRules = seqUpdExt.pushRules(isFat = false, Some(PushTexts.Left), Seq(clientAuthId))
          )
          _ ← dialogExt.messageRead(groupPeer, userId, 0L, date.toEpochMilli)
          _ ← dialogExt.writeMessage(
            groupPeer,
            initiatorId,
            date,
            randomId,
            serviceMessage
          )
        } yield seqState
      )
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

  private def rng = ThreadLocalSecureRandom.current()
}
