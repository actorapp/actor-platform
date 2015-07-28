package im.actor.server.api.rpc.service.groups

import scala.concurrent.duration._
import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.concurrent.{ ExecutionContext, Future }

import akka.actor.ActorSystem
import akka.util.Timeout
import slick.dbio.DBIO
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc.PeerHelpers._
import im.actor.api.rpc._
import im.actor.api.rpc.files.FileLocation
import im.actor.api.rpc.groups._
import im.actor.api.rpc.misc.ResponseSeqDate
import im.actor.api.rpc.peers.{ GroupOutPeer, UserOutPeer }
import im.actor.server.api.ApiConversions._
import im.actor.server.file.FileErrors
import im.actor.server.group.{ GroupCommands, GroupErrors, GroupOffice, GroupOfficeRegion }
import im.actor.server.presences.{ GroupPresenceManager, GroupPresenceManagerRegion }
import im.actor.server.push.SeqUpdatesManagerRegion
import im.actor.server.sequence.SeqStateDate
import im.actor.server.util.ACLUtils.accessToken
import im.actor.server.util.UserUtils._
import im.actor.server.util._
import im.actor.server.{ models, persist }

class GroupsServiceImpl(groupInviteConfig: GroupInviteConfig)(
  implicit
  seqUpdManagerRegion:        SeqUpdatesManagerRegion,
  groupPresenceManagerRegion: GroupPresenceManagerRegion,
  groupPeerManagerRegion:     GroupOfficeRegion,
  fsAdapter:                  FileStorageAdapter,
  db:                         Database,
  actorSystem:                ActorSystem
) extends GroupsService {

  import FileHelpers._
  import GroupCommands._
  import IdUtils._
  import ImageUtils._

  override implicit val ec: ExecutionContext = actorSystem.dispatcher
  private implicit val timeout = Timeout(5.seconds)

  override def jhandleEditGroupAvatar(groupOutPeer: GroupOutPeer, randomId: Long, fileLocation: FileLocation, clientData: ClientData): Future[HandlerResult[ResponseEditGroupAvatar]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client ⇒
      withOwnGroupMember(groupOutPeer, client.userId) { fullGroup ⇒
        withFileLocation(fileLocation, AvatarSizeLimit) {
          scaleAvatar(fileLocation.fileId, ThreadLocalRandom.current()) flatMap {
            case Right(avatar) ⇒
              for {
                UpdateAvatarResponse(avatar, SeqStateDate(seq, state, date)) ← DBIO.from(GroupOffice.updateAvatar(fullGroup.id, client.userId, client.authId, Some(avatar), randomId))
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
    }

    db.run(toDBIOAction(authorizedAction)) recover {
      case FileErrors.LocationInvalid ⇒ Error(Errors.LocationInvalid)
    }
  }

  override def jhandleRemoveGroupAvatar(groupOutPeer: GroupOutPeer, randomId: Long, clientData: ClientData): Future[HandlerResult[ResponseSeqDate]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client ⇒
      withOwnGroupMember(groupOutPeer, client.userId) { fullGroup ⇒
        for {
          UpdateAvatarResponse(avatar, SeqStateDate(seq, state, date)) ← DBIO.from(GroupOffice.updateAvatar(fullGroup.id, client.userId, client.authId, None, randomId))
        } yield Ok(ResponseSeqDate(
          seq,
          state.toByteArray,
          date
        ))
      }
    }

    db.run(toDBIOAction(authorizedAction))
  }

  override def jhandleKickUser(groupOutPeer: GroupOutPeer, randomId: Long, userOutPeer: UserOutPeer, clientData: ClientData): Future[HandlerResult[ResponseSeqDate]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client ⇒
      withKickableGroupMember(groupOutPeer, userOutPeer) { fullGroup ⇒ //maybe move to group peer manager
        for {
          //todo: get rid of DBIO.from
          SeqStateDate(seq, state, date) ← DBIO.from(GroupOffice.kickUser(fullGroup.id, userOutPeer.userId, randomId))
        } yield {
          GroupPresenceManager.notifyGroupUserRemoved(fullGroup.id, userOutPeer.userId)
          Ok(ResponseSeqDate(seq, state.toByteArray, date))
        }
      }
    }
    db.run(toDBIOAction(authorizedAction map (_.transactionally)))
  }

  override def jhandleLeaveGroup(groupOutPeer: GroupOutPeer, randomId: Long, clientData: ClientData): Future[HandlerResult[ResponseSeqDate]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client ⇒
      withOwnGroupMember(groupOutPeer, client.userId) { fullGroup ⇒
        for {
          SeqStateDate(seq, state, date) ← DBIO.from(GroupOffice.leaveGroup(fullGroup.id, randomId))
        } yield {
          GroupPresenceManager.notifyGroupUserRemoved(fullGroup.id, client.userId)
          Ok(ResponseSeqDate(seq, state.toByteArray, date))
        }
      }
    }
    db.run(toDBIOAction(authorizedAction map (_.transactionally)))
  }

  override def jhandleCreateGroup(randomId: Long, title: String, users: Vector[UserOutPeer], clientData: ClientData): Future[HandlerResult[ResponseCreateGroup]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client ⇒
      withUserOutPeers(users) {
        withValidGroupTitle(title) { validTitle ⇒
          val groupId = nextIntId(ThreadLocalRandom.current())
          val userIds = users.map(_.userId).toSet
          val groupUserIds = userIds + client.userId

          val f = for (res ← GroupOffice.create(groupId, title, randomId, userIds)) yield {
            Ok(ResponseCreateGroup(
              groupPeer = GroupOutPeer(groupId, res.accessHash),
              seq = res.seq,
              state = res.state.toByteArray,
              users = groupUserIds.toVector,
              date = res.date
            ))
          }

          DBIO.from(f)
        }
      }
    }

    db.run(toDBIOAction(authorizedAction map (_.transactionally)))
  }

  override def jhandleInviteUser(groupOutPeer: GroupOutPeer, randomId: Long, userOutPeer: UserOutPeer, clientData: ClientData): Future[HandlerResult[ResponseSeqDate]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client ⇒
      withOwnGroupMember(groupOutPeer, client.userId) { fullGroup ⇒
        withUserOutPeer(userOutPeer) {
          for {
            res ← DBIO.from(GroupOffice.inviteToGroup(fullGroup.id, userOutPeer.userId, randomId))
          } yield {
            GroupPresenceManager.notifyGroupUserAdded(fullGroup.id, userOutPeer.userId)
            Ok(ResponseSeqDate(res.seq, res.state.toByteArray, res.date))
          }
        }
      }
    }

    db.run(toDBIOAction(authorizedAction)) recover {
      case GroupErrors.UserAlreadyInvited ⇒ Error(GroupRpcErrors.UserAlreadyInvited)
    }
  }

  override def jhandleEditGroupTitle(groupOutPeer: GroupOutPeer, randomId: Long, title: String, clientData: ClientData): Future[HandlerResult[ResponseSeqDate]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client ⇒
      withOwnGroupMember(groupOutPeer, client.userId) { fullGroup ⇒
        for {
          SeqStateDate(seq, state, date) ← DBIO.from(GroupOffice.updateTitle(fullGroup.id, client.userId, client.authId, title, randomId))
        } yield Ok(ResponseSeqDate(seq, state.toByteArray, date))
      }
    }

    db.run(toDBIOAction(authorizedAction map (_.transactionally)))
  }

  override def jhandleGetGroupInviteUrl(groupPeer: GroupOutPeer, clientData: ClientData): Future[HandlerResult[ResponseInviteUrl]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client ⇒
      withOwnGroupMember(groupPeer, client.userId) { fullGroup ⇒
        for {
          token ← persist.GroupInviteToken.find(fullGroup.id, client.userId).headOption.flatMap {
            case Some(invToken) ⇒ DBIO.successful(invToken.token)
            case None ⇒
              val token = accessToken(ThreadLocalRandom.current())
              val inviteToken = models.GroupInviteToken(fullGroup.id, client.userId, token)
              for (_ ← persist.GroupInviteToken.create(inviteToken)) yield token
          }
        } yield Ok(ResponseInviteUrl(genInviteUrl(groupInviteConfig.baseUrl, token)))
      }
    }
    db.run(toDBIOAction(authorizedAction))
  }

  override def jhandleJoinGroup(url: String, clientData: ClientData): Future[HandlerResult[ResponseJoinGroup]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client ⇒
      withValidInviteToken(groupInviteConfig.baseUrl, url) { (fullGroup, token) ⇒
        val group = models.Group.fromFull(fullGroup)

        val join = GroupOffice.joinGroup(
          groupId = group.id,
          joiningUserId = client.userId,
          joiningUserAuthId = client.authId,
          invitingUserId = token.creatorId
        )
        for {
          (seqstatedate, userIds, randomId) ← DBIO.from(join)
          users ← persist.User.findByIds(userIds.toSet)
          userStructs ← DBIO.sequence(users.map(userStruct(_, client.userId, client.authId)))
          groupStruct ← GroupUtils.getGroupStructUnsafe(group)
        } yield Ok(ResponseJoinGroup(groupStruct, seqstatedate.seq, seqstatedate.state.toByteArray, seqstatedate.date, userStructs.toVector, randomId))
      }
    }
    db.run(toDBIOAction(authorizedAction)) recover {
      case GroupErrors.UserAlreadyInvited ⇒ Error(GroupRpcErrors.UserAlreadyInvited)
    }
  }

  override def jhandleJoinGroupDirect(peer: GroupOutPeer, clientData: ClientData): Future[HandlerResult[ResponseJoinGroupDirect]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client ⇒
      withPublicGroup(peer) { fullGroup ⇒
        persist.GroupUser.find(fullGroup.id, client.userId) flatMap {
          case Some(_) ⇒ DBIO.successful(Error(GroupRpcErrors.UserAlreadyInvited))
          case None ⇒
            val group = models.Group.fromFull(fullGroup)
            for {
              (seqstatedate, userIds, randomId) ← DBIO.from(GroupOffice.joinGroup(group.id, client.userId, client.authId, fullGroup.creatorUserId))
              users ← persist.User.findByIds(userIds.toSet)
              userStructs ← DBIO.sequence(users.map(userStruct(_, client.userId, client.authId)))
              groupStruct ← GroupUtils.getGroupStructUnsafe(group)
            } yield Ok(ResponseJoinGroupDirect(groupStruct, userStructs.toVector, randomId, seqstatedate.seq, seqstatedate.state.toByteArray, seqstatedate.date))
        }
      }
    }

    db.run(toDBIOAction(authorizedAction)) recover {
      case GroupErrors.UserAlreadyInvited ⇒ Error(GroupRpcErrors.UserAlreadyInvited)
    }
  }

  override def jhandleRevokeInviteUrl(groupPeer: GroupOutPeer, clientData: ClientData): Future[HandlerResult[ResponseInviteUrl]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client ⇒
      withOwnGroupMember(groupPeer, client.userId) { fullGroup ⇒
        val token = accessToken(ThreadLocalRandom.current())
        val inviteToken = models.GroupInviteToken(fullGroup.id, client.userId, token)

        for {
          _ ← persist.GroupInviteToken.revoke(fullGroup.id, client.userId)
          _ ← persist.GroupInviteToken.create(inviteToken)
        } yield Ok(ResponseInviteUrl(genInviteUrl(groupInviteConfig.baseUrl, token)))
      }
    }
    db.run(toDBIOAction(authorizedAction))
  }

}
