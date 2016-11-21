package im.actor.server.group

import java.time.Instant

import akka.actor.Status
import akka.http.scaladsl.util.FastFuture
import akka.pattern.pipe
import com.github.ghik.silencer.silent
import im.actor.api.rpc.Update
import im.actor.api.rpc.groups._
import im.actor.api.rpc.users.ApiSex
import im.actor.concurrent.FutureExt
import im.actor.server.acl.ACLUtils
import im.actor.server.dialog.UserAcl
import im.actor.server.group.GroupCommands._
import im.actor.server.group.GroupErrors._
import im.actor.server.group.GroupEvents._
import im.actor.server.model.Group
import im.actor.server.persist.{ GroupBotRepo, GroupRepo, GroupUserRepo }
import im.actor.server.sequence.Optimization
import im.actor.util.ThreadLocalSecureRandom
import im.actor.util.misc.IdUtils

import scala.concurrent.Future

private[group] trait GroupCommandHandlers
  extends MemberCommandHandlers
  with InfoCommandHandlers
  with AdminCommandHandlers
  with UserAcl {
  this: GroupProcessor ⇒

  protected val notMember = Status.Failure(NotAMember)
  protected val notAdmin = Status.Failure(NotAdmin)
  protected val noPermission = Status.Failure(NoPermission)

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
      val groupType = GroupType.fromValue(cmd.typ)

      persist(Created(
        ts = createdAt,
        groupId,
        typ = Some(groupType),
        creatorUserId = cmd.creatorUserId,
        accessHash = accessHash,
        title = cmd.title,
        userIds = Seq(cmd.creatorUserId), // only creator user becomes group member. all other users are invited via Invite message
        isHidden = Some(false),
        isHistoryShared = Some(groupType.isChannel),
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
                isPublic = false,
                createdAt = evt.ts,
                about = None,
                topic = None
              ),
              cmd.randomId,
              isHidden = false
            ): @silent
            _ ← GroupUserRepo.create(groupId, cmd.creatorUserId, cmd.creatorUserId, createdAt, None, isAdmin = true): @silent
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
        db.run(GroupBotRepo.create(groupId, botUserId, botToken): @silent)

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

  protected def permissionsUpdates(userId: Int, currState: GroupState): Vector[Update] =
    Vector(
      UpdateGroupPermissionsChanged(groupId, currState.permissions.groupFor(userId)),
      UpdateGroupFullPermissionsChanged(groupId, currState.permissions.fullFor(userId))
    )

  protected def isValidTitle(title: String) = title.nonEmpty && title.length < 255

  protected def updateCanCall(currState: GroupState): Unit = {
    log.debug(s"Group {} can call updated", groupId)
    currState.memberIds foreach { userId ⇒
      permissionsUpdates(userId, currState) foreach { update ⇒
        seqUpdExt.deliverUserUpdate(userId, update)
      }
    }
  }

  protected def makeMembersAsync(): Unit = {
    persist(MembersBecameAsync(Instant.now)) { evt ⇒
      val newState = commit(evt)
      log.debug(s"Group {} became async members", groupId)

      seqUpdExt.broadcastPeopleUpdate(
        userIds = newState.memberIds,
        update = UpdateGroupMembersBecameAsync(groupId)
      )
    }
  }
}
