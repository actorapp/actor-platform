package im.actor.server.group

import java.time.Instant

import akka.actor.Status
import akka.pattern.pipe
import akka.http.scaladsl.util.FastFuture
import im.actor.api.rpc.{ PeersImplicits, Update }
import im.actor.api.rpc.groups._
import im.actor.api.rpc.messaging.UpdateChatClear
import im.actor.concurrent.FutureExt
import im.actor.server.CommonErrors
import im.actor.server.acl.ACLUtils
import im.actor.server.group.GroupCommands.{ DeleteGroup, DismissUserAdmin, MakeHistoryShared, MakeUserAdmin, RevokeIntegrationToken, RevokeIntegrationTokenAck, TransferOwnership, UpdateAdminSettings, UpdateAdminSettingsAck }
import im.actor.server.group.GroupErrors.{ NotAMember, NotAdmin, UserAlreadyAdmin, UserAlreadyNotAdmin }
import im.actor.server.group.GroupEvents.{ AdminSettingsUpdated, AdminStatusChanged, GroupDeleted, HistoryBecameShared, IntegrationTokenRevoked, OwnerChanged }
import im.actor.server.persist.{ GroupBotRepo, GroupInviteTokenRepo, GroupUserRepo, HistoryMessageRepo }
import im.actor.server.sequence.{ SeqState, SeqStateDate }

import scala.concurrent.Future

private[group] trait AdminCommandHandlers extends GroupsImplicits {
  this: GroupProcessor ⇒

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

        val prevOwnerUpdates = List(
          UpdateGroupCanLeaveChanged(groupId, canLeaveChanged = true),
          UpdateGroupCanDeleteChanged(groupId, canDeleteChanged = false)
        )

        val newOwnerUpdates = List(
          UpdateGroupCanLeaveChanged(groupId, canLeaveChanged = false),
          UpdateGroupCanDeleteChanged(groupId, canDeleteChanged = true)
        )

        val result: Future[SeqState] = for {
          // push updates to previous owner
          _ ← FutureExt.ftraverse(prevOwnerUpdates) { update ⇒
            seqUpdExt.deliverUserUpdate(cmd.clientUserId, update)
          }
          // push updates to new owner
          _ ← FutureExt.ftraverse(newOwnerUpdates) { update ⇒
            seqUpdExt.deliverUserUpdate(cmd.newOwnerId, update)
          }
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

  protected def makeHistoryShared(cmd: MakeHistoryShared): Unit = {
    if (!state.permissions.canMakeHistoryShared(cmd.clientUserId)) {
      sender() ! noPermission
    } else {
      persist(HistoryBecameShared(Instant.now, cmd.clientUserId)) { evt ⇒
        val newState = commit(evt)
        log.debug("History of group {} became shared", groupId)

        val memberIds = newState.memberIds

        val result: Future[SeqState] = for {
          seqState ← seqUpdExt.broadcastClientUpdate(
            userId = cmd.clientUserId,
            authId = cmd.clientAuthId,
            bcastUserIds = memberIds - cmd.clientUserId,
            update = UpdateGroupHistoryShared(groupId)
          )
        } yield seqState

        result pipeTo sender()
      }
    }
  }

  protected def deleteGroup(cmd: DeleteGroup): Unit = {
    if (!state.permissions.canDelete(cmd.clientUserId)) {
      sender() ! noPermission
    } else {
      persist(GroupDeleted(Instant.now, cmd.clientUserId)) { evt ⇒
        val newState = commit(evt)

        val dateMillis = evt.ts.toEpochMilli
        val randomId = ACLUtils.randomLong()

        // TODO: add UpdateIsDeleted
        val deleteGroupMembersUpdates: Vector[Update] = Vector(
          UpdateGroupCanSendMessagesChanged(groupId, canSendMessages = false),
          UpdateGroupCanViewMembersChanged(groupId, canViewMembers = false),
          UpdateGroupCanEditInfoChanged(groupId, canEditGroup = false),
          UpdateGroupCanEditUsernameChanged(groupId, canEditUsername = false),
          UpdateGroupCanEditAdminsChanged(groupId, canAssignAdmins = false),
          UpdateGroupCanViewAdminsChanged(groupId, canViewAdmins = false),
          UpdateGroupCanEditAdminSettingsChanged(groupId, canEditAdminSettings = false),
          UpdateGroupCanInviteMembersChanged(groupId, canInviteMembers = false),
          UpdateGroupCanInviteViaLink(groupId, canInviteViaLink = false),
          UpdateGroupCanLeaveChanged(groupId, canLeaveChanged = false),
          UpdateGroupCanDeleteChanged(groupId, canDeleteChanged = false),
          UpdateGroupMemberChanged(groupId, isMember = false),
          // if channel, or group is big enough
          if (newState.groupType.isChannel)
            UpdateGroupMembersCountChanged(groupId, membersCount = 0)
          else
            UpdateGroupMembersUpdated(groupId, members = Vector.empty)
        )

        //TODO: remove deprecated. GroupInviteTokenRepo don't have replacement yet.
        newState.memberIds foreach { userId ⇒
          db.run(
            for {
              _ ← GroupUserRepo.delete(groupId, userId)
              _ ← GroupInviteTokenRepo.revoke(groupId, userId)
            } yield ()
          )
        }

        val result: Future[SeqState] = for {
          _ ← db.run(HistoryMessageRepo.deleteAll(cmd.clientUserId, apiGroupPeer.asModel))

          ///////////////////////////
          // Groups V1 API updates //
          ///////////////////////////

          // push all members updates about other members left group
          _ ← FutureExt.ftraverse(newState.memberIds.toSeq) { userId ⇒
            seqUpdExt.broadcastPeopleUpdate(
              userIds = newState.memberIds - userId,
              update = UpdateGroupUserLeaveObsolete(groupId, userId, dateMillis, randomId)
            )
          }

          ///////////////////////////
          // Groups V2 API updates //
          ///////////////////////////
          _ ← Future.traverse(deleteGroupMembersUpdates) { update ⇒
            seqUpdExt.broadcastPeopleUpdate(newState.memberIds, update)
          }
          seqState ← seqUpdExt.broadcastClientUpdate(
            cmd.clientUserId,
            cmd.clientAuthId,
            bcastUserIds = state.memberIds - cmd.clientUserId,
            update = UpdateChatClear(apiGroupPeer)
          )
        } yield seqState

        result pipeTo sender()
      }
    }
  }

}
