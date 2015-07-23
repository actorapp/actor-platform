package im.actor.server.api.rpc.service.groups

import java.time.{ LocalDateTime, ZoneOffset }

import im.actor.server.group.{ GroupOffice, GroupOfficeRegion }

import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.concurrent.{ ExecutionContext, Future }

import akka.actor.ActorSystem
import akka.util.Timeout
import org.joda.time.DateTime
import slick.dbio.DBIO
import slick.driver.PostgresDriver.api._

import scala.concurrent.duration._
import im.actor.api.rpc.PeerHelpers._
import im.actor.api.rpc._
import im.actor.api.rpc.files.FileLocation
import im.actor.api.rpc.groups._
import im.actor.api.rpc.misc.ResponseSeqDate
import im.actor.api.rpc.peers.{ GroupOutPeer, UserOutPeer }
import im.actor.server.models.UserState.Registered
import im.actor.server.presences.{ GroupPresenceManager, GroupPresenceManagerRegion }
import im.actor.server.push.SeqUpdatesManager._
import im.actor.server.push.SeqUpdatesManagerRegion
import im.actor.server.sequence.SeqStateDate
import im.actor.server.util.ACLUtils.{ accessToken, nextAccessSalt }
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
              val date = new DateTime
              val avatarData = getAvatarData(models.AvatarData.OfGroup, fullGroup.id, avatar)

              val update = UpdateGroupAvatarChanged(fullGroup.id, client.userId, Some(avatar), date.getMillis, randomId)
              val serviceMessage = GroupServiceMessages.changedAvatar(Some(avatar))

              for {
                _ ← persist.AvatarData.createOrUpdate(avatarData)
                groupUserIds ← persist.GroupUser.findUserIds(fullGroup.id)
                _ ← broadcastClientAndUsersUpdate(groupUserIds.toSet, update, None)
                seqstate ← broadcastClientUpdate(update, None)
                _ ← HistoryUtils.writeHistoryMessage(
                  models.Peer.privat(client.userId),
                  models.Peer.group(fullGroup.id),
                  date,
                  randomId,
                  serviceMessage.header,
                  serviceMessage.toByteArray
                )
              } yield {
                Ok(ResponseEditGroupAvatar(avatar, seqstate._1, seqstate._2, date.getMillis))
              }
            case Left(e) ⇒
              actorSystem.log.error(e, "Failed to scale group avatar")
              DBIO.successful(Error(Errors.LocationInvalid))
          }
        }
      }
    }

    db.run(toDBIOAction(authorizedAction map (_.transactionally)))
  }

  override def jhandleRemoveGroupAvatar(groupOutPeer: GroupOutPeer, randomId: Long, clientData: ClientData): Future[HandlerResult[ResponseSeqDate]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client ⇒
      withOwnGroupMember(groupOutPeer, client.userId) { fullGroup ⇒
        val date = new DateTime
        val update = UpdateGroupAvatarChanged(fullGroup.id, client.userId, None, date.getMillis, randomId)
        val serviceMessage = GroupServiceMessages.changedAvatar(None)

        for {
          _ ← persist.AvatarData.createOrUpdate(models.AvatarData.empty(models.AvatarData.OfGroup, fullGroup.id.toLong))
          groupUserIds ← persist.GroupUser.findUserIds(fullGroup.id)
          _ ← broadcastClientAndUsersUpdate(groupUserIds.toSet, update, None)
          seqstate ← broadcastClientUpdate(update, None)
          _ ← HistoryUtils.writeHistoryMessage(
            models.Peer.privat(client.userId),
            models.Peer.group(fullGroup.id),
            date,
            randomId,
            serviceMessage.header,
            serviceMessage.toByteArray
          )
        } yield Ok(ResponseSeqDate(seqstate._1, seqstate._2, date.getMillis))
      }
    }

    db.run(toDBIOAction(authorizedAction map (_.transactionally)))
  }

  override def jhandleKickUser(groupOutPeer: GroupOutPeer, randomId: Long, userOutPeer: UserOutPeer, clientData: ClientData): Future[HandlerResult[ResponseSeqDate]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client ⇒
      withKickableGroupMember(groupOutPeer, userOutPeer) { fullGroup ⇒ //maybe move to group peer manager
        for {
          //todo: get rid of DBIO.from
          (seqstate, date) ← DBIO.from(GroupOffice.kickUser(fullGroup.id, userOutPeer.userId, randomId))
        } yield {
          GroupPresenceManager.notifyGroupUserRemoved(fullGroup.id, userOutPeer.userId)
          Ok(ResponseSeqDate(seqstate._1, seqstate._2, date))
        }
      }
    }
    db.run(toDBIOAction(authorizedAction map (_.transactionally)))
  }

  override def jhandleLeaveGroup(groupOutPeer: GroupOutPeer, randomId: Long, clientData: ClientData): Future[HandlerResult[ResponseSeqDate]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client ⇒
      withOwnGroupMember(groupOutPeer, client.userId) { fullGroup ⇒
        for {
          (seqstate, date) ← DBIO.from(GroupOffice.leaveGroup(fullGroup.id, randomId))
        } yield {
          GroupPresenceManager.notifyGroupUserRemoved(fullGroup.id, client.userId)
          Ok(ResponseSeqDate(seqstate._1, seqstate._2, date))
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
            optInvite ← DBIO.from(GroupOffice.inviteToGroup(fullGroup, userOutPeer.userId, randomId))
            result ← DBIO.successful(optInvite map {
              case (seqstate, date) ⇒
                GroupPresenceManager.notifyGroupUserAdded(fullGroup.id, userOutPeer.userId)
                Ok(ResponseSeqDate(seqstate._1, seqstate._2, date))
            } getOrElse Error(GroupRpcErrors.UserAlreadyInvited))
          } yield result
        }
      }
    }

    db.run(toDBIOAction(authorizedAction))
  }

  override def jhandleEditGroupTitle(groupOutPeer: GroupOutPeer, randomId: Long, title: String, clientData: ClientData): Future[HandlerResult[ResponseSeqDate]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client ⇒
      withOwnGroupMember(groupOutPeer, client.userId) { fullGroup ⇒
        val date = new DateTime
        val dateMillis = date.getMillis

        val update = UpdateGroupTitleChanged(groupId = fullGroup.id, userId = client.userId, title = title, date = dateMillis, randomId = randomId)
        val serviceMessage = GroupServiceMessages.changedTitle(title)

        for {
          _ ← persist.Group.updateTitle(fullGroup.id, title, client.userId, randomId, date)
          _ ← HistoryUtils.writeHistoryMessage(
            models.Peer.privat(client.userId),
            models.Peer.group(fullGroup.id),
            date,
            randomId,
            serviceMessage.header,
            serviceMessage.toByteArray
          )
          userIds ← persist.GroupUser.findUserIds(fullGroup.id)
          (seqstate, _) ← broadcastClientAndUsersUpdate(userIds.toSet, update, Some(PushTexts.TitleChanged))
        } yield Ok(ResponseSeqDate(seqstate._1, seqstate._2, dateMillis))
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
          optJoin ← DBIO.from(join)
          result ← optJoin.map {
            case (SeqStateDate(seq, state, date), userIds, randomId) ⇒
              for {
                users ← persist.User.findByIds(userIds.toSet)
                userStructs ← DBIO.sequence(users.map(userStruct(_, client.userId, client.authId)))

                groupStruct ← GroupUtils.getGroupStructUnsafe(group)
              } yield Ok(ResponseJoinGroup(groupStruct, seq, state.toByteArray, date, userStructs.toVector, randomId))
          }.getOrElse(DBIO.successful(Error(GroupRpcErrors.UserAlreadyInvited)))
        } yield result
      }
    }
    db.run(toDBIOAction(authorizedAction))
  }

  override def jhandleJoinGroupDirect(peer: GroupOutPeer, clientData: ClientData): Future[HandlerResult[ResponseJoinGroupDirect]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client ⇒
      withPublicGroup(peer) { fullGroup ⇒
        persist.GroupUser.find(fullGroup.id, client.userId) flatMap {
          case Some(_) ⇒ DBIO.successful(Error(GroupRpcErrors.UserAlreadyInvited))
          case None ⇒
            val group = models.Group.fromFull(fullGroup)
            for {
              optJoin ← DBIO.from(GroupOffice.joinGroup(group.id, client.userId, client.authId, fullGroup.creatorUserId))
              result ← optJoin.map {
                case (SeqStateDate(seq, state, date), userIds, randomId) ⇒
                  for {
                    users ← persist.User.findByIds(userIds.toSet)
                    userStructs ← DBIO.sequence(users.map(userStruct(_, client.userId, client.authId)))
                    groupStruct ← GroupUtils.getGroupStructUnsafe(group)
                  } yield Ok(ResponseJoinGroupDirect(groupStruct, userStructs.toVector, randomId, seq, state.toByteArray, date))
              }.getOrElse(DBIO.successful(Error(GroupRpcErrors.UserAlreadyInvited)))
            } yield result
        }
      }
    }

    db.run(toDBIOAction(authorizedAction))
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
