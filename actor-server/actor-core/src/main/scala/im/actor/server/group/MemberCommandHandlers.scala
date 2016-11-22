package im.actor.server.group

import java.time.{ Instant, LocalDateTime, ZoneOffset }

import akka.actor.Status
import akka.http.scaladsl.util.FastFuture
import akka.pattern.pipe
import com.github.ghik.silencer.silent
import im.actor.api.rpc.Update
import im.actor.api.rpc.groups._
import im.actor.api.rpc.messaging.{ ApiServiceMessage, UpdateChatDropCache, UpdateMessage }
import im.actor.concurrent.FutureExt
import im.actor.server.acl.ACLUtils
import im.actor.server.group.GroupCommands.{ Invite, Join, Kick, Leave }
import im.actor.server.group.GroupErrors.CantLeaveGroup
import im.actor.server.group.GroupEvents.{ UserInvited, UserJoined, UserKicked, UserLeft }
import im.actor.server.persist.{ GroupInviteTokenRepo, GroupUserRepo }
import im.actor.server.sequence.{ Optimization, SeqState, SeqStateDate }

import scala.concurrent.Future

private[group] trait MemberCommandHandlers extends GroupsImplicits {
  this: GroupProcessor ⇒

  import im.actor.server.ApiConversions._

  protected def invite(cmd: Invite): Unit = {
    if (!state.permissions.canInviteMembers(cmd.inviterUserId)) {
      sender() ! noPermission
    } else if (state.isInvited(cmd.inviteeUserId)) {
      sender() ! Status.Failure(GroupErrors.UserAlreadyInvited)
    } else if (state.isMember(cmd.inviteeUserId)) {
      sender() ! Status.Failure(GroupErrors.UserAlreadyJoined)
    } else {
      val replyTo = sender()

      val isBlockedFu = checkIsBlocked(cmd.inviteeUserId, state.ownerUserId)

      onSuccess(isBlockedFu) { isBlocked ⇒
        if (isBlocked) {
          replyTo ! Status.Failure(GroupErrors.UserIsBanned)
        } else {
          val inviteeIsExUser = state.isExUser(cmd.inviteeUserId)

          persist(UserInvited(Instant.now, cmd.inviteeUserId, cmd.inviterUserId)) { evt ⇒
            val newState = commit(evt)

            val dateMillis = evt.ts.toEpochMilli
            val memberIds = newState.memberIds

            // TODO: unify isHistoryShared usage
            val inviteeUpdatesNew: Vector[Update] = {
              val optDrop = if (newState.isHistoryShared) Some(UpdateChatDropCache(apiGroupPeer)) else None
              optDrop ++: refreshGroupUpdates(newState, cmd.inviteeUserId)
            }

            // For groups with not async members we should push Diff for members, and all Members for invitee
            // For groups with async members we should push UpdateGroupMembersCountChanged for both invitee and members
            val (inviteeUpdateNew, membersUpdateNew): (Update, Update) =
              if (newState.isAsyncMembers) {
                val u = UpdateGroupMembersCountChanged(groupId, newState.membersCount)
                (u, u)
              } else {
                val apiMembers = newState.members.values.map(_.asStruct).toVector
                val inviteeMember = apiMembers.find(_.userId == cmd.inviteeUserId)

                (
                  UpdateGroupMembersUpdated(groupId, apiMembers),
                  UpdateGroupMemberDiff(
                    groupId,
                    addedMembers = inviteeMember.toVector,
                    membersCount = newState.membersCount,
                    removedUsers = Vector.empty
                  )
                )
              }

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
            db.run(GroupUserRepo.create(groupId, cmd.inviteeUserId, cmd.inviterUserId, evt.ts, None, isAdmin = false): @silent)

            def inviteGROUPUpdates: Future[SeqStateDate] =
              for {
                // push updated members list/count to inviteeUserId,
                // make it `FatSeqUpdate` if this user invited to group for first time.
                _ ← seqUpdExt.deliverUserUpdate(
                  userId = cmd.inviteeUserId,
                  update = inviteeUpdateNew,
                  pushRules = seqUpdExt.pushRules(isFat = !inviteeIsExUser, Some(PushTexts.invited(newState.groupType))),
                  deliveryId = s"invite_${groupId}_${cmd.randomId}"
                )

                // push all "refresh group" updates to inviteeUserId
                _ ← FutureExt.ftraverse(inviteeUpdatesNew) { update ⇒
                  seqUpdExt.deliverUserUpdate(userId = cmd.inviteeUserId, update)
                }

                // push updated members difference to all group members except inviteeUserId
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
                  update = inviteeUpdateNew,
                  pushRules = seqUpdExt.pushRules(isFat = false, Some(PushTexts.invited(newState.groupType))),
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
                _ ← dialogExt.bump(cmd.inviteeUserId, apiGroupPeer.asModel)
              } yield SeqStateDate(seq, state, dateMillis)

            val result: Future[SeqStateDate] = for {
              ///////////////////////////
              // Groups V1 API updates //
              ///////////////////////////

              // push "Invited" to invitee
              _ ← seqUpdExt.deliverUserUpdate(
                userId = cmd.inviteeUserId,
                inviteeUpdateObsolete,
                pushRules = seqUpdExt.pushRules(isFat = true, Some(PushTexts.invited(newState.groupType))),
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

            result pipeTo replyTo
          }
        }
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
      val replyTo = sender()

      val isBlockedFu = checkIsBlocked(cmd.joiningUserId, state.ownerUserId)

      onSuccess(isBlockedFu) { isBlocked ⇒
        if (isBlocked) {
          replyTo ! Status.Failure(GroupErrors.UserIsBanned)
        } else {
          // user was invited in group by other group user
          val wasInvited = state.isInvited(cmd.joiningUserId)

          // trying to figure out who invited joining user.
          // Descending priority:
          // • inviter defined in `Join` command (when invited via token)
          // • inviter from members list (when invited by other user)
          // • group creator (safe fallback)
          val optMember = state.members.get(cmd.joiningUserId)
          val inviterUserId = cmd.invitingUserId
            .orElse(optMember.map(_.inviterUserId))
            .getOrElse(state.ownerUserId)

          persist(UserJoined(Instant.now, cmd.joiningUserId, inviterUserId)) { evt ⇒
            val newState = commit(evt)

            val date = evt.ts
            val dateMillis = date.toEpochMilli
            val showJoinMessage = newState.adminSettings.showJoinLeaveMessages
            val memberIds = newState.memberIds
            val apiMembers = newState.members.values.map(_.asStruct).toVector
            val randomId = ACLUtils.randomLong()

            // If user was never invited to group - he don't have group on devices,
            // that means we need to push all group-info related updates
            //
            // If user was invited to group by other member - we don't need to push group updates,
            // cause they were pushed already on invite step
            // TODO: unify isHistoryShared usage
            val joiningUserUpdatesNew: Vector[Update] = {
              if (wasInvited) {
                Vector.empty[Update]
              } else {
                val optDrop = if (newState.isHistoryShared) Some(UpdateChatDropCache(apiGroupPeer)) else None
                optDrop ++: refreshGroupUpdates(newState, cmd.joiningUserId)
              }
            }

            // For groups with not async members we should push:
            // • Diff for members;
            // • Diff for joining user if he was previously invited;
            // • Members for joining user if he wasn't previously invited.
            //
            // For groups with async members we should push:
            // • UpdateGroupMembersCountChanged for both joining user and members
            val (joiningUpdateNew, membersUpdateNew): (Update, Update) =
              if (newState.isAsyncMembers) {
                val u = UpdateGroupMembersCountChanged(groupId, newState.membersCount)
                (u, u)
              } else {
                val joiningMember = apiMembers.find(_.userId == cmd.joiningUserId)
                val diff = UpdateGroupMemberDiff(
                  groupId,
                  addedMembers = joiningMember.toVector,
                  membersCount = newState.membersCount,
                  removedUsers = Vector.empty
                )

                if (wasInvited) {
                  (diff, diff)
                } else {
                  (
                    UpdateGroupMembersUpdated(groupId, apiMembers),
                    diff
                  )
                }
              }

            // TODO: not sure how it should be in old API
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
            ): @silent)

            def joinGROUPUpdates: Future[SeqStateDate] =
              for {
                // push all group updates to joiningUserId
                _ ← FutureExt.ftraverse(joiningUserUpdatesNew) { update ⇒
                  seqUpdExt.deliverUserUpdate(userId = cmd.joiningUserId, update)
                }

                // push updated members list/count/difference to joining user,
                // make it `FatSeqUpdate` if this user invited to group for first time.
                // TODO???: isFat = !wasInvited - is it correct?
                SeqState(seq, state) ← seqUpdExt.deliverClientUpdate(
                  userId = cmd.joiningUserId,
                  authId = cmd.joiningUserAuthId,
                  update = joiningUpdateNew,
                  pushRules = seqUpdExt.pushRules(isFat = !wasInvited, None), //!wasInvited means that user came for first time here
                  deliveryId = s"join_${groupId}_${randomId}"

                )

                // push updated members list/count to all group members except joiningUserId
                _ ← seqUpdExt.broadcastPeopleUpdate(
                  memberIds - cmd.joiningUserId,
                  membersUpdateNew,
                  deliveryId = s"userjoined_${groupId}_${randomId}"
                )

                date ← if (showJoinMessage) {
                  dialogExt.sendServerMessage(
                    apiGroupPeer,
                    senderUserId = cmd.joiningUserId,
                    senderAuthId = cmd.joiningUserAuthId,
                    randomId = randomId,
                    serviceMessage // no delivery tag. This updated handled this way in Groups V1
                  ) map (_.date)
                } else {
                  // write service message only for joining user
                  // and push join message
                  for {
                    _ ← dialogExt.writeMessageSelf(
                      userId = cmd.joiningUserId,
                      peer = apiGroupPeer,
                      senderUserId = cmd.joiningUserId,
                      dateMillis = dateMillis,
                      randomId = randomId,
                      serviceMessage
                    )
                    _ ← seqUpdExt.deliverUserUpdate(
                      userId = cmd.joiningUserId,
                      update = serviceMessageUpdate(
                        cmd.joiningUserId,
                        dateMillis,
                        randomId,
                        serviceMessage
                      ),
                      deliveryTag = Some(Optimization.GroupV2)
                    )
                  } yield dateMillis
                }
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
                  update = joiningUpdateNew,
                  deliveryId = s"join_${groupId}_${randomId}"
                )

                // push updated members count to all group members except joining user
                _ ← seqUpdExt.broadcastPeopleUpdate(
                  memberIds - cmd.joiningUserId,
                  membersUpdateNew,
                  deliveryId = s"userjoined_${groupId}_${randomId}"
                )

                // push join message only to joining user
                _ ← seqUpdExt.deliverUserUpdate(
                  userId = cmd.joiningUserId,
                  update = serviceMessageUpdate(
                    cmd.joiningUserId,
                    dateMillis,
                    randomId,
                    serviceMessage
                  ),
                  deliveryTag = Some(Optimization.GroupV2)
                )
                _ ← dialogExt.bump(cmd.joiningUserId, apiGroupPeer.asModel)
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

            result pipeTo replyTo
          }
        }
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
    } else if (!state.permissions.canLeave(cmd.userId)) {
      sender() ! Status.Failure(CantLeaveGroup)
    } else {
      val leftEvent = UserLeft(Instant.now, cmd.userId)
      persist(leftEvent) { evt ⇒
        // no commit here. it will be after service message sent

        val dateMillis = evt.ts.toEpochMilli
        val showLeaveMessage = state.adminSettings.showJoinLeaveMessages

        val updateObsolete = UpdateGroupUserLeaveObsolete(groupId, cmd.userId, dateMillis, cmd.randomId)

        val updatePermissions = permissionsUpdates(cmd.userId, currState = state.updated(leftEvent))

        val membersUpdateNew =
          if (state.isAsyncMembers) {
            UpdateGroupMembersCountChanged(
              groupId,
              membersCount = state.membersCount - 1
            )
          } else {
            UpdateGroupMemberDiff(
              groupId,
              removedUsers = Vector(cmd.userId),
              addedMembers = Vector.empty,
              membersCount = state.membersCount - 1
            )
          }

        val serviceMessage = GroupServiceMessages.userLeft

        //TODO: remove deprecated. GroupInviteTokenRepo don't have replacement yet.
        db.run(
          for {
            _ ← GroupUserRepo.delete(groupId, cmd.userId): @silent
            _ ← GroupInviteTokenRepo.revoke(groupId, cmd.userId): @silent
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
            date ← if (showLeaveMessage) {
              dialogExt.sendServerMessage(
                apiGroupPeer,
                senderUserId = cmd.userId,
                senderAuthId = cmd.authId,
                randomId = cmd.randomId,
                message = serviceMessage,
                deliveryTag = Some(Optimization.GroupV2)
              ) map (_.date)
            } else {
              FastFuture.successful(dateMillis)
            }

            // push left user that he is no longer a member
            SeqState(seq, state) ← seqUpdExt.deliverClientUpdate(
              userId = cmd.userId,
              authId = cmd.authId,
              update = UpdateGroupMemberChanged(groupId, isMember = false)
            )

            // push left user updates
            // • with empty group members
            // • that he can't view and invite members
            leftUpdates = updatePermissions :+ UpdateGroupMembersUpdated(groupId, members = Vector.empty)
            _ ← FutureExt.ftraverse(leftUpdates) { update ⇒
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
            leftUpdates = updatePermissions :+ UpdateGroupMembersCountChanged(groupId, membersCount = 0)
            _ ← FutureExt.ftraverse(leftUpdates) { update ⇒
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
    val canKick =
      state.permissions.canKickAnyone(cmd.kickerUserId) ||
        (
          state.permissions.canKickInvited(cmd.kickerUserId) &&
          state.members.get(cmd.kickedUserId).exists(_.inviterUserId == cmd.kickerUserId) // user we kick invited by kicker
        )
    if (!canKick) {
      sender() ! noPermission
    } else if (state.nonMember(cmd.kickedUserId)) {
      sender() ! notMember
    } else {
      persist(UserKicked(Instant.now, cmd.kickedUserId, cmd.kickerUserId)) { evt ⇒
        val newState = commit(evt)

        val dateMillis = evt.ts.toEpochMilli

        val updateObsolete = UpdateGroupUserKickObsolete(groupId, cmd.kickedUserId, cmd.kickerUserId, dateMillis, cmd.randomId)

        val updatePermissions = permissionsUpdates(cmd.kickedUserId, newState)

        val membersUpdateNew: Update =
          if (newState.isAsyncMembers) {
            UpdateGroupMembersCountChanged(
              groupId,
              membersCount = newState.membersCount
            )
          } else {
            UpdateGroupMemberDiff(
              groupId,
              removedUsers = Vector(cmd.kickedUserId),
              addedMembers = Vector.empty,
              membersCount = newState.membersCount
            )
          }

        val serviceMessage = GroupServiceMessages.userKicked(cmd.kickedUserId)

        //TODO: remove deprecated. GroupInviteTokenRepo don't have replacement yet.
        db.run(
          for {
            _ ← GroupUserRepo.delete(groupId, cmd.kickedUserId): @silent
            _ ← GroupInviteTokenRepo.revoke(groupId, cmd.kickedUserId): @silent
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
            kickedUserUpdates = updatePermissions ++ Vector(
              UpdateGroupMembersUpdated(groupId, members = Vector.empty),
              UpdateGroupMemberChanged(groupId, isMember = false)
            )
            _ ← FutureExt.ftraverse(kickedUserUpdates) { update ⇒
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
            kickedUserUpdates = updatePermissions :+ UpdateGroupMemberChanged(groupId, isMember = false)
            _ ← FutureExt.ftraverse(kickedUserUpdates) { update ⇒
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

  // Updates that will be sent to user, when he enters group.
  // Helps clients that have this group to refresh it's data.
  private def refreshGroupUpdates(newState: GroupState, userId: Int): Vector[Update] = Vector(
    UpdateGroupMemberChanged(groupId, isMember = true),
    UpdateGroupAboutChanged(groupId, newState.about),
    UpdateGroupAvatarChanged(groupId, newState.avatar),
    UpdateGroupTopicChanged(groupId, newState.topic),
    UpdateGroupTitleChanged(groupId, newState.title),
    UpdateGroupOwnerChanged(groupId, newState.ownerUserId)
  //    UpdateGroupExtChanged(groupId, newState.extension) //TODO: figure out and fix
  //          if(bigGroup) UpdateGroupMembersCountChanged(groupId, newState.extension)
  ) ++ permissionsUpdates(userId, newState)

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

}
