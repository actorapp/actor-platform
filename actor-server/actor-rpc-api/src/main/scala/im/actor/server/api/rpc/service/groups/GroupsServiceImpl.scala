package im.actor.server.api.rpc.service.groups

import akka.actor.ActorSystem
import im.actor.api.rpc.PeerHelpers._
import im.actor.api.rpc._
import im.actor.api.rpc.files.ApiFileLocation
import im.actor.api.rpc.groups._
import im.actor.api.rpc.misc.ResponseSeqDate
import im.actor.api.rpc.peers.{ ApiGroupOutPeer, ApiUserOutPeer }
import im.actor.api.rpc.sequence.ApiUpdateOptimization
import im.actor.concurrent.FutureExt
import im.actor.server.acl.ACLUtils.accessToken
import im.actor.server.db.DbExtension
import im.actor.server.file.{ FileErrors, FileStorageAdapter, FileStorageExtension, ImageUtils }
import im.actor.server.group._
import im.actor.server.model.{ Group, GroupInviteToken }
import im.actor.server.persist.{ GroupInviteTokenRepo, GroupUserRepo }
import im.actor.server.presences.GroupPresenceExtension
import im.actor.server.sequence.{ SeqState, SeqStateDate }
import im.actor.server.user.UserExtension
import im.actor.util.misc.IdUtils
import im.actor.util.ThreadLocalSecureRandom
import slick.dbio.DBIO
import slick.driver.PostgresDriver.api._

import scala.concurrent.{ ExecutionContext, Future }

final class GroupsServiceImpl(groupInviteConfig: GroupInviteConfig)(implicit actorSystem: ActorSystem) extends GroupsService {

  import FileHelpers._
  import GroupCommands._
  import IdUtils._
  import ImageUtils._

  override implicit val ec: ExecutionContext = actorSystem.dispatcher

  private val db: Database = DbExtension(actorSystem).db
  private val groupExt = GroupExtension(actorSystem)
  private val userExt = UserExtension(actorSystem)
  private implicit val fsAdapter: FileStorageAdapter = FileStorageExtension(actorSystem).fsAdapter
  private val groupPresenceExt = GroupPresenceExtension(actorSystem)

  /**
   * Loading Full Groups
   *
   * @param groups Groups to load
   */
  override protected def doHandleLoadFullGroups(
    groups:     IndexedSeq[ApiGroupOutPeer],
    clientData: ClientData
  ): Future[HandlerResult[ResponseLoadFullGroups]] = {
    authorized(clientData) { implicit client ⇒
      withGroupOutPeersF(groups) {
        for {
          fullGroups ← FutureExt.ftraverse(groups)(group ⇒ groupExt.getApiFullStruct(group.groupId, client.userId))
        } yield Ok(ResponseLoadFullGroups(fullGroups.toVector))
      }
    }
  }

  /**
   * Make user admin
   *
   * @param groupPeer Group's peer
   * @param userPeer  User's peer
   */
  override protected def doHandleMakeUserAdmin(groupPeer: ApiGroupOutPeer, userPeer: ApiUserOutPeer, clientData: ClientData): Future[HandlerResult[ResponseSeqDate]] = {
    authorized(clientData) { implicit client ⇒
      withGroupOutPeerF(groupPeer) { _ ⇒
        for {
          (_, SeqStateDate(seq, state, date)) ← groupExt.makeUserAdmin(groupPeer.groupId, client.userId, userPeer.userId)
        } yield Ok(ResponseSeqDate(seq, state.toByteArray, date))
      }
    }
  }

  /**
   * Loading group members
   *
   * @param group Group peer
   * @param limit Limit members
   * @param next  Load more reference
   */
  override protected def doHandleLoadMembers(
    group:      ApiGroupOutPeer,
    limit:      Int,
    next:       Option[Array[Byte]],
    clientData: ClientData
  ): Future[HandlerResult[ResponseLoadMembers]] = {
    authorized(clientData) { implicit client ⇒
      withGroupOutPeerF(group) { _ ⇒
        for {
          (userIds, nextOffset) ← groupExt.loadMembers(group.groupId, client.userId, limit, next)
          members ← FutureExt.ftraverse(userIds)(userExt.getApiStruct(_, client.userId, client.authId))
        } yield Ok(ResponseLoadMembers(members.toVector map (u ⇒ ApiUserOutPeer(u.id, u.accessHash)), nextOffset))
      }
    }
  }

  /**
   * Transfer ownership of group
   *
   * @param groupPeer Group's peer
   * @param newOwner  New group's owner
   */
  override protected def doHandleTransferOwnership(groupPeer: ApiGroupOutPeer, newOwner: Int, clientData: ClientData): Future[HandlerResult[ResponseSeqDate]] =
    authorized(clientData) { implicit client ⇒
      withGroupOutPeerF(groupPeer) { _ ⇒
        for {
          SeqState(seq, state) ← groupExt.transferOwnership(groupPeer.groupId, client.userId, newOwner)
        } yield Ok(ResponseSeqDate(seq, state.toByteArray, 0))
      }
    }

  override def doHandleEditGroupAvatar(
    groupOutPeer:  ApiGroupOutPeer,
    randomId:      Long,
    fileLocation:  ApiFileLocation,
    optimizations: IndexedSeq[ApiUpdateOptimization.Value],
    clientData:    ClientData
  ): Future[HandlerResult[ResponseEditGroupAvatar]] =
    authorized(clientData) { implicit client ⇒
      val action = withOwnGroupMember(groupOutPeer, client.userId) { fullGroup ⇒
        withFileLocation(fileLocation, AvatarSizeLimit) {
          scaleAvatar(fileLocation.fileId, ThreadLocalSecureRandom.current()) flatMap {
            case Right(avatar) ⇒
              for {
                UpdateAvatarAck(avatar, SeqStateDate(seq, state, date)) ← DBIO.from(groupExt.updateAvatar(fullGroup.id, client.userId, Some(avatar), randomId))
              } yield Ok(ResponseEditGroupAvatar(
                avatar.get,
                seq,
                state.toByteArray,
                date
              ))
            case Left(e) ⇒
              throw FileErrors.LocationInvalid
          }
        }
      }
      db.run(action)
    }

  override def doHandleRemoveGroupAvatar(
    groupOutPeer:  ApiGroupOutPeer,
    randomId:      Long,
    optimizations: IndexedSeq[ApiUpdateOptimization.Value],
    clientData:    ClientData
  ): Future[HandlerResult[ResponseSeqDate]] =
    authorized(clientData) { implicit client ⇒
      val action = withOwnGroupMember(groupOutPeer, client.userId) { fullGroup ⇒
        for {
          UpdateAvatarAck(avatar, SeqStateDate(seq, state, date)) ← DBIO.from(groupExt.updateAvatar(fullGroup.id, client.userId, None, randomId))
        } yield Ok(ResponseSeqDate(
          seq,
          state.toByteArray,
          date
        ))
      }
      db.run(action)
    }

  override def doHandleKickUser(
    groupOutPeer:  ApiGroupOutPeer,
    randomId:      Long,
    userOutPeer:   ApiUserOutPeer,
    optimizations: IndexedSeq[ApiUpdateOptimization.Value],
    clientData:    ClientData
  ): Future[HandlerResult[ResponseSeqDate]] =
    authorized(clientData) { implicit client ⇒
      val action = withKickableGroupMember(groupOutPeer, userOutPeer) { fullGroup ⇒ //maybe move to group peer manager
        for {
          //todo: get rid of DBIO.from
          SeqStateDate(seq, state, date) ← DBIO.from(groupExt.kickUser(fullGroup.id, userOutPeer.userId, randomId))
        } yield {
          groupPresenceExt.notifyGroupUserRemoved(fullGroup.id, userOutPeer.userId)
          Ok(ResponseSeqDate(seq, state.toByteArray, date))
        }
      }
      db.run(action)
    }

  override def doHandleLeaveGroup(
    groupOutPeer:  ApiGroupOutPeer,
    randomId:      Long,
    optimizations: IndexedSeq[ApiUpdateOptimization.Value],
    clientData:    ClientData
  ): Future[HandlerResult[ResponseSeqDate]] =
    authorized(clientData) { implicit client ⇒
      val action = withOwnGroupMember(groupOutPeer, client.userId) { fullGroup ⇒
        for {
          SeqStateDate(seq, state, date) ← DBIO.from(groupExt.leaveGroup(fullGroup.id, randomId))
        } yield {
          groupPresenceExt.notifyGroupUserRemoved(fullGroup.id, client.userId)
          Ok(ResponseSeqDate(seq, state.toByteArray, date))
        }
      }
      db.run(action)
    }

  override def doHandleCreateGroup(
    randomId:      Long,
    title:         String,
    users:         IndexedSeq[ApiUserOutPeer],
    groupType:     Option[ApiGroupType.Value],
    optimizations: IndexedSeq[ApiUpdateOptimization.Value],
    clientData:    ClientData
  ): Future[HandlerResult[ResponseCreateGroup]] = {
    authorized(clientData) { implicit client ⇒
      db.run(withUserOutPeers(users) {
        withValidGroupTitle(title) { validTitle ⇒
          DBIO.from {
            val groupId = nextIntId()
            val userIds = users.map(_.userId)
            val typ = groupType map {
              case ApiGroupType.GROUP   ⇒ GroupType.General
              case ApiGroupType.CHANNEL ⇒ GroupType.Channel
            } getOrElse GroupType.General

            for {
              res ← groupExt.create(
                groupId,
                client.userId,
                client.authSid,
                validTitle,
                randomId,
                userIds.toSet,
                typ
              )
              group ← groupExt.getApiStruct(groupId, client.userId)
              users ← Future.sequence(GroupUtils.getUserIds(group) map (userExt.getApiStruct(_, client.userId, client.authId))) map (_.toVector)
            } yield {
              val stripEntities = optimizations.contains(ApiUpdateOptimization.STRIP_ENTITIES)

              Ok(ResponseCreateGroup(
                seq = res.seqstate.seq,
                state = res.seqstate.state.toByteArray,
                group = group,
                users = if (stripEntities) Vector.empty else users,
                userPeers = if (stripEntities) users map (u ⇒ ApiUserOutPeer(u.id, u.accessHash)) else Vector.empty
              ))
            }
          }
        }
      })
    }
  }

  override def doHandleInviteUser(
    groupOutPeer:  ApiGroupOutPeer,
    randomId:      Long,
    userOutPeer:   ApiUserOutPeer,
    optimizations: IndexedSeq[ApiUpdateOptimization.Value],
    clientData:    ClientData
  ): Future[HandlerResult[ResponseSeqDate]] =
    authorized(clientData) { implicit client ⇒
      val action = withOwnGroupMember(groupOutPeer, client.userId) { fullGroup ⇒
        withUserOutPeer(userOutPeer) {
          for {
            res ← DBIO.from(groupExt.inviteToGroup(fullGroup.id, userOutPeer.userId, randomId))
          } yield {
            groupPresenceExt.notifyGroupUserAdded(fullGroup.id, userOutPeer.userId)
            Ok(ResponseSeqDate(res.seq, res.state.toByteArray, res.date))
          }
        }
      }
      db.run(action)
    }

  override def doHandleEditGroupTitle(
    groupOutPeer:  ApiGroupOutPeer,
    randomId:      Long,
    title:         String,
    optimizations: IndexedSeq[ApiUpdateOptimization.Value],
    clientData:    ClientData
  ): Future[HandlerResult[ResponseSeqDate]] =
    authorized(clientData) { implicit client ⇒
      val action = withOwnGroupMember(groupOutPeer, client.userId) { fullGroup ⇒
        for {
          SeqStateDate(seq, state, date) ← DBIO.from(groupExt.updateTitle(fullGroup.id, client.userId, title, randomId))
        } yield Ok(ResponseSeqDate(seq, state.toByteArray, date))
      }
      db.run(action)
    }

  override def doHandleGetGroupInviteUrl(groupPeer: ApiGroupOutPeer, clientData: ClientData): Future[HandlerResult[ResponseInviteUrl]] =
    authorized(clientData) { implicit client ⇒
      val action = withOwnGroupMember(groupPeer, client.userId) { fullGroup ⇒
        for {
          token ← GroupInviteTokenRepo.find(fullGroup.id, client.userId).headOption.flatMap {
            case Some(invToken) ⇒ DBIO.successful(invToken.token)
            case None ⇒
              val token = accessToken(ThreadLocalSecureRandom.current())
              val inviteToken = GroupInviteToken(fullGroup.id, client.userId, token)
              for (_ ← GroupInviteTokenRepo.create(inviteToken)) yield token
          }
        } yield Ok(ResponseInviteUrl(genInviteUrl(groupInviteConfig.baseUrl, token)))
      }
      db.run(action)
    }

  override def doHandleJoinGroup(
    url:           String,
    optimizations: IndexedSeq[ApiUpdateOptimization.Value],
    clientData:    ClientData
  ): Future[HandlerResult[ResponseJoinGroup]] =
    authorized(clientData) { implicit client ⇒
      val action = withValidInviteToken(groupInviteConfig.baseUrl, url) { (fullGroup, token) ⇒
        val group = Group.fromFull(fullGroup)

        val join = groupExt.joinGroup(
          groupId = group.id,
          joiningUserId = client.userId,
          joiningUserAuthSid = client.authSid,
          invitingUserId = token.creatorId
        )
        for {
          (seqstatedate, userIds, randomId) ← DBIO.from(join)
          userStructs ← DBIO.from(Future.sequence(userIds.map(userExt.getApiStruct(_, client.userId, client.authId))))
          groupStruct ← DBIO.from(groupExt.getApiStruct(group.id, client.userId))
        } yield {
          val stripEntities = optimizations.contains(ApiUpdateOptimization.STRIP_ENTITIES)

          Ok(ResponseJoinGroup(
            groupStruct,
            seqstatedate.seq,
            seqstatedate.state.toByteArray,
            seqstatedate.date,
            if (stripEntities) Vector.empty else userStructs,
            randomId,
            if (stripEntities) userStructs map (u ⇒ ApiUserOutPeer(u.id, u.accessHash)) else Vector.empty
          ))
        }
      }
      db.run(action)
    }

  override def doHandleRevokeInviteUrl(groupPeer: ApiGroupOutPeer, clientData: ClientData): Future[HandlerResult[ResponseInviteUrl]] =
    authorized(clientData) { implicit client ⇒
      val action = withOwnGroupMember(groupPeer, client.userId) { fullGroup ⇒
        val token = accessToken(ThreadLocalSecureRandom.current())
        val inviteToken = GroupInviteToken(fullGroup.id, client.userId, token)

        for {
          _ ← GroupInviteTokenRepo.revoke(fullGroup.id, client.userId)
          _ ← GroupInviteTokenRepo.create(inviteToken)
        } yield Ok(ResponseInviteUrl(genInviteUrl(groupInviteConfig.baseUrl, token)))
      }
      db.run(action)
    }

  /**
   * all members of group can edit group topic
   */
  override def doHandleEditGroupTopic(
    groupPeer:     ApiGroupOutPeer,
    randomId:      Long,
    topic:         Option[String],
    optimizations: IndexedSeq[ApiUpdateOptimization.Value],
    clientData:    ClientData
  ): Future[HandlerResult[ResponseSeqDate]] = {
    authorized(clientData) { implicit client ⇒
      for {
        SeqStateDate(seq, state, date) ← groupExt.updateTopic(groupPeer.groupId, client.userId, topic, randomId)
      } yield Ok(ResponseSeqDate(seq, state.toByteArray, date))
    }
  }

  /**
   * only admin can change group's about
   */
  override def doHandleEditGroupAbout(
    groupPeer:     ApiGroupOutPeer,
    randomId:      Long,
    about:         Option[String],
    optimizations: IndexedSeq[ApiUpdateOptimization.Value],
    clientData:    ClientData
  ): Future[HandlerResult[ResponseSeqDate]] = {
    authorized(clientData) { implicit client ⇒
      for {
        SeqStateDate(seq, state, date) ← groupExt.updateAbout(groupPeer.groupId, client.userId, about, randomId)
      } yield Ok(ResponseSeqDate(seq, state.toByteArray, date))
    }
  }

  override def doHandleCreateGroupObsolete(randomId: Long, title: String, users: IndexedSeq[ApiUserOutPeer], clientData: ClientData): Future[HandlerResult[ResponseCreateGroupObsolete]] =
    authorized(clientData) { implicit client ⇒
      val action = withUserOutPeers(users) {
        withValidGroupTitle(title) { validTitle ⇒
          val groupId = nextIntId(ThreadLocalSecureRandom.current())
          val userIds = users.map(_.userId).toSet
          val f = for (res ← groupExt.create(groupId, title, randomId, userIds)) yield {
            Ok(ResponseCreateGroupObsolete(
              groupPeer = ApiGroupOutPeer(groupId, res.accessHash),
              seq = res.seqstate.seq,
              state = res.seqstate.state.toByteArray,
              users = (userIds + client.userId).toVector,
              date = res.date
            ))
          }
          DBIO.from(f)
        }
      }
      db.run(action)
    }

  override def doHandleEnterGroupObsolete(peer: ApiGroupOutPeer, clientData: ClientData): Future[HandlerResult[ResponseEnterGroupObsolete]] =
    authorized(clientData) { implicit client ⇒
      val action = withPublicGroup(peer) { fullGroup ⇒
        GroupUserRepo.find(fullGroup.id, client.userId) flatMap {
          case Some(_) ⇒ DBIO.successful(Error(GroupRpcErrors.YouAlreadyAMember))
          case None ⇒
            val group = Group.fromFull(fullGroup)
            for {
              (seqstatedate, userIds, randomId) ← DBIO.from(groupExt.joinGroup(group.id, client.userId, client.authSid, fullGroup.creatorUserId))
              userStructs ← DBIO.from(Future.sequence(userIds.map(userExt.getApiStruct(_, client.userId, client.authId))))
              groupStruct ← DBIO.from(groupExt.getApiStruct(group.id, client.userId))
            } yield Ok(ResponseEnterGroupObsolete(groupStruct, userStructs, randomId, seqstatedate.seq, seqstatedate.state.toByteArray, seqstatedate.date))
        }
      }
      db.run(action)
    }

  /**
   * only admin can give another group member admin rights
   * if this user id already admin - `GroupErrors.UserAlreadyAdmin` will be returned
   * it could be many admins in one group
   */
  override def doHandleMakeUserAdminObsolete(
    groupPeer:  ApiGroupOutPeer,
    userPeer:   ApiUserOutPeer,
    clientData: ClientData
  ): Future[HandlerResult[ResponseMakeUserAdminObsolete]] = {
    authorized(clientData) { implicit client ⇒
      for {
        (members, SeqStateDate(seq, state, _)) ← groupExt.makeUserAdmin(groupPeer.groupId, client.userId, userPeer.userId)
      } yield Ok(ResponseMakeUserAdminObsolete(members, seq, state.toByteArray))
    }
  }

  override def onFailure: PartialFunction[Throwable, RpcError] = {
    case GroupErrors.NotAMember         ⇒ CommonRpcErrors.forbidden("User is not a group member.")
    case GroupErrors.NotAdmin           ⇒ CommonRpcErrors.forbidden("Only admin can perform this action.")
    case GroupErrors.UserAlreadyAdmin   ⇒ GroupRpcErrors.UserAlreadyAdmin
    case GroupErrors.AboutTooLong       ⇒ GroupRpcErrors.AboutTooLong
    case GroupErrors.TopicTooLong       ⇒ GroupRpcErrors.TopicTooLong
    case GroupErrors.BlockedByUser      ⇒ GroupRpcErrors.BlockedByUser
    case FileErrors.LocationInvalid     ⇒ FileRpcErrors.LocationInvalid
    case GroupErrors.UserAlreadyInvited ⇒ GroupRpcErrors.YouAlreadyAMember
  }

}
