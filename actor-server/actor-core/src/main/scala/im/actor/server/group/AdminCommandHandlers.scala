package im.actor.server.group

import java.time.Instant

import akka.actor.Status
import akka.pattern.pipe
import akka.http.scaladsl.util.FastFuture
import com.github.ghik.silencer.silent
import im.actor.api.rpc.Update
import im.actor.api.rpc.groups._
import im.actor.api.rpc.messaging.UpdateChatClear
import im.actor.concurrent.FutureExt
import im.actor.server.CommonErrors
import im.actor.server.acl.ACLUtils
import im.actor.server.dialog.HistoryUtils
import im.actor.server.group.GroupCommands.{ DeleteGroup, DismissUserAdmin, MakeHistoryShared, MakeUserAdmin, RevokeIntegrationToken, RevokeIntegrationTokenAck, TransferOwnership, UpdateAdminSettings, UpdateAdminSettingsAck }
import im.actor.server.group.GroupErrors.{ NotAMember, NotAdmin, UserAlreadyAdmin, UserAlreadyNotAdmin }
import im.actor.server.group.GroupEvents.{ AdminSettingsUpdated, AdminStatusChanged, GroupDeleted, HistoryBecameShared, IntegrationTokenRevoked, OwnerChanged }
import im.actor.server.names.{ GlobalNameOwner, OwnerType }
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
        db.run(GroupBotRepo.updateToken(groupId, newToken): @silent)

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

  // TODO: duplicate isBot check
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
        val updateMembers = UpdateGroupMembersUpdated(groupId, members) // don't push it!
        // now this user is admin, change edit rules for admins

        val updatePermissions = permissionsUpdates(cmd.candidateUserId, newState)
        //        val updateCanEdit = UpdateGroupCanEditInfoChanged(groupId, canEditGroup = newState.adminSettings.canAdminsEditGroupInfo)

        val updateObsolete = UpdateGroupMembersUpdateObsolete(groupId, members)

        //TODO: remove deprecated
        db.run(GroupUserRepo.makeAdmin(groupId, cmd.candidateUserId): @silent)

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
          _ ← FutureExt.ftraverse(updatePermissions) { update ⇒
            seqUpdExt.deliverUserUpdate(cmd.candidateUserId, update)
          }
          seqStateDate ← if (state.groupType.isChannel) adminCHANNELUpdates else adminGROUPUpdates

        } yield (members, seqStateDate)

        result pipeTo sender()
      }
    }
  }

  // TODO: duplicate isBot check
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

        val memberIds = newState.memberIds
        val members = newState.members.values.map(_.asStruct).toVector

        val updateAdmin = UpdateGroupMemberAdminChanged(groupId, cmd.targetUserId, isAdmin = false)
        val updatePermissions = permissionsUpdates(cmd.targetUserId, newState)

        val updateObsolete = UpdateGroupMembersUpdateObsolete(groupId, members)

        //TODO: remove deprecated
        db.run(GroupUserRepo.dismissAdmin(groupId, cmd.targetUserId): @silent)

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
          _ ← FutureExt.ftraverse(updatePermissions) { update ⇒
            seqUpdExt.deliverUserUpdate(cmd.targetUserId, update)
          }
          seqState ← seqUpdExt.broadcastClientUpdate(
            cmd.clientUserId,
            cmd.clientAuthId,
            memberIds - cmd.clientUserId,
            updateAdmin
          )
        } yield seqState

        result pipeTo sender()
      }
    }
  }

  // TODO: duplicate isBot check
  protected def transferOwnership(cmd: TransferOwnership): Unit = {
    if (!state.isOwner(cmd.clientUserId)) {
      sender() ! Status.Failure(CommonErrors.Forbidden)
    } else {
      persist(OwnerChanged(Instant.now, cmd.newOwnerId)) { evt ⇒
        val newState = commit(evt)
        val memberIds = newState.memberIds

        val updatePermissionsPrevOwner = permissionsUpdates(cmd.clientUserId, newState)
        val updatePermissionsNewOwner = permissionsUpdates(cmd.newOwnerId, newState)

        val result: Future[SeqState] = for {
          // push permission updates to previous owner
          _ ← FutureExt.ftraverse(updatePermissionsPrevOwner) { update ⇒
            seqUpdExt.deliverUserUpdate(cmd.clientUserId, update)
          }
          // push permission updates to new owner
          _ ← FutureExt.ftraverse(updatePermissionsNewOwner) { update ⇒
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
      sender() ! noPermission
    } else if (AdminSettings.fromBitMask(cmd.settingsBitMask) == state.adminSettings) {
      sender() ! UpdateAdminSettingsAck()
    } else {
      persist(AdminSettingsUpdated(Instant.now, cmd.settingsBitMask)) { evt ⇒
        val newState = commit(evt)

        //TODO: check if settings actually changed
        val result: Future[UpdateAdminSettingsAck] = for {
          // deliver permissions updates to all group members
          // maybe there is no need to generate updates for each user
          // three parts: members, admins, owner should be enough
          _ ← FutureExt.ftraverse(newState.memberIds.toSeq) { userId ⇒
            FutureExt.ftraverse(permissionsUpdates(userId, newState)) { update ⇒
              seqUpdExt.deliverUserUpdate(userId, update)
            }
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
      val exMemberIds = state.memberIds
      val exGlobalName = state.shortName
      val exGroupType = state.groupType
      val exHistoryShared = state.isHistoryShared
      val peer = apiGroupPeer.asModel

      persist(GroupDeleted(Instant.now, cmd.clientUserId)) { evt ⇒
        commit(evt)

        val ZeroPermissions = 0L

        val deleteGroupMembersUpdates: Vector[Update] =
          Vector(
            UpdateGroupMemberChanged(groupId, isMember = false),
            // if channel, or group is big enough
            (if (exGroupType.isChannel)
              UpdateGroupMembersCountChanged(groupId, membersCount = 0)
            else
              UpdateGroupMembersUpdated(groupId, members = Vector.empty)),
            UpdateGroupPermissionsChanged(groupId, ZeroPermissions),
            UpdateGroupFullPermissionsChanged(groupId, ZeroPermissions),
            UpdateGroupDeleted(groupId)
          )

        //TODO: remove deprecated. GroupInviteTokenRepo don't have replacement yet.
        exMemberIds foreach { userId ⇒
          db.run(
            for {
              _ ← GroupUserRepo.delete(groupId, userId): @silent
              _ ← GroupInviteTokenRepo.revoke(groupId, userId): @silent
            } yield ()
          )
        }

        val result: Future[SeqState] = for {
          // release global name of group
          _ ← globalNamesStorage.updateOrRemove(exGlobalName, newGlobalName = None, GlobalNameOwner(OwnerType.Group, groupId))

          // explicitly delete group history.
          // TODO: move to utility method
          _ ← if (exHistoryShared) {
            db.run(HistoryMessageRepo.deleteAll(HistoryUtils.SharedUserId, peer))
          } else {
            // for client user we delete history separately
            FutureExt.ftraverse((exMemberIds - cmd.clientUserId).toSeq) { userId ⇒
              db.run(HistoryMessageRepo.deleteAll(userId, peer))
            }
          }

          ///////////////////////////
          // Groups V1 API updates //
          ///////////////////////////

          // push all members updates about group members became empty
          _ ← seqUpdExt.broadcastPeopleUpdate(
            userIds = exMemberIds,
            update = UpdateGroupMembersUpdateObsolete(groupId, members = Vector.empty)
          )

          ///////////////////////////
          // Groups V2 API updates //
          ///////////////////////////

          // send all members update about group became empty(no members)
          _ ← Future.traverse(deleteGroupMembersUpdates) { update ⇒
            seqUpdExt.broadcastPeopleUpdate(exMemberIds, update)
          }

          // send all members except clientUserId `UpdateChatClear`
          _ ← seqUpdExt.broadcastPeopleUpdate(
            userIds = exMemberIds - cmd.clientUserId,
            update = UpdateChatClear(apiGroupPeer)
          )

          // delete dialog from client user's dialog list
          // history deletion happens inside
          seqState ← dialogExt.delete(cmd.clientUserId, cmd.clientAuthId, apiGroupPeer.asModel)
        } yield seqState

        result pipeTo sender()
      }
    }
  }

}
