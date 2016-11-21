package im.actor.server.api.rpc.service.groups

import java.time.Instant

import akka.actor.ActorSystem
import akka.http.scaladsl.util.FastFuture
import cats.data.Xor
import com.github.ghik.silencer.silent
import im.actor.api.rpc.PeerHelpers._
import im.actor.api.rpc._
import im.actor.api.rpc.files.ApiFileLocation
import im.actor.api.rpc.groups._
import im.actor.api.rpc.misc.{ ResponseSeq, ResponseSeqDate, ResponseVoid }
import im.actor.api.rpc.peers.{ ApiGroupOutPeer, ApiUserOutPeer }
import im.actor.api.rpc.sequence.ApiUpdateOptimization
import im.actor.api.rpc.users.ApiUser
import im.actor.concurrent.FutureExt
import im.actor.server.acl.ACLUtils
import im.actor.server.db.DbExtension
import im.actor.server.dialog.DialogExtension
import im.actor.server.file.{ FileErrors, ImageUtils }
import im.actor.server.group._
import im.actor.server.model.GroupInviteToken
import im.actor.server.names.GlobalNamesStorageKeyValueStorage
import im.actor.server.persist.{ GroupInviteTokenRepo, GroupUserRepo }
import im.actor.server.presences.GroupPresenceExtension
import im.actor.server.sequence.{ SeqState, SeqStateDate, SeqUpdatesExtension }
import im.actor.server.user.UserExtension
import im.actor.util.ThreadLocalSecureRandom
import im.actor.util.misc.{ IdUtils, StringUtils }
import slick.dbio.DBIO
import slick.driver.PostgresDriver.api._

import scala.concurrent.{ ExecutionContext, Future }

final class GroupsServiceImpl(groupInviteConfig: GroupInviteConfig)(implicit actorSystem: ActorSystem) extends GroupsService {

  import EntitiesHelpers._
  import FileHelpers._
  import FutureResultRpc._
  import GroupCommands._
  import IdUtils._
  import ImageUtils._

  case object NoSeqStateDate extends RuntimeException("No SeqStateDate in response from group found")

  override implicit val ec: ExecutionContext = actorSystem.dispatcher

  private val db: Database = DbExtension(actorSystem).db
  private val groupExt = GroupExtension(actorSystem)
  private val seqUpdExt = SeqUpdatesExtension(actorSystem)
  private val userExt = UserExtension(actorSystem)
  private val groupPresenceExt = GroupPresenceExtension(actorSystem)
  private val globalNamesStorage = new GlobalNamesStorageKeyValueStorage
  private val dialogExt = DialogExtension(actorSystem)

  /**
   * Loading Full Groups
   *
   * @param groups Groups to load
   */
  override protected def doHandleLoadFullGroups(
    groups:     IndexedSeq[ApiGroupOutPeer],
    clientData: ClientData
  ): Future[HandlerResult[ResponseLoadFullGroups]] =
    authorized(clientData) { implicit client ⇒
      withGroupOutPeers(groups) {
        for {
          fullGroups ← FutureExt.ftraverse(groups)(group ⇒ groupExt.getApiFullStruct(group.groupId, client.userId))
        } yield Ok(ResponseLoadFullGroups(fullGroups.toVector))
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
      withGroupOutPeer(groupPeer) {
        withUserOutPeer(userPeer) {
          (for {
            _ ← fromFutureBoolean(GroupRpcErrors.CantGrantToBot)(userExt.getUser(userPeer.userId) map (!_.isBot))
            resp ← fromFuture(groupExt.makeUserAdmin(groupPeer.groupId, client.userId, client.authId, userPeer.userId))
            (_, SeqStateDate(seq, state, date)) = resp
          } yield ResponseSeqDate(seq, state.toByteArray, date)).value
        }
      }
    }
  }

  override def doHandleDismissUserAdmin(groupPeer: ApiGroupOutPeer, userPeer: ApiUserOutPeer, clientData: ClientData): Future[HandlerResult[ResponseSeq]] =
    authorized(clientData) { implicit client ⇒
      withGroupOutPeer(groupPeer) {
        withUserOutPeer(userPeer) {
          (for {
            _ ← fromFutureBoolean(GroupRpcErrors.CantGrantToBot)(userExt.getUser(userPeer.userId) map (!_.isBot))
            seqState ← fromFuture(groupExt.dismissUserAdmin(groupPeer.groupId, client.userId, client.authId, userPeer.userId))
          } yield ResponseSeq(seqState.seq, seqState.state.toByteArray)).value
        }
      }
    }

  override def doHandleLoadAdminSettings(groupPeer: ApiGroupOutPeer, clientData: ClientData): Future[HandlerResult[ResponseLoadAdminSettings]] =
    authorized(clientData) { client ⇒
      withGroupOutPeer(groupPeer) {
        for {
          settings ← groupExt.loadAdminSettings(groupPeer.groupId, client.userId)
        } yield Ok(ResponseLoadAdminSettings(settings))
      }
    }

  override def doHandleSaveAdminSettings(groupPeer: ApiGroupOutPeer, settings: ApiAdminSettings, clientData: ClientData): Future[HandlerResult[ResponseVoid]] =
    authorized(clientData) { client ⇒
      withGroupOutPeer(groupPeer) {
        for {
          _ ← groupExt.updateAdminSettings(groupPeer.groupId, client.userId, settings)
        } yield Ok(ResponseVoid)
      }
    }

  /**
   * Loading group members
   *
   * @param groupPeer Group peer
   * @param limit Limit members
   * @param next  Load more reference
   */
  override protected def doHandleLoadMembers(
    groupPeer:  ApiGroupOutPeer,
    limit:      Int,
    next:       Option[Array[Byte]],
    clientData: ClientData
  ): Future[HandlerResult[ResponseLoadMembers]] = {
    authorized(clientData) { implicit client ⇒
      withGroupOutPeer(groupPeer) {
        for {
          (members, nextOffset) ← groupExt.loadMembers(groupPeer.groupId, client.userId, limit, next)
          membersAndPeers ← FutureExt.ftraverse(members) { member ⇒
            userExt.getAccessHash(member.userId, client.authId) map (hash ⇒ member → ApiUserOutPeer(member.userId, hash))
          }
          (members, peers) = membersAndPeers.unzip
        } yield Ok(ResponseLoadMembers(peers.toVector, nextOffset, members.toVector))
      }
    }
  }

  /**
   * Transfer ownership of group
   *
   * @param groupPeer Group's peer
   * @param newOwner  New group's owner
   */
  override protected def doHandleTransferOwnership(groupPeer: ApiGroupOutPeer, newOwner: ApiUserOutPeer, clientData: ClientData): Future[HandlerResult[ResponseSeqDate]] =
    authorized(clientData) { implicit client ⇒
      withGroupOutPeer(groupPeer) {
        withUserOutPeer(newOwner) {
          (for {
            _ ← fromFutureBoolean(GroupRpcErrors.CantGrantToBot)(userExt.getUser(newOwner.userId) map (!_.isBot))
            seqState ← fromFuture(groupExt.transferOwnership(groupPeer.groupId, client.userId, client.authId, newOwner.userId))
          } yield ResponseSeqDate(
            seq = seqState.seq,
            state = seqState.state.toByteArray,
            date = Instant.now.toEpochMilli
          )).value
        }
      }
    }

  override def doHandleEditGroupAvatar(
    groupPeer:     ApiGroupOutPeer,
    randomId:      Long,
    fileLocation:  ApiFileLocation,
    optimizations: IndexedSeq[ApiUpdateOptimization.Value],
    clientData:    ClientData
  ): Future[HandlerResult[ResponseEditGroupAvatar]] =
    authorized(clientData) { implicit client ⇒
      withGroupOutPeer(groupPeer) {
        addOptimizations(optimizations)
        val action = withFileLocation(fileLocation, AvatarSizeLimit) {
          scaleAvatar(fileLocation.fileId) flatMap {
            case Right(avatar) ⇒
              for {
                UpdateAvatarAck(avatar, seqStateDate) ← DBIO.from(groupExt.updateAvatar(groupPeer.groupId, client.userId, client.authId, Some(avatar), randomId))
                SeqStateDate(seq, state, date) = seqStateDate.getOrElse(throw NoSeqStateDate)
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
        db.run(action)
      }
    }

  override def doHandleRemoveGroupAvatar(
    groupPeer:     ApiGroupOutPeer,
    randomId:      Long,
    optimizations: IndexedSeq[ApiUpdateOptimization.Value],
    clientData:    ClientData
  ): Future[HandlerResult[ResponseSeqDate]] =
    authorized(clientData) { implicit client ⇒
      addOptimizations(optimizations)
      for {
        UpdateAvatarAck(avatar, seqStateDate) ← groupExt.updateAvatar(
          groupPeer.groupId,
          client.userId,
          client.authId,
          avatarOpt = None,
          randomId
        )
        SeqStateDate(seq, state, date) = seqStateDate.getOrElse(throw NoSeqStateDate)
      } yield Ok(ResponseSeqDate(
        seq,
        state.toByteArray,
        date
      ))
    }

  override def doHandleKickUser(
    groupPeer:     ApiGroupOutPeer,
    randomId:      Long,
    userOutPeer:   ApiUserOutPeer,
    optimizations: IndexedSeq[ApiUpdateOptimization.Value],
    clientData:    ClientData
  ): Future[HandlerResult[ResponseSeqDate]] =
    authorized(clientData) { implicit client ⇒
      addOptimizations(optimizations)
      withGroupOutPeer(groupPeer) {
        withUserOutPeer(userOutPeer) {
          for {
            SeqStateDate(seq, state, date) ← groupExt.kickUser(groupPeer.groupId, userOutPeer.userId, randomId)
          } yield {
            groupPresenceExt.notifyGroupUserRemoved(groupPeer.groupId, userOutPeer.userId)
            Ok(ResponseSeqDate(seq, state.toByteArray, date))
          }
        }
      }
    }

  override def doHandleLeaveGroup(
    groupPeer:     ApiGroupOutPeer,
    randomId:      Long,
    optimizations: IndexedSeq[ApiUpdateOptimization.Value],
    clientData:    ClientData
  ): Future[HandlerResult[ResponseSeqDate]] =
    authorized(clientData) { implicit client ⇒
      addOptimizations(optimizations)
      withGroupOutPeer(groupPeer) {
        for {
          SeqStateDate(seq, state, date) ← groupExt.leaveGroup(groupPeer.groupId, randomId)
        } yield {
          groupPresenceExt.notifyGroupUserRemoved(groupPeer.groupId, client.userId)
          Ok(ResponseSeqDate(seq, state.toByteArray, date))
        }
      }
    }

  override def doHandleLeaveAndDelete(groupPeer: ApiGroupOutPeer, clientData: ClientData): Future[HandlerResult[ResponseSeq]] =
    authorized(clientData) { implicit client ⇒
      withGroupOutPeer(groupPeer) {
        for {
          _ ← groupExt.leaveGroup(groupPeer.groupId, ACLUtils.randomLong())
          SeqState(seq, state) ← dialogExt.delete(client.userId, client.authId, groupPeer.asModel)
        } yield {
          groupPresenceExt.notifyGroupUserRemoved(groupPeer.groupId, client.userId)
          Ok(ResponseSeq(seq, state.toByteArray))
        }
      }
    }

  override def doHandleCreateGroup(
    randomId:      Long,
    title:         String,
    users:         IndexedSeq[ApiUserOutPeer],
    groupType:     Option[ApiGroupType.Value],
    optimizations: IndexedSeq[ApiUpdateOptimization.Value],
    clientData:    ClientData
  ): Future[HandlerResult[ResponseCreateGroup]] =
    authorized(clientData) { implicit client ⇒
      addOptimizations(optimizations)
      withUserOutPeers(users) {
        val stripEntities = optimizations.contains(ApiUpdateOptimization.STRIP_ENTITIES)

        val groupId = nextIntId()
        val typ = groupType map {
          case ApiGroupType.GROUP   ⇒ GroupType.General
          case ApiGroupType.CHANNEL ⇒ GroupType.Channel
        } getOrElse GroupType.General

        for {
          CreateAck(_, seqStateDate) ← groupExt.create(
            groupId,
            client.userId,
            client.authId,
            title,
            randomId,
            userIds = users.map(_.userId).toSet,
            typ
          )
          SeqStateDate(seq, state, date) = seqStateDate.getOrElse(throw NoSeqStateDate)
          group ← groupExt.getApiStruct(groupId, client.userId)
          memberIds = GroupUtils.getUserIds(group)
          (users, userPeers) ← usersOrPeers(memberIds.toVector, stripEntities)
        } yield Ok(ResponseCreateGroup(
          seq = seq,
          state = state.toByteArray,
          group = group,
          users = users,
          userPeers = userPeers,
          date = date
        ))

      }
    }

  override def doHandleInviteUser(
    groupPeer:     ApiGroupOutPeer,
    randomId:      Long,
    userOutPeer:   ApiUserOutPeer,
    optimizations: IndexedSeq[ApiUpdateOptimization.Value],
    clientData:    ClientData
  ): Future[HandlerResult[ResponseSeqDate]] =
    authorized(clientData) { implicit client ⇒
      addOptimizations(optimizations)
      withGroupOutPeer(groupPeer) {
        withUserOutPeer(userOutPeer) {
          for {
            SeqStateDate(seq, state, date) ← groupExt.inviteToGroup(groupPeer.groupId, userOutPeer.userId, randomId)
          } yield {
            groupPresenceExt.notifyGroupUserAdded(groupPeer.groupId, userOutPeer.userId)
            Ok(ResponseSeqDate(seq, state.toByteArray, date))
          }
        }
      }
    }

  override def doHandleEditGroupTitle(
    groupPeer:     ApiGroupOutPeer,
    randomId:      Long,
    title:         String,
    optimizations: IndexedSeq[ApiUpdateOptimization.Value],
    clientData:    ClientData
  ): Future[HandlerResult[ResponseSeqDate]] =
    authorized(clientData) { implicit client ⇒
      addOptimizations(optimizations)
      withGroupOutPeer(groupPeer) {
        for {
          SeqStateDate(seq, state, date) ← groupExt.updateTitle(groupPeer.groupId, client.userId, client.authId, title, randomId)
        } yield Ok(ResponseSeqDate(seq, state.toByteArray, date))
      }
    }

  override def doHandleGetGroupInviteUrl(groupPeer: ApiGroupOutPeer, clientData: ClientData): Future[HandlerResult[ResponseInviteUrl]] =
    authorized(clientData) { implicit client ⇒
      groupExt.getApiFullStruct(groupPeer.groupId, client.userId) flatMap { group ⇒
        val isMember = group.members.exists(_.userId == client.userId)
        if (!isMember) {
          FastFuture.successful(Error(GroupRpcErrors.NotAMember))
        } else {
          withGroupOutPeer(groupPeer) {
            for {
              inviteString ← group.shortName match {
                case Some(name) ⇒ FastFuture.successful(name)
                case None ⇒
                  db.run((GroupInviteTokenRepo.find(groupPeer.groupId, client.userId): @silent).headOption flatMap {
                    case Some(invToken) ⇒ DBIO.successful(invToken.token)
                    case None ⇒
                      val token = ACLUtils.accessToken()
                      val inviteToken = GroupInviteToken(groupPeer.groupId, client.userId, token)
                      for (_ ← GroupInviteTokenRepo.create(inviteToken): @silent) yield token
                  })
              }
            } yield Ok(ResponseInviteUrl(genInviteUrl(inviteString)))
          }
        }
      }
    }

  override def doHandleJoinGroup(
    joinStringOrUrl: String,
    optimizations:   IndexedSeq[ApiUpdateOptimization.Value],
    clientData:      ClientData
  ): Future[HandlerResult[ResponseJoinGroup]] =
    authorized(clientData) { implicit client ⇒
      addOptimizations(optimizations)
      val stripEntities = optimizations.contains(ApiUpdateOptimization.STRIP_ENTITIES)

      val action = for {
        joinSting ← fromOption(GroupRpcErrors.InvalidInviteUrl)(extractJoinString(joinStringOrUrl))
        joinInfo ← joinSting match {
          case Xor.Left(token) ⇒
            for {
              info ← fromFutureOption(GroupRpcErrors.InvalidInviteToken)(db.run(GroupInviteTokenRepo.findByToken(token): @silent))
            } yield info.groupId → Some(info.creatorId)
          case Xor.Right(groupName) ⇒
            for {
              groupId ← fromFutureOption(GroupRpcErrors.InvalidInviteGroup)(globalNamesStorage.getGroupId(groupName))
            } yield groupId → None
        }
        (groupId, optInviter) = joinInfo
        joinResp ← fromFuture(groupExt.joinGroup(
          groupId = groupId,
          joiningUserId = client.userId,
          joiningUserAuthId = client.authId,
          invitingUserId = optInviter
        ))
        ((SeqStateDate(seq, state, date), userIds, randomId)) = joinResp
        up ← fromFuture(usersOrPeers(userIds, stripEntities))
        (users, userPeers) = up
        groupStruct ← fromFuture(groupExt.getApiStruct(groupId, client.userId))
      } yield ResponseJoinGroup(
        groupStruct,
        seq,
        state.toByteArray,
        date,
        users,
        randomId,
        userPeers
      )

      action.value
    }

  override def doHandleJoinGroupByPeer(groupPeer: ApiGroupOutPeer, clientData: ClientData): Future[HandlerResult[ResponseSeq]] =
    authorized(clientData) { implicit client ⇒
      withGroupOutPeer(groupPeer) {
        val action = for {
          apiGroup ← fromFuture(groupExt.getApiStruct(groupPeer.groupId, client.userId))
          _ ← fromBoolean(GroupRpcErrors.CantJoinGroup)(canJoin(apiGroup.permissions))
          joinResp ← fromFuture(groupExt.joinGroup(
            groupId = groupPeer.groupId,
            joiningUserId = client.userId,
            joiningUserAuthId = client.authId,
            invitingUserId = None
          ))
          SeqStateDate(seq, state, _) = joinResp._1
        } yield ResponseSeq(seq, state.toByteArray)

        action.value
      }
    }

  private def canJoin(permissions: Option[Long]) =
    permissions exists (p ⇒ (p & (1 << 4)) != 0) // TODO: make wrapper around permissions

  override def doHandleRevokeInviteUrl(groupPeer: ApiGroupOutPeer, clientData: ClientData): Future[HandlerResult[ResponseInviteUrl]] =
    authorized(clientData) { implicit client ⇒
      withGroupOutPeer(groupPeer) {
        val token = ACLUtils.accessToken()
        db.run(
          for {
            _ ← GroupInviteTokenRepo.revoke(groupPeer.groupId, client.userId): @silent
            _ ← GroupInviteTokenRepo.create(
              GroupInviteToken(groupPeer.groupId, client.userId, token)
            ): @silent
          } yield Ok(ResponseInviteUrl(genInviteUrl(token)))
        )
      }
    }

  override def doHandleEditGroupTopic(
    groupPeer:     ApiGroupOutPeer,
    randomId:      Long,
    topic:         Option[String],
    optimizations: IndexedSeq[ApiUpdateOptimization.Value],
    clientData:    ClientData
  ): Future[HandlerResult[ResponseSeqDate]] = {
    authorized(clientData) { implicit client ⇒
      addOptimizations(optimizations)
      withGroupOutPeer(groupPeer) {
        for {
          SeqStateDate(seq, state, date) ← groupExt.updateTopic(groupPeer.groupId, client.userId, client.authId, topic, randomId) //isV2(optimizations)
        } yield Ok(ResponseSeqDate(seq, state.toByteArray, date))
      }
    }
  }

  override def doHandleEditGroupAbout(
    groupPeer:     ApiGroupOutPeer,
    randomId:      Long,
    about:         Option[String],
    optimizations: IndexedSeq[ApiUpdateOptimization.Value],
    clientData:    ClientData
  ): Future[HandlerResult[ResponseSeqDate]] = {
    authorized(clientData) { implicit client ⇒
      addOptimizations(optimizations)
      withGroupOutPeer(groupPeer) {
        for {
          SeqStateDate(seq, state, date) ← groupExt.updateAbout(groupPeer.groupId, client.userId, client.authId, about, randomId)
        } yield Ok(ResponseSeqDate(seq, state.toByteArray, date))
      }
    }
  }

  override def doHandleEditGroupShortName(groupPeer: ApiGroupOutPeer, shortName: Option[String], clientData: ClientData): Future[HandlerResult[ResponseSeq]] =
    authorized(clientData) { client ⇒
      withGroupOutPeer(groupPeer) {
        for {
          SeqState(seq, state) ← groupExt.updateShortName(groupPeer.groupId, client.userId, client.authId, shortName)
        } yield Ok(ResponseSeq(seq, state.toByteArray))
      }
    }

  protected def doHandleDeleteGroup(groupPeer: ApiGroupOutPeer, clientData: ClientData): Future[HandlerResult[ResponseSeq]] =
    authorized(clientData) { client ⇒
      withGroupOutPeer(groupPeer) {
        for {
          SeqState(seq, state) ← groupExt.deleteGroup(groupPeer.groupId, client.userId, client.authId)
        } yield Ok(ResponseSeq(seq, state.toByteArray))
      }
    }

  protected def doHandleShareHistory(groupPeer: ApiGroupOutPeer, clientData: ClientData): Future[HandlerResult[ResponseSeq]] =
    authorized(clientData) { client ⇒
      withGroupOutPeer(groupPeer) {
        for {
          SeqState(seq, state) ← groupExt.makeHistoryShared(groupPeer.groupId, client.userId, client.authId)
        } yield Ok(ResponseSeq(seq, state.toByteArray))
      }
    }

  private val inviteUriBase = s"${groupInviteConfig.baseUrl}/join/"

  private def genInviteUrl(token: String) = s"$inviteUriBase$token"

  private def extractJoinString(urlOrTokenOrGroupName: String): Option[String Xor String] = {
    val extracted = if (urlOrTokenOrGroupName.startsWith(groupInviteConfig.baseUrl))
      urlOrTokenOrGroupName.drop(inviteUriBase.length).takeWhile(c ⇒ c != '?' && c != '#')
    else
      urlOrTokenOrGroupName

    if (StringUtils.validGroupInviteToken(extracted)) {
      Some(Xor.left(extracted))
    } else if (StringUtils.validGlobalName(extracted)) {
      Some(Xor.right(extracted))
    } else {
      None
    }
  }

  private def addOptimizations(opts: IndexedSeq[ApiUpdateOptimization.Value])(implicit client: AuthorizedClientData): Unit =
    seqUpdExt.addOptimizations(client.userId, client.authId, opts map (_.id))

  //TODO: move to separate trait
  override def doHandleCreateGroupObsolete(randomId: Long, title: String, users: IndexedSeq[ApiUserOutPeer], clientData: ClientData): Future[HandlerResult[ResponseCreateGroupObsolete]] =
    authorized(clientData) { implicit client ⇒
      withUserOutPeers(users) {
        val groupId = nextIntId(ThreadLocalSecureRandom.current())
        val userIds = users.map(_.userId).toSet
        for {
          CreateAck(accessHash, seqStateDate) ← groupExt.create(
            groupId,
            client.userId,
            client.authId,
            title,
            randomId,
            userIds
          )
          SeqStateDate(seq, state, date) = seqStateDate.getOrElse(throw NoSeqStateDate)
        } yield Ok(ResponseCreateGroupObsolete(
          groupPeer = ApiGroupOutPeer(groupId, accessHash),
          seq = seq,
          state = state.toByteArray,
          users = (userIds + client.userId).toVector,
          date = date
        ))
      }
    }

  //TODO: move to separate trait
  override def doHandleEnterGroupObsolete(groupPeer: ApiGroupOutPeer, clientData: ClientData): Future[HandlerResult[ResponseEnterGroupObsolete]] =
    authorized(clientData) { implicit client ⇒
      withGroupOutPeer(groupPeer) {
        for {
          // TODO: what should it be? was
          // isPublic ← groupExt.isPublic(groupPeer.groupId)
          isHistoryShared ← groupExt.isHistoryShared(groupPeer.groupId)
          result ← if (isHistoryShared) {
            db.run(
              for {
                member ← GroupUserRepo.find(groupPeer.groupId, client.userId): @silent
                response ← member match {
                  case Some(_) ⇒ DBIO.successful(Error(GroupRpcErrors.AlreadyInvited))
                  case None ⇒
                    for {
                      groupStruct ← DBIO.from(groupExt.getApiStruct(groupPeer.groupId, client.userId))
                      (seqstatedate, userIds, randomId) ← DBIO.from(groupExt.joinGroup(groupPeer.groupId, client.userId, client.authId, Some(groupStruct.creatorUserId)))
                      userStructs ← DBIO.from(Future.sequence(userIds.map(userExt.getApiStruct(_, client.userId, client.authId))))

                    } yield Ok(ResponseEnterGroupObsolete(groupStruct, userStructs, randomId, seqstatedate.seq, seqstatedate.state.toByteArray, seqstatedate.date))
                }
              } yield response
            )
          } else {
            FastFuture.successful(Error(GroupRpcErrors.GroupNotPublic))
          }
        } yield result
      }
    }

  /**
   * only admin can give another group member admin rights
   * if this user id already admin - `GroupErrors.UserAlreadyAdmin` will be returned
   * it could be many admins in one group
   */
  //TODO: move to separate trait
  override def doHandleMakeUserAdminObsolete(
    groupPeer:  ApiGroupOutPeer,
    userPeer:   ApiUserOutPeer,
    clientData: ClientData
  ): Future[HandlerResult[ResponseMakeUserAdminObsolete]] = {
    authorized(clientData) { implicit client ⇒
      withGroupOutPeer(groupPeer) {
        withUserOutPeer(userPeer) {
          for {
            (members, SeqStateDate(seq, state, _)) ← groupExt.makeUserAdmin(groupPeer.groupId, client.userId, client.authId, userPeer.userId)
          } yield Ok(ResponseMakeUserAdminObsolete(members, seq, state.toByteArray))
        }
      }
    }
  }

  override def onFailure: PartialFunction[Throwable, RpcError] = recoverCommon orElse {
    case GroupErrors.NotAMember              ⇒ CommonRpcErrors.forbidden("Not a group member!")
    case GroupErrors.NotAdmin                ⇒ CommonRpcErrors.forbidden("Only group admin can perform this action.")
    case GroupErrors.NotOwner                ⇒ CommonRpcErrors.forbidden("Only group owner can perform this action.")
    case GroupErrors.UserAlreadyAdmin        ⇒ GroupRpcErrors.UserAlreadyAdmin
    case GroupErrors.UserAlreadyNotAdmin     ⇒ GroupRpcErrors.UserAlreadyNotAdmin
    case GroupErrors.InvalidTitle            ⇒ GroupRpcErrors.InvalidTitle
    case GroupErrors.AboutTooLong            ⇒ GroupRpcErrors.AboutTooLong
    case GroupErrors.TopicTooLong            ⇒ GroupRpcErrors.TopicTooLong
    case GroupErrors.BlockedByUser           ⇒ GroupRpcErrors.BlockedByUser
    case FileErrors.LocationInvalid          ⇒ FileRpcErrors.LocationInvalid
    case GroupErrors.UserAlreadyInvited      ⇒ GroupRpcErrors.AlreadyInvited
    case GroupErrors.UserAlreadyJoined       ⇒ GroupRpcErrors.AlreadyJoined
    case GroupErrors.GroupIdAlreadyExists(_) ⇒ GroupRpcErrors.GroupIdAlreadyExists
    case GroupErrors.InvalidShortName        ⇒ GroupRpcErrors.InvalidShortName
    case GroupErrors.ShortNameTaken          ⇒ GroupRpcErrors.ShortNameTaken
    case GroupErrors.NoPermission            ⇒ GroupRpcErrors.NoPermission
    case GroupErrors.CantLeaveGroup          ⇒ GroupRpcErrors.CantLeaveGroup
    case GroupErrors.UserIsBanned            ⇒ GroupRpcErrors.UserIsBanned
  }

}
