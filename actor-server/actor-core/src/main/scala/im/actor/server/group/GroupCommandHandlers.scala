package im.actor.server.group

import java.time.{ Instant, LocalDateTime, ZoneOffset }

import akka.actor.Status
import akka.http.scaladsl.util.FastFuture
import akka.pattern.pipe
import im.actor.api.rpc.Update
import im.actor.api.rpc.files.ApiAvatar
import im.actor.api.rpc.groups._
import im.actor.api.rpc.messaging.{ ApiServiceMessage, UpdateMessage }
import im.actor.api.rpc.users.ApiSex
import im.actor.concurrent.FutureExt
import im.actor.server.CommonErrors
import im.actor.server.acl.ACLUtils
import im.actor.server.dialog.UserAcl
import im.actor.server.file.{ Avatar, ImageUtils }
import im.actor.server.group.GroupErrors._
import im.actor.server.group.GroupEvents._
import im.actor.server.group.GroupCommands._
import im.actor.server.model.{ AvatarData, Group }
import im.actor.server.names.{ GlobalNameOwner, OwnerType }
import im.actor.server.persist.{ AvatarDataRepo, GroupBotRepo, GroupInviteTokenRepo, GroupRepo, GroupUserRepo }
import im.actor.server.sequence.{ Optimization, SeqState, SeqStateDate }
import im.actor.util.ThreadLocalSecureRandom
import im.actor.util.misc.{ IdUtils, StringUtils }

import scala.concurrent.Future

//TODO: spit into MemberCommandHandlers - InfoCommandHandlers - AdminCommandHandlers
private[group] trait GroupCommandHandlers extends GroupsImplicits with UserAcl {
  this: GroupProcessor ⇒

  import im.actor.server.ApiConversions._

  private val notMember = Status.Failure(NotAMember)
  private val notAdmin = Status.Failure(NotAdmin)
  private val noPermission = Status.Failure(NoPermission)

  protected def create(cmd: Create): Unit = {
    if (!isValidTitle(cmd.title)) {
      sender() ! Status.Failure(InvalidTitle)
    } else {
      val rng = ThreadLocalSecureRandom.current()
      val accessHash = ACLUtils.randomLong(rng)
      val createdAt = Instant.now

      // exclude ids of users, who blocked group creator
      val resolvedUserIds = FutureExt.ftraverse(cmd.userIds.filterNot(_ == cmd.creatorUserId)) { userId ⇒
        withNonBlockedUser(cmd.creatorUserId, userId)(
          default = FastFuture.successful(Some(userId)),
          failed = FastFuture.successful(None)
        )
      } map (_.flatten)

      // send invites to all users, that creator can invite
      for {
        userIds ← resolvedUserIds
        _ = userIds foreach (u ⇒ context.parent !
          GroupEnvelope(groupId)
          .withInvite(Invite(u, cmd.creatorUserId, cmd.creatorAuthId, rng.nextLong())))
      } yield ()

      integrationStorage = new IntegrationTokensKeyValueStorage

      // Group creation
      val groupType = GroupType.fromValue(cmd.typ) //FIXME: make it normal enum
      val isHistoryShared = groupType.isChannel || groupType.isPublic

      persist(Created(
        ts = createdAt,
        groupId,
        typ = Some(groupType),
        creatorUserId = cmd.creatorUserId,
        accessHash = accessHash,
        title = cmd.title,
        userIds = Seq(cmd.creatorUserId), // only creator user becomes group member. all other users are invited via Invite message
        isHidden = Some(false),
        isHistoryShared = Some(isHistoryShared),
        extensions = Seq.empty
      )) { evt ⇒
        val newState = commit(evt)

        val dateMillis = createdAt.toEpochMilli

        val updateObsolete = UpdateGroupInviteObsolete(
          groupId,
          inviteUserId = cmd.creatorUserId,
          date = dateMillis,
          randomId = cmd.randomId
        )

        val serviceMessage = GroupServiceMessages.groupCreated

        //TODO: remove deprecated
        db.run(
          for {
            _ ← GroupRepo.create(
              Group(
                id = groupId,
                creatorUserId = newState.creatorUserId,
                accessHash = newState.accessHash,
                title = newState.title,
                isPublic = isHistoryShared,
                createdAt = evt.ts,
                about = None,
                topic = None
              ),
              cmd.randomId,
              isHidden = false
            )
            _ ← GroupUserRepo.create(groupId, cmd.creatorUserId, cmd.creatorUserId, createdAt, None, isAdmin = true)
          } yield ()
        )

        val result: Future[CreateAck] = for {
          ///////////////////////////
          // Groups V1 API updates //
          ///////////////////////////

          _ ← seqUpdExt.deliverUserUpdate(
            userId = cmd.creatorUserId,
            update = updateObsolete,
            pushRules = seqUpdExt.pushRules(isFat = true, None, excludeAuthIds = Seq(cmd.creatorAuthId)), //do we really need to remove self auth id here?
            reduceKey = None,
            deliveryId = s"creategroup_obsolete_${groupId}_${cmd.randomId}"
          )

          ///////////////////////////
          // Groups V2 API updates //
          ///////////////////////////

          // send service message to group
          seqStateDate ← dialogExt.sendServerMessage(
            apiGroupPeer,
            senderUserId = cmd.creatorUserId,
            senderAuthId = cmd.creatorAuthId,
            randomId = cmd.randomId,
            message = serviceMessage,
            deliveryTag = Some(Optimization.GroupV2)
          )

        } yield CreateAck(newState.accessHash).withSeqStateDate(seqStateDate)

        result pipeTo sender() onFailure {
          case e ⇒
            log.error(e, "Failed to create a group")
        }
      }

      //Adding bot to group
      val botUserId = IdUtils.nextIntId(rng)
      val botToken = ACLUtils.accessToken(rng)

      persist(BotAdded(Instant.now, botUserId, botToken)) { evt ⇒
        val newState = commit(evt)

        //TODO: remove deprecated
        db.run(GroupBotRepo.create(groupId, botUserId, botToken))

        (for {
          _ ← userExt.create(botUserId, ACLUtils.nextAccessSalt(), None, "Bot", "US", ApiSex.Unknown, isBot = true)
          _ ← integrationStorage.upsertToken(botToken, groupId)
        } yield ()) onFailure {
          case e ⇒
            log.error(e, "Failed to create group bot")
        }
      }
    }
  }

  protected def invite(cmd: Invite): Unit = {
    if (!state.permissions.canInvitePeople(cmd.inviterUserId)) {
      sender() ! noPermission
    } else if (state.isInvited(cmd.inviteeUserId)) {
      sender() ! Status.Failure(GroupErrors.UserAlreadyInvited)
    } else if (state.isMember(cmd.inviteeUserId)) {
      sender() ! Status.Failure(GroupErrors.UserAlreadyJoined)
    } else {
      val inviteeIsExUser = state.isExUser(cmd.inviteeUserId)

      persist(UserInvited(Instant.now, cmd.inviteeUserId, cmd.inviterUserId)) { evt ⇒
        val newState = commit(evt)

        val dateMillis = evt.ts.toEpochMilli
        val memberIds = newState.memberIds
        val apiMembers = newState.members.values.map(_.asStruct).toVector

        // if user ever been in this group - we should push these updates,
        val inviteeUpdatesNew: List[Update] = refreshGroupUpdates(newState, cmd.inviteeUserId)

        val membersUpdateNew: Update =
          if (newState.groupType.isChannel) // if channel, or group is big enough
            UpdateGroupMembersCountChanged(groupId, newState.membersCount)
          else
            UpdateGroupMembersUpdated(groupId, apiMembers)

        val inviteeUpdateObsolete = UpdateGroupInviteObsolete(
          groupId,
          inviteUserId = cmd.inviterUserId,
          date = dateMillis,
          randomId = cmd.randomId
        )

        val membersUpdateObsolete = UpdateGroupUserInvitedObsolete(
          groupId,
          userId = cmd.inviteeUserId,
          inviterUserId = cmd.inviterUserId,
          date = dateMillis,
          randomId = cmd.randomId
        )
        val serviceMessage = GroupServiceMessages.userInvited(cmd.inviteeUserId)

        //TODO: remove deprecated
        db.run(GroupUserRepo.create(groupId, cmd.inviteeUserId, cmd.inviterUserId, evt.ts, None, isAdmin = false))

        def inviteGROUPUpdates: Future[SeqStateDate] =
          for {
            // push updated members list to inviteeUserId,
            // make it `FatSeqUpdate` if this user invited to group for first time.
            _ ← seqUpdExt.deliverUserUpdate(
              userId = cmd.inviteeUserId,
              membersUpdateNew,
              pushRules = seqUpdExt.pushRules(isFat = !inviteeIsExUser, Some(PushTexts.Invited)),
              deliveryId = s"invite_${groupId}_${cmd.randomId}"
            )

            // push all "refresh group" updates to inviteeUserId
            _ ← FutureExt.ftraverse(inviteeUpdatesNew) { update ⇒
              seqUpdExt.deliverUserUpdate(userId = cmd.inviteeUserId, update)
            }

            // push updated members list to all group members except inviteeUserId
            SeqState(seq, state) ← seqUpdExt.broadcastClientUpdate(
              userId = cmd.inviterUserId,
              authId = cmd.inviterAuthId,
              bcastUserIds = (memberIds - cmd.inviterUserId) - cmd.inviteeUserId,
              update = membersUpdateNew,
              deliveryId = s"useradded_${groupId}_${cmd.randomId}"
            )

            // explicitly send service message
            SeqStateDate(_, _, date) ← dialogExt.sendServerMessage(
              apiGroupPeer,
              cmd.inviterUserId,
              cmd.inviterAuthId,
              cmd.randomId,
              serviceMessage,
              deliveryTag = Some(Optimization.GroupV2)
            )
          } yield SeqStateDate(seq, state, date)

        def inviteCHANNELUpdates: Future[SeqStateDate] =
          for {
            // push updated members count to inviteeUserId
            _ ← seqUpdExt.deliverUserUpdate(
              userId = cmd.inviteeUserId,
              membersUpdateNew,
              pushRules = seqUpdExt.pushRules(isFat = false, Some(PushTexts.Invited)),
              deliveryId = s"invite_${groupId}_${cmd.randomId}"
            )

            // push all "refresh group" updates to inviteeUserId
            _ ← FutureExt.ftraverse(inviteeUpdatesNew) { update ⇒
              seqUpdExt.deliverUserUpdate(userId = cmd.inviteeUserId, update)
            }

            // push updated members count to all group members
            SeqState(seq, state) ← seqUpdExt.broadcastClientUpdate(
              userId = cmd.inviterUserId,
              authId = cmd.inviterAuthId,
              bcastUserIds = (memberIds - cmd.inviterUserId) - cmd.inviteeUserId,
              update = membersUpdateNew,
              deliveryId = s"useradded_${groupId}_${cmd.randomId}"
            )

            // push service message to invitee
            _ ← seqUpdExt.deliverUserUpdate(
              userId = cmd.inviteeUserId,
              update = serviceMessageUpdate(
                cmd.inviterUserId,
                dateMillis,
                cmd.randomId,
                serviceMessage
              ),
              deliveryTag = Some(Optimization.GroupV2)
            )
          } yield SeqStateDate(seq, state, dateMillis)

        val result: Future[SeqStateDate] = for {
          ///////////////////////////
          // Groups V1 API updates //
          ///////////////////////////

          // push "Invited" to invitee
          _ ← seqUpdExt.deliverUserUpdate(
            userId = cmd.inviteeUserId,
            inviteeUpdateObsolete,
            pushRules = seqUpdExt.pushRules(isFat = true, Some(PushTexts.Invited)),
            deliveryId = s"invite_obsolete_${groupId}_${cmd.randomId}"
          )

          // push "User added" to all group members except for `inviterUserId`
          _ ← seqUpdExt.broadcastPeopleUpdate(
            (memberIds - cmd.inviteeUserId) - cmd.inviterUserId, // is it right?
            membersUpdateObsolete,
            pushRules = seqUpdExt.pushRules(isFat = true, Some(PushTexts.Added)),
            deliveryId = s"useradded_obsolete_${groupId}_${cmd.randomId}"
          )

          // push "User added" to `inviterUserId`
          _ ← seqUpdExt.deliverClientUpdate(
            cmd.inviterUserId,
            cmd.inviterAuthId,
            membersUpdateObsolete,
            pushRules = seqUpdExt.pushRules(isFat = true, None),
            deliveryId = s"useradded_obsolete_${groupId}_${cmd.randomId}"
          )

          ///////////////////////////
          // Groups V2 API updates //
          ///////////////////////////

          seqStateDate ← if (newState.groupType.isChannel) inviteCHANNELUpdates else inviteGROUPUpdates

        } yield seqStateDate

        result pipeTo sender()
      }
    }
  }

  /**
   * User can join
   * • after invite(was invited by other user previously). In this case he already have group on devices
   * • via invite link. In this case he doesn't have group, and we need to deliver it.
   */
  protected def join(cmd: Join): Unit = {
    // user is already a member, and should not complete invitation process
    if (state.isMember(cmd.joiningUserId) && !state.isInvited(cmd.joiningUserId)) {
      sender() ! Status.Failure(GroupErrors.UserAlreadyJoined)
    } else {
      // user was invited in group by other group user
      val wasInvited = state.isInvited(cmd.joiningUserId)

      // trying to figure out who invited joining user.
      // Descdending priority:
      // • inviter defined in `Join` command (when invited via token)
      // • inviter from members list (when invited by other user)
      // • group creator (safe fallback)
      val optMember = state.members.get(cmd.joiningUserId)
      val inviterUserId = cmd.invitingUserId
        .orElse(optMember.map(_.inviterUserId))
        .getOrElse(state.creatorUserId)

      persist(UserJoined(Instant.now, cmd.joiningUserId, inviterUserId)) { evt ⇒
        val newState = commit(evt)

        val date = evt.ts
        val dateMillis = date.toEpochMilli
        val memberIds = newState.memberIds
        val apiMembers = newState.members.values.map(_.asStruct).toVector
        val randomId = ACLUtils.randomLong()

        // If user was never invited to group - he don't have group on devices,
        // that means we need to push all group-info related updates
        //
        // If user was invited to group by other member - we don't need to push group updates,
        // cause they we pushed already on invite step
        val joiningUserUpdatesNew: List[Update] =
          if (wasInvited) List.empty[Update] else refreshGroupUpdates(newState, cmd.joiningUserId)

        val membersUpdateNew: Update =
          if (newState.groupType.isChannel) // if channel, or group is big enough
            UpdateGroupMembersCountChanged(groupId, newState.membersCount)
          else
            UpdateGroupMembersUpdated(groupId, apiMembers) // will update date when member got into group

        val membersUpdateObsolete = UpdateGroupMembersUpdateObsolete(groupId, apiMembers)

        val serviceMessage = GroupServiceMessages.userJoined

        //TODO: remove deprecated
        db.run(GroupUserRepo.create(
          groupId,
          userId = cmd.joiningUserId,
          inviterUserId = inviterUserId,
          invitedAt = optMember.map(_.invitedAt).getOrElse(date),
          joinedAt = Some(LocalDateTime.now(ZoneOffset.UTC)),
          isAdmin = false
        ))

        def joinGROUPUpdates: Future[SeqStateDate] =
          for {
            // push all group updates to joiningUserId
            _ ← FutureExt.ftraverse(joiningUserUpdatesNew) { update ⇒
              seqUpdExt.deliverUserUpdate(userId = cmd.joiningUserId, update)
            }

            // push updated members list to joining user,
            // make it `FatSeqUpdate` if this user invited to group for first time.
            // TODO???: isFat = !wasInvited - is it correct?
            SeqState(seq, state) ← seqUpdExt.deliverClientUpdate(
              userId = cmd.joiningUserId,
              authId = cmd.joiningUserAuthId,
              update = membersUpdateNew,
              pushRules = seqUpdExt.pushRules(isFat = !wasInvited, None), //!wasInvited means that user came for first time here
              deliveryId = s"join_${groupId}_${randomId}"

            )

            // push updated members list to all group members except joiningUserId
            _ ← seqUpdExt.broadcastPeopleUpdate(
              memberIds - cmd.joiningUserId,
              membersUpdateNew,
              deliveryId = s"userjoined_${groupId}_${randomId}"
            )

            SeqStateDate(_, _, date) ← dialogExt.sendServerMessage(
              apiGroupPeer,
              senderUserId = cmd.joiningUserId,
              senderAuthId = cmd.joiningUserAuthId,
              randomId = randomId,
              serviceMessage // no delivery tag. This updated handled this way in Groups V1
            )
          } yield SeqStateDate(seq, state, date)

        def joinCHANNELUpdates: Future[SeqStateDate] =
          for {
            // push all group updates to joiningUserId
            _ ← FutureExt.ftraverse(joiningUserUpdatesNew) { update ⇒
              seqUpdExt.deliverUserUpdate(userId = cmd.joiningUserId, update)
            }

            // push updated members count to joining user
            SeqState(seq, state) ← seqUpdExt.deliverClientUpdate(
              userId = cmd.joiningUserId,
              authId = cmd.joiningUserAuthId,
              update = membersUpdateNew,
              deliveryId = s"join_${groupId}_${randomId}"
            )

            // push updated members count to all group members except joining user
            _ ← seqUpdExt.broadcastPeopleUpdate(
              memberIds - cmd.joiningUserId,
              membersUpdateNew,
              deliveryId = s"userjoined_${groupId}_${randomId}"
            )
          } yield SeqStateDate(seq, state, dateMillis)

        val result: Future[(SeqStateDate, Vector[Int], Long)] =
          for {
            ///////////////////////////
            // Groups V1 API updates //
            ///////////////////////////

            // push update about members to all users, except joining user
            _ ← seqUpdExt.broadcastPeopleUpdate(
              memberIds - cmd.joiningUserId,
              membersUpdateObsolete,
              pushRules = seqUpdExt.pushRules(isFat = true, None),
              deliveryId = s"userjoined_obsolete_${groupId}_${randomId}"
            )

            ///////////////////////////
            // Groups V2 API updates //
            ///////////////////////////

            seqStateDate ← if (newState.groupType.isChannel) joinCHANNELUpdates else joinGROUPUpdates

          } yield (seqStateDate, memberIds.toVector :+ inviterUserId, randomId)

        result pipeTo sender()
      }
    }
  }

  /**
   * This case handled in other manner, so we change state in the end
   * cause user that left, should send service message. And we don't allow non-members
   * to send message. So we keep him as member until message sent, and remove him from members
   */
  protected def leave(cmd: Leave): Unit = {
    if (state.nonMember(cmd.userId)) {
      sender() ! notMember
    } else {
      persist(UserLeft(Instant.now, cmd.userId)) { evt ⇒
        // no commit here. it will be after service message sent

        val dateMillis = evt.ts.toEpochMilli

        val updateObsolete = UpdateGroupUserLeaveObsolete(groupId, cmd.userId, dateMillis, cmd.randomId)

        // TODO: merge, they are almost identical
        val leftUserUpdatesNew =
          if (state.groupType.isChannel) List(
            UpdateGroupCanViewMembersChanged(groupId, canViewMembers = false),
            UpdateGroupCanEditInfoChanged(groupId, canEditGroup = false),
            UpdateGroupCanEditUsernameChanged(groupId, canEditUsername = false),
            UpdateGroupCanEditAdminsChanged(groupId, canAssignAdmins = false),
            UpdateGroupCanViewAdminsChanged(groupId, canViewAdmins = false),
            UpdateGroupCanEditAdminSettingsChanged(groupId, canEditAdminSettings = false),
            UpdateGroupCanInviteMembersChanged(groupId, canInviteMembers = false)
          )
          else List(
            UpdateGroupCanEditInfoChanged(groupId, canEditGroup = false),
            UpdateGroupCanEditUsernameChanged(groupId, canEditUsername = false),
            UpdateGroupCanEditAdminsChanged(groupId, canAssignAdmins = false),
            UpdateGroupCanViewAdminsChanged(groupId, canViewAdmins = false),
            UpdateGroupCanEditAdminSettingsChanged(groupId, canEditAdminSettings = false),
            UpdateGroupCanViewMembersChanged(groupId, canViewMembers = false),
            UpdateGroupMembersUpdated(groupId, members = Vector.empty),
            UpdateGroupCanInviteMembersChanged(groupId, canInviteMembers = false)
          )

        val membersUpdateNew =
          if (state.groupType.isChannel) { // if channel, or group is big enough
            UpdateGroupMembersCountChanged(
              groupId,
              membersCount = state.membersCount - 1
            )
          } else {
            UpdateGroupMembersUpdated(
              groupId,
              members = state.members.filterNot(_._1 == cmd.userId).values.map(_.asStruct).toVector
            )
          }

        val serviceMessage = GroupServiceMessages.userLeft

        //TODO: remove deprecated. GroupInviteTokenRepo don't have replacement yet.
        db.run(
          for {
            _ ← GroupUserRepo.delete(groupId, cmd.userId)
            _ ← GroupInviteTokenRepo.revoke(groupId, cmd.userId)
          } yield ()
        )

        val leaveGROUPUpdates: Future[SeqStateDate] =
          for {
            // push updated members list to all group members
            _ ← seqUpdExt.broadcastPeopleUpdate(
              state.memberIds - cmd.userId,
              membersUpdateNew
            )

            // send service message
            SeqStateDate(_, _, date) ← dialogExt.sendServerMessage(
              apiGroupPeer,
              senderUserId = cmd.userId,
              senderAuthId = cmd.authId,
              randomId = cmd.randomId,
              message = serviceMessage,
              deliveryTag = Some(Optimization.GroupV2)
            )

            // push left user that he is no longer a member
            SeqState(seq, state) ← seqUpdExt.deliverClientUpdate(
              userId = cmd.userId,
              authId = cmd.authId,
              update = UpdateGroupMemberChanged(groupId, isMember = false)
            )

            // push left user updates
            // • with empty group members
            // • that he can't view and invite members
            _ ← FutureExt.ftraverse(leftUserUpdatesNew) { update ⇒
              seqUpdExt.deliverUserUpdate(userId = cmd.userId, update)
            }
          } yield SeqStateDate(seq, state, date)

        val leaveCHANNELUpdates: Future[SeqStateDate] =
          for {
            // push updated members count to all group members
            _ ← seqUpdExt.broadcastPeopleUpdate(
              state.memberIds - cmd.userId,
              membersUpdateNew
            )

            // push left user that he is no longer a member
            SeqState(seq, state) ← seqUpdExt.deliverClientUpdate(
              userId = cmd.userId,
              authId = cmd.authId,
              update = UpdateGroupMemberChanged(groupId, isMember = false)
            )

            // push left user updates that he has no group rights
            _ ← FutureExt.ftraverse(leftUserUpdatesNew) { update ⇒
              seqUpdExt.deliverUserUpdate(userId = cmd.userId, update)
            }
          } yield SeqStateDate(seq, state, dateMillis)

        // read this dialog by user that leaves group. don't wait for ack
        dialogExt.messageRead(apiGroupPeer, cmd.userId, 0L, dateMillis)
        val result: Future[SeqStateDate] = for {

          ///////////////////////////
          // Groups V1 API updates //
          ///////////////////////////

          _ ← seqUpdExt.broadcastClientUpdate(
            userId = cmd.userId,
            authId = cmd.authId,
            bcastUserIds = state.memberIds + cmd.userId, // push this to other user's devices too. actually cmd.userId is still in state.memberIds
            update = updateObsolete,
            pushRules = seqUpdExt.pushRules(isFat = false, Some(PushTexts.Left), Seq(cmd.authId))
          )

          ///////////////////////////
          // Groups V2 API updates //
          ///////////////////////////

          seqStateDate ← if (state.groupType.isChannel) leaveCHANNELUpdates else leaveGROUPUpdates

        } yield seqStateDate

        result andThen { case _ ⇒ commit(evt) } pipeTo sender()
      }
    }
  }

  protected def kick(cmd: Kick): Unit = {
    if (!state.permissions.canKickMember(cmd.kickerUserId)) {
      sender() ! noPermission
    } else if (state.nonMember(cmd.kickedUserId)) {
      sender() ! notMember
    } else {
      persist(UserKicked(Instant.now, cmd.kickedUserId, cmd.kickerUserId)) { evt ⇒
        val newState = commit(evt)

        val dateMillis = evt.ts.toEpochMilli

        val updateObsolete = UpdateGroupUserKickObsolete(groupId, cmd.kickedUserId, cmd.kickerUserId, dateMillis, cmd.randomId)

        // TODO: merge, they are almost identical
        val kickedUserUpdatesNew: List[Update] =
          if (state.groupType.isChannel) List(
            UpdateGroupCanViewMembersChanged(groupId, canViewMembers = false),
            UpdateGroupCanEditInfoChanged(groupId, canEditGroup = false),
            UpdateGroupCanEditUsernameChanged(groupId, canEditUsername = false),
            UpdateGroupCanEditAdminsChanged(groupId, canAssignAdmins = false),
            UpdateGroupCanViewAdminsChanged(groupId, canViewAdmins = false),
            UpdateGroupCanEditAdminSettingsChanged(groupId, canEditAdminSettings = false),
            UpdateGroupCanInviteMembersChanged(groupId, canInviteMembers = false),
            UpdateGroupMemberChanged(groupId, isMember = false)
          )
          else List(
            UpdateGroupCanViewMembersChanged(groupId, canViewMembers = false),
            UpdateGroupCanEditInfoChanged(groupId, canEditGroup = false),
            UpdateGroupCanEditUsernameChanged(groupId, canEditUsername = false),
            UpdateGroupCanEditAdminsChanged(groupId, canAssignAdmins = false),
            UpdateGroupCanViewAdminsChanged(groupId, canViewAdmins = false),
            UpdateGroupCanEditAdminSettingsChanged(groupId, canEditAdminSettings = false),
            UpdateGroupCanInviteMembersChanged(groupId, canInviteMembers = false),
            UpdateGroupMembersUpdated(groupId, members = Vector.empty),
            UpdateGroupMemberChanged(groupId, isMember = false)
          )

        val membersUpdateNew: Update =
          if (newState.groupType.isChannel) { // if channel, or group is big enough
            UpdateGroupMembersCountChanged(
              groupId,
              membersCount = newState.membersCount
            )
          } else {
            UpdateGroupMembersUpdated(
              groupId,
              members = newState.members.values.map(_.asStruct).toVector
            )
          }

        val serviceMessage = GroupServiceMessages.userKicked(cmd.kickedUserId)

        //TODO: remove deprecated. GroupInviteTokenRepo don't have replacement yet.
        db.run(
          for {
            _ ← GroupUserRepo.delete(groupId, cmd.kickedUserId)
            _ ← GroupInviteTokenRepo.revoke(groupId, cmd.kickedUserId)
          } yield ()
        )

        val kickGROUPUpdates: Future[SeqStateDate] =
          for {
            // push updated members list to all group members. Don't push to kicked user!
            SeqState(seq, state) ← seqUpdExt.broadcastClientUpdate(
              userId = cmd.kickerUserId,
              authId = cmd.kickerAuthId,
              bcastUserIds = newState.memberIds - cmd.kickerUserId,
              update = membersUpdateNew
            )

            SeqStateDate(_, _, date) ← dialogExt.sendServerMessage(
              apiGroupPeer,
              senderUserId = cmd.kickerUserId,
              senderAuthId = cmd.kickerAuthId,
              randomId = cmd.randomId,
              message = serviceMessage,
              deliveryTag = Some(Optimization.GroupV2)
            )

            // push kicked user updates
            // • with empty group members
            // • that he is no longer a member of group
            // • that he can't view and invite members
            _ ← FutureExt.ftraverse(kickedUserUpdatesNew) { update ⇒
              seqUpdExt.deliverUserUpdate(userId = cmd.kickedUserId, update)
            }
          } yield SeqStateDate(seq, state, date)

        val kickCHANNELUpdates: Future[SeqStateDate] =
          for {
            // push updated members count to all group members. Don't push to kicked user!
            SeqState(seq, state) ← seqUpdExt.broadcastClientUpdate(
              userId = cmd.kickerUserId,
              authId = cmd.kickerAuthId,
              bcastUserIds = newState.memberIds - cmd.kickerUserId,
              update = membersUpdateNew
            )

            // push service message to kicker and kicked users.
            _ ← seqUpdExt.broadcastPeopleUpdate(
              userIds = Set(cmd.kickedUserId, cmd.kickerUserId),
              update = serviceMessageUpdate(
                cmd.kickerUserId,
                dateMillis,
                cmd.randomId,
                serviceMessage
              ),
              deliveryTag = Some(Optimization.GroupV2)
            )

            // push kicked user updates that he has no group rights
            _ ← FutureExt.ftraverse(kickedUserUpdatesNew) { update ⇒
              seqUpdExt.deliverUserUpdate(userId = cmd.kickedUserId, update)
            }
          } yield SeqStateDate(seq, state, dateMillis)

        // read this dialog by kicked user. don't wait for ack
        dialogExt.messageRead(apiGroupPeer, cmd.kickedUserId, 0L, dateMillis)
        val result: Future[SeqStateDate] = for {
          ///////////////////////////
          // Groups V1 API updates //
          ///////////////////////////

          _ ← seqUpdExt.broadcastClientUpdate(
            userId = cmd.kickerUserId,
            authId = cmd.kickerAuthId,
            bcastUserIds = newState.memberIds,
            update = updateObsolete,
            pushRules = seqUpdExt.pushRules(isFat = false, Some(PushTexts.Kicked), Seq(cmd.kickerAuthId))
          )

          ///////////////////////////
          // Groups V2 API updates //
          ///////////////////////////

          seqStateDate ← if (state.groupType.isChannel) kickCHANNELUpdates else kickGROUPUpdates

        } yield seqStateDate

        result pipeTo sender()
      }
    }
  }

  protected def updateAvatar(cmd: UpdateAvatar): Unit = {
    if (!state.permissions.canEditInfo(cmd.clientUserId)) {
      sender() ! noPermission
    } else {
      persist(AvatarUpdated(Instant.now, cmd.avatar)) { evt ⇒
        val newState = commit(evt)

        val dateMillis = evt.ts.toEpochMilli
        val apiAvatar: Option[ApiAvatar] = cmd.avatar
        val memberIds = newState.memberIds

        val updateNew = UpdateGroupAvatarChanged(groupId, apiAvatar)
        val updateObsolete = UpdateGroupAvatarChangedObsolete(groupId, cmd.clientUserId, apiAvatar, dateMillis, cmd.randomId)
        val serviceMessage = GroupServiceMessages.changedAvatar(apiAvatar)

        db.run(AvatarDataRepo.createOrUpdate(getAvatarData(cmd.avatar)))
        val result: Future[UpdateAvatarAck] = for {
          ///////////////////////////
          // Groups V1 API updates //
          ///////////////////////////

          _ ← seqUpdExt.broadcastClientUpdate(cmd.clientUserId, cmd.clientAuthId, memberIds, updateObsolete)

          ///////////////////////////
          // Groups V2 API updates //
          ///////////////////////////

          SeqState(seq, state) ← seqUpdExt.broadcastClientUpdate(
            userId = cmd.clientUserId,
            authId = cmd.clientAuthId,
            bcastUserIds = memberIds - cmd.clientUserId,
            update = updateNew
          )
          SeqStateDate(_, _, date) ← dialogExt.sendServerMessage(
            apiGroupPeer,
            senderUserId = cmd.clientUserId,
            senderAuthId = cmd.clientAuthId,
            randomId = cmd.randomId,
            message = serviceMessage,
            deliveryTag = Some(Optimization.GroupV2)
          )
        } yield UpdateAvatarAck(apiAvatar).withSeqStateDate(SeqStateDate(seq, state, date))

        result pipeTo sender()
      }
    }
  }

  protected def updateTitle(cmd: UpdateTitle): Unit = {
    val title = cmd.title
    if (!state.permissions.canEditInfo(cmd.clientUserId)) {
      sender() ! noPermission
    } else if (!isValidTitle(title)) {
      sender() ! Status.Failure(InvalidTitle)
    } else {
      persist(TitleUpdated(Instant.now(), title)) { evt ⇒
        val newState = commit(evt)

        val dateMillis = evt.ts.toEpochMilli
        val memberIds = newState.memberIds

        val updateNew = UpdateGroupTitleChanged(groupId, title)
        val updateObsolete = UpdateGroupTitleChangedObsolete(
          groupId,
          userId = cmd.clientUserId,
          title = title,
          date = dateMillis,
          randomId = cmd.randomId
        )
        val serviceMessage = GroupServiceMessages.changedTitle(title)
        val pushRules = seqUpdExt.pushRules(isFat = false, Some(PushTexts.TitleChanged))

        //TODO: remove deprecated
        db.run(GroupRepo.updateTitle(groupId, title, cmd.clientUserId, cmd.randomId, date = evt.ts))

        val result: Future[SeqStateDate] = for {

          ///////////////////////////
          // Groups V1 API updates //
          ///////////////////////////

          _ ← seqUpdExt.broadcastClientUpdate(
            cmd.clientUserId,
            cmd.clientAuthId,
            memberIds - cmd.clientUserId,
            updateObsolete,
            pushRules
          )

          ///////////////////////////
          // Groups V2 API updates //
          ///////////////////////////

          SeqState(seq, state) ← seqUpdExt.broadcastClientUpdate(
            userId = cmd.clientUserId,
            authId = cmd.clientAuthId,
            bcastUserIds = memberIds - cmd.clientUserId,
            update = updateNew,
            pushRules = pushRules
          )
          SeqStateDate(_, _, date) ← dialogExt.sendServerMessage(
            apiGroupPeer,
            senderUserId = cmd.clientUserId,
            senderAuthId = cmd.clientAuthId,
            randomId = cmd.randomId,
            message = serviceMessage,
            deliveryTag = Some(Optimization.GroupV2)
          )
        } yield SeqStateDate(seq, state, date)

        result pipeTo sender()
      }

    }
  }

  protected def updateTopic(cmd: UpdateTopic): Unit = {
    def isValidTopic(topic: Option[String]) = topic.forall(_.length < 255)

    val topic = trimToEmpty(cmd.topic)

    if (!state.permissions.canEditInfo(cmd.clientUserId)) {
      sender() ! noPermission
    } else if (!isValidTopic(topic)) {
      sender() ! Status.Failure(TopicTooLong)
    } else {
      persist(TopicUpdated(Instant.now, topic)) { evt ⇒
        val newState = commit(evt)

        val dateMillis = evt.ts.toEpochMilli
        val memberIds = newState.memberIds

        val updateNew = UpdateGroupTopicChanged(groupId, topic)
        val updateObsolete = UpdateGroupTopicChangedObsolete(
          groupId,
          randomId = cmd.randomId,
          userId = cmd.clientUserId,
          topic = topic,
          date = dateMillis
        )
        val serviceMessage = GroupServiceMessages.changedTopic(topic)
        val pushRules = seqUpdExt.pushRules(isFat = false, Some(PushTexts.TopicChanged))

        //TODO: remove deprecated
        db.run(GroupRepo.updateTopic(groupId, topic))

        val result: Future[SeqStateDate] = for {

          ///////////////////////////
          // Groups V1 API updates //
          ///////////////////////////

          _ ← seqUpdExt.broadcastClientUpdate(
            cmd.clientUserId,
            cmd.clientAuthId,
            memberIds - cmd.clientUserId,
            updateObsolete,
            pushRules
          )

          ///////////////////////////
          // Groups V2 API updates //
          ///////////////////////////

          SeqState(seq, state) ← seqUpdExt.broadcastClientUpdate(
            userId = cmd.clientUserId,
            authId = cmd.clientAuthId,
            bcastUserIds = memberIds - cmd.clientUserId,
            update = updateNew,
            pushRules = pushRules
          )
          SeqStateDate(_, _, date) ← dialogExt.sendServerMessage(
            apiGroupPeer,
            senderUserId = cmd.clientUserId,
            senderAuthId = cmd.clientAuthId,
            randomId = cmd.randomId,
            message = serviceMessage,
            deliveryTag = Some(Optimization.GroupV2)
          )
        } yield SeqStateDate(seq, state, date)

        result pipeTo sender()
      }
    }
  }

  protected def updateAbout(cmd: UpdateAbout): Unit = {
    def isValidAbout(about: Option[String]) = about.forall(_.length < 255)

    val about = trimToEmpty(cmd.about)

    if (!state.permissions.canEditInfo(cmd.clientUserId)) {
      sender() ! noPermission
    } else if (!isValidAbout(about)) {
      sender() ! Status.Failure(AboutTooLong)
    } else {

      persist(AboutUpdated(Instant.now, about)) { evt ⇒
        val newState = commit(evt)

        val memberIds = newState.memberIds

        val updateNew = UpdateGroupAboutChanged(groupId, about)
        val updateObsolete = UpdateGroupAboutChangedObsolete(groupId, about)
        val serviceMessage = GroupServiceMessages.changedAbout(about)
        val pushRules = seqUpdExt.pushRules(isFat = false, Some(PushTexts.TopicChanged))

        //TODO: remove deprecated
        db.run(GroupRepo.updateAbout(groupId, about))

        val result: Future[SeqStateDate] = for {

          ///////////////////////////
          // Groups V1 API updates //
          ///////////////////////////

          _ ← seqUpdExt.broadcastClientUpdate(
            cmd.clientUserId,
            cmd.clientAuthId,
            memberIds - cmd.clientUserId,
            updateObsolete,
            pushRules
          )

          ///////////////////////////
          // Groups V2 API updates //
          ///////////////////////////

          SeqState(seq, state) ← seqUpdExt.broadcastClientUpdate(
            userId = cmd.clientUserId,
            authId = cmd.clientAuthId,
            bcastUserIds = memberIds - cmd.clientUserId,
            update = updateNew,
            pushRules = pushRules
          )
        } yield SeqStateDate(seq, state, evt.ts.toEpochMilli)

        result pipeTo sender()
      }
    }
  }

  protected def updateShortName(cmd: UpdateShortName): Unit = {
    def isValidShortName(shortName: Option[String]) = shortName forall StringUtils.validGlobalName

    val oldShortName = state.shortName
    val newShortName = trimToEmpty(cmd.shortName)

    if (!state.permissions.canEditShortName(cmd.clientUserId)) {
      sender() ! Status.Failure(NotOwner)
    } else if (!isValidShortName(newShortName)) {
      sender() ! Status.Failure(InvalidShortName)
    } else if (oldShortName == newShortName) {
      seqUpdExt.getSeqState(cmd.clientUserId, cmd.clientAuthId) pipeTo sender()
    } else {
      val replyTo = sender()

      val existsFu = newShortName map { name ⇒
        globalNamesStorage.exists(name)
      } getOrElse FastFuture.successful(false)

      //TODO: timeout for this
      onSuccess(existsFu) { exists ⇒
        if (exists) {
          replyTo ! Status.Failure(ShortNameTaken)
        } else {
          persist(ShortNameUpdated(Instant.now, newShortName)) { evt ⇒
            val newState = commit(evt)

            val memberIds = newState.memberIds

            val result: Future[SeqState] = for {
              _ ← globalNamesStorage.updateOrRemove(
                oldShortName,
                newShortName,
                GlobalNameOwner(OwnerType.Group, groupId)
              )
              seqState ← seqUpdExt.broadcastClientUpdate(
                userId = cmd.clientUserId,
                authId = cmd.clientAuthId,
                bcastUserIds = memberIds - cmd.clientUserId,
                update = UpdateGroupShortNameChanged(groupId, newShortName)
              )
            } yield seqState

            result pipeTo replyTo
          }
        }
      }
    }
  }

  protected def revokeIntegrationToken(cmd: RevokeIntegrationToken): Unit = {
    if (!(state.isAdmin(cmd.clientUserId) || state.isOwner(cmd.clientUserId))) {
      sender() ! notAdmin
    } else {
      val oldToken = state.bot.map(_.token)
      val newToken = ACLUtils.accessToken()

      persist(IntegrationTokenRevoked(Instant.now, newToken)) { evt ⇒
        val newState = commit(evt)

        //TODO: remove deprecated
        db.run(GroupBotRepo.updateToken(groupId, newToken))

        val result: Future[RevokeIntegrationTokenAck] = for {
          _ ← oldToken match {
            case Some(token) ⇒ integrationStorage.deleteToken(token)
            case None        ⇒ FastFuture.successful(())
          }
          _ ← integrationStorage.upsertToken(newToken, groupId)
        } yield RevokeIntegrationTokenAck(newToken)

        result pipeTo sender()
      }
    }
  }

  protected def makeUserAdmin(cmd: MakeUserAdmin): Unit = {
    if (!state.permissions.canEditAdmins(cmd.clientUserId)) {
      sender() ! noPermission
    } else if (state.nonMember(cmd.candidateUserId)) {
      sender() ! Status.Failure(NotAMember)
    } else if (state.isAdmin(cmd.candidateUserId)) {
      sender() ! Status.Failure(UserAlreadyAdmin)
    } else {
      persist(AdminStatusChanged(Instant.now, cmd.candidateUserId, isAdmin = true)) { evt ⇒
        val newState = commit(evt)

        val dateMillis = evt.ts.toEpochMilli
        val memberIds = newState.memberIds
        val members = newState.members.values.map(_.asStruct).toVector

        val updateAdmin = UpdateGroupMemberAdminChanged(groupId, cmd.candidateUserId, isAdmin = true)
        val updateMembers = UpdateGroupMembersUpdated(groupId, members)
        // now this user is admin, change edit rules for admins
        val updateCanEdit = UpdateGroupCanEditInfoChanged(groupId, canEditGroup = newState.adminSettings.canAdminsEditGroupInfo)

        val updateObsolete = UpdateGroupMembersUpdateObsolete(groupId, members)

        //TODO: remove deprecated
        db.run(GroupUserRepo.makeAdmin(groupId, cmd.candidateUserId))

        val adminGROUPUpdates: Future[SeqStateDate] =
          for {
            _ ← seqUpdExt.broadcastPeopleUpdate(
              userIds = memberIds + cmd.clientUserId,
              updateAdmin
            )
            SeqState(seq, state) ← seqUpdExt.broadcastClientUpdate(
              cmd.clientUserId,
              cmd.clientAuthId,
              memberIds - cmd.clientUserId,
              updateMembers
            )
          } yield SeqStateDate(seq, state, dateMillis)

        val adminCHANNELUpdates: Future[SeqStateDate] =
          for {
            // push admin changed to all
            _ ← seqUpdExt.broadcastPeopleUpdate(
              userIds = memberIds + cmd.clientUserId,
              updateAdmin
            )
            // push changed members to admins and fresh admin
            SeqState(seq, state) ← seqUpdExt.broadcastClientUpdate(
              cmd.clientUserId,
              cmd.clientAuthId,
              (newState.adminIds - cmd.clientUserId) + cmd.candidateUserId,
              updateMembers
            )
          } yield SeqStateDate(seq, state, dateMillis)

        val result: Future[(Vector[ApiMember], SeqStateDate)] = for {

          ///////////////////////////
          // Groups V1 API updates //
          ///////////////////////////

          _ ← seqUpdExt.broadcastClientUpdate(
            cmd.clientUserId,
            cmd.clientAuthId,
            memberIds - cmd.clientUserId,
            updateObsolete
          )

          ///////////////////////////
          // Groups V2 API updates //
          ///////////////////////////
          _ ← seqUpdExt.deliverUserUpdate(
            userId = cmd.candidateUserId,
            update = updateCanEdit
          )
          seqStateDate ← if (state.groupType.isChannel) adminCHANNELUpdates else adminGROUPUpdates

        } yield (members, seqStateDate)

        result pipeTo sender()
      }
    }
  }

  protected def dismissUserAdmin(cmd: DismissUserAdmin): Unit = {
    if (!state.permissions.canEditAdmins(cmd.clientUserId)) {
      sender() ! noPermission
    } else if (state.nonMember(cmd.targetUserId)) {
      sender() ! Status.Failure(NotAMember)
    } else if (!state.isAdmin(cmd.targetUserId)) {
      sender() ! Status.Failure(UserAlreadyNotAdmin)
    } else {
      persist(AdminStatusChanged(Instant.now, cmd.targetUserId, isAdmin = false)) { evt ⇒
        val newState = commit(evt)

        val dateMillis = evt.ts.toEpochMilli
        val memberIds = newState.memberIds
        val members = newState.members.values.map(_.asStruct).toVector

        val updateAdmin = UpdateGroupMemberAdminChanged(groupId, cmd.targetUserId, isAdmin = false)
        val updateMembers = UpdateGroupMembersUpdated(groupId, members)
        // now this user is not admin, change edit rules to plain members
        val updateCanEdit = UpdateGroupCanEditInfoChanged(groupId, canEditGroup = newState.adminSettings.canMembersEditGroupInfo)

        val updateObsolete = UpdateGroupMembersUpdateObsolete(groupId, members)

        //TODO: remove deprecated
        db.run(GroupUserRepo.dismissAdmin(groupId, cmd.targetUserId))

        val adminGROUPUpdates: Future[SeqState] =
          for {
            // push admin changed to all
            _ ← seqUpdExt.broadcastPeopleUpdate(
              userIds = memberIds + cmd.clientUserId,
              updateAdmin
            )
            // push changed members to all users
            seqState ← seqUpdExt.broadcastClientUpdate(
              cmd.clientUserId,
              cmd.clientAuthId,
              memberIds - cmd.clientUserId,
              updateMembers
            )
          } yield seqState

        val adminCHANNELUpdates: Future[SeqState] =
          for {
            // push admin changed to all
            _ ← seqUpdExt.broadcastPeopleUpdate(
              userIds = memberIds + cmd.clientUserId,
              updateAdmin
            )
            // push changed members to admins and fresh admin
            seqState ← seqUpdExt.broadcastClientUpdate(
              cmd.clientUserId,
              cmd.clientAuthId,
              newState.adminIds - cmd.clientUserId,
              updateMembers
            )
          } yield seqState

        val result: Future[SeqState] = for {

          ///////////////////////////
          // Groups V1 API updates //
          ///////////////////////////

          _ ← seqUpdExt.broadcastClientUpdate(
            cmd.clientUserId,
            cmd.clientAuthId,
            memberIds - cmd.clientUserId,
            updateObsolete
          )

          ///////////////////////////
          // Groups V2 API updates //
          ///////////////////////////
          _ ← seqUpdExt.deliverUserUpdate(
            userId = cmd.targetUserId,
            update = updateCanEdit
          )
          seqState ← if (state.groupType.isChannel) adminCHANNELUpdates else adminGROUPUpdates

        } yield seqState

        result pipeTo sender()
      }
    }
  }

  protected def transferOwnership(cmd: TransferOwnership): Unit = {
    if (!state.isOwner(cmd.clientUserId)) {
      sender() ! Status.Failure(CommonErrors.Forbidden)
    } else {
      persist(OwnerChanged(Instant.now, cmd.newOwnerId)) { evt ⇒
        val newState = commit(evt)
        val memberIds = newState.memberIds

        val result: Future[SeqState] = for {
          seqState ← seqUpdExt.broadcastClientUpdate(
            userId = cmd.clientUserId,
            authId = cmd.clientAuthId,
            bcastUserIds = memberIds - cmd.clientUserId,
            update = UpdateGroupOwnerChanged(groupId, cmd.newOwnerId),
            pushRules = seqUpdExt.pushRules(isFat = false, None)
          )
        } yield seqState

        result pipeTo sender()
      }
    }
  }

  protected def updateAdminSettings(cmd: UpdateAdminSettings): Unit = {
    if (!state.permissions.canEditAdminSettings(cmd.clientUserId)) {
      sender() ! Status.Failure(NotAdmin)
    } else {
      val settOld = state.adminSettings

      persist(AdminSettingsUpdated(Instant.now, cmd.settingsBitMask)) { evt ⇒
        val newState = commit(evt)
        val settNew = newState.adminSettings

        val (membersUpdates, adminsUpdates) = {
          // push to all members except admins and owner
          val showAdminToMembers = PartialFunction.condOpt(settOld.showAdminsToMembers != settNew.showAdminsToMembers) {
            case true ⇒ UpdateGroupCanViewAdminsChanged(groupId, canViewAdmins = settNew.showAdminsToMembers)
          }

          // push to all members except admins and owner
          val canMembersInvite = PartialFunction.condOpt(settOld.canMembersInvite != settNew.canMembersInvite) {
            case true ⇒ UpdateGroupCanInviteMembersChanged(groupId, canInviteMembers = settNew.canMembersInvite)
          }

          // push to all members except admins and owner
          val canMembersEditGroupInfo = PartialFunction.condOpt(settOld.canMembersEditGroupInfo != settNew.canMembersEditGroupInfo) {
            case true ⇒ UpdateGroupCanEditInfoChanged(groupId, canEditGroup = settNew.canMembersEditGroupInfo)
          }

          // push to admins only
          val canAdminsEditGroupInfo = PartialFunction.condOpt(settOld.canAdminsEditGroupInfo != settNew.canAdminsEditGroupInfo) {
            case true ⇒ UpdateGroupCanEditInfoChanged(groupId, canEditGroup = settNew.canAdminsEditGroupInfo)
          }

          (
            List(showAdminToMembers, canMembersInvite, canMembersEditGroupInfo).flatten[Update],
            List(canAdminsEditGroupInfo).flatten[Update]
          )
        }

        val plainMemberIds = (newState.memberIds - newState.ownerUserId) -- newState.adminIds
        val adminsOnlyIds = newState.adminIds - newState.ownerUserId

        val result: Future[UpdateAdminSettingsAck] = for {
          // deliver updates about settings changed to plain group members
          _ ← FutureExt.ftraverse(membersUpdates) { update ⇒
            seqUpdExt.broadcastPeopleUpdate(userIds = plainMemberIds, update)
          }
          // deliver updates about settings changed to plain group members
          _ ← FutureExt.ftraverse(membersUpdates) { update ⇒
            seqUpdExt.broadcastPeopleUpdate(userIds = adminsOnlyIds, update)
          }
        } yield UpdateAdminSettingsAck()

        result pipeTo sender()
      }
    }
  }

  private def serviceMessageUpdate(senderUserId: Int, date: Long, randomId: Long, message: ApiServiceMessage) =
    UpdateMessage(
      peer = apiGroupPeer,
      senderUserId = senderUserId,
      date = date,
      randomId = randomId,
      message = message,
      attributes = None,
      quotedMessage = None
    )

  private def trimToEmpty(s: Option[String]): Option[String] =
    s map (_.trim) filter (_.nonEmpty)

  private def getAvatarData(avatar: Option[Avatar]): AvatarData =
    avatar
      .map(ImageUtils.getAvatarData(AvatarData.OfGroup, groupId, _))
      .getOrElse(AvatarData.empty(AvatarData.OfGroup, groupId.toLong))

  private def isValidTitle(title: String) = title.nonEmpty && title.length < 255

  // Updates that will be sent to user, when he enters group.
  // Helps clients that have this group to refresh it's data.
  // TODO: review when channels will be added
  private def refreshGroupUpdates(newState: GroupState, userId: Int): List[Update] = List(
    UpdateGroupMemberChanged(groupId, isMember = true),
    UpdateGroupAboutChanged(groupId, newState.about),
    UpdateGroupAvatarChanged(groupId, newState.avatar),
    UpdateGroupTopicChanged(groupId, newState.topic),
    UpdateGroupTitleChanged(groupId, newState.title),
    UpdateGroupOwnerChanged(groupId, newState.ownerUserId),
    UpdateGroupCanViewMembersChanged(groupId, canViewMembers = newState.permissions.canViewMembers(userId)),
    UpdateGroupCanInviteMembersChanged(groupId, canInviteMembers = true) // TODO: figure out right value
  //    UpdateGroupExtChanged(groupId, newState.extension) //TODO: figure out and fix
  //          if(bigGroup) UpdateGroupMembersCountChanged(groupId, newState.extension)
  )

}
