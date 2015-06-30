package im.actor.server.api.rpc.service.groups

import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.concurrent.{ ExecutionContext, Future }

import akka.actor.ActorSystem
import akka.util.Timeout
import com.amazonaws.services.s3.transfer.TransferManager
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
import im.actor.server.api.rpc.service.groups.GroupHelpers._
import im.actor.server.models.UserState.Registered
import im.actor.server.peermanagers.{ GroupPeerManager, GroupPeerManagerRegion }
import im.actor.server.presences.{ GroupPresenceManager, GroupPresenceManagerRegion }
import im.actor.server.push.SeqUpdatesManager._
import im.actor.server.push.SeqUpdatesManagerRegion
import im.actor.server.util.ACLUtils.{ accessToken, nextAccessSalt }
import im.actor.server.util.UserUtils._
import im.actor.server.util._
import im.actor.server.{ models, persist }

class GroupsServiceImpl(bucketName: String, groupInviteConfig: GroupInviteConfig)(
  implicit
  seqUpdManagerRegion:        SeqUpdatesManagerRegion,
  groupPresenceManagerRegion: GroupPresenceManagerRegion,
  groupPeerManagerRegion:     GroupPeerManagerRegion,
  transferManager:            TransferManager,
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
          scaleAvatar(fileLocation.fileId, ThreadLocalRandom.current(), bucketName) flatMap {
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
      withKickableGroupMember(groupOutPeer, userOutPeer) { fullGroup ⇒
        val date = new DateTime

        val update = UpdateGroupUserKick(fullGroup.id, userOutPeer.userId, client.userId, date.getMillis, randomId)
        val serviceMessage = GroupServiceMessages.userKicked(userOutPeer.userId)

        for {
          _ ← persist.GroupUser.delete(fullGroup.id, userOutPeer.userId)
          _ ← persist.GroupInviteToken.revoke(fullGroup.id, userOutPeer.userId) //TODO: move to cleanup helper, with all cleanup code and use in kick/leave
          groupUserIds ← persist.GroupUser.findUserIds(fullGroup.id)
          (seqstate, _) ← broadcastClientAndUsersUpdate(groupUserIds.toSet, update, Some(PushTexts.Kicked))
          _ ← HistoryUtils.writeHistoryMessage(
            models.Peer.privat(client.userId),
            models.Peer.group(fullGroup.id),
            date,
            randomId,
            serviceMessage.header,
            serviceMessage.toByteArray
          )
        } yield {
          GroupPresenceManager.notifyGroupUserRemoved(fullGroup.id, userOutPeer.userId)
          Ok(ResponseSeqDate(seqstate._1, seqstate._2, date.getMillis))
        }
      }
    }

    db.run(toDBIOAction(authorizedAction map (_.transactionally)))
  }

  override def jhandleLeaveGroup(groupOutPeer: GroupOutPeer, randomId: Long, clientData: ClientData): Future[HandlerResult[ResponseSeqDate]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client ⇒
      withOwnGroupMember(groupOutPeer, client.userId) { fullGroup ⇒
        val date = new DateTime

        val update = UpdateGroupUserLeave(fullGroup.id, client.userId, date.getMillis, randomId)
        val serviceMessage = GroupServiceMessages.userLeft(client.userId)

        for {
          groupUserIds ← persist.GroupUser.findUserIds(fullGroup.id)
          _ ← persist.GroupUser.delete(fullGroup.id, client.userId)
          _ ← persist.GroupInviteToken.revoke(fullGroup.id, client.userId) //TODO: move to cleanup helper, with all cleanup code and use in kick/leave
          (seqstate, _) ← broadcastClientAndUsersUpdate(groupUserIds.toSet, update, Some(PushTexts.Left))
          _ ← HistoryUtils.writeHistoryMessage(
            models.Peer.privat(client.userId),
            models.Peer.group(fullGroup.id),
            date,
            randomId,
            serviceMessage.header,
            serviceMessage.toByteArray
          )
        } yield {
          GroupPresenceManager.notifyGroupUserRemoved(fullGroup.id, client.userId)
          Ok(ResponseSeqDate(seqstate._1, seqstate._2, date.getMillis))
        }
      }
    }

    db.run(toDBIOAction(authorizedAction map (_.transactionally)))
  }

  override def jhandleCreateGroup(randomId: Long, title: String, users: Vector[UserOutPeer], clientData: ClientData): Future[HandlerResult[ResponseCreateGroup]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client ⇒
      withUserOutPeers(users) {
        withValidGroupTitle(title) { validTitle ⇒
          val dateTime = new DateTime()
          val rnd = ThreadLocalRandom.current()

          val group = models.Group(
            id = nextIntId(rnd),
            creatorUserId = client.userId,
            accessHash = rnd.nextLong(),
            title = title,
            isPublic = false,
            createdAt = dateTime,
            description = ""
          )

          val bot = models.User(
            id = nextIntId(rnd),
            accessSalt = nextAccessSalt(rnd),
            name = "Bot",
            countryCode = "US",
            sex = models.NoSex,
            state = Registered,
            isBot = true
          )
          val botToken = accessToken(rnd)

          val userIds = users.map(_.userId).toSet
          val groupUserIds = userIds + client.userId

          val update = UpdateGroupInvite(groupId = group.id, inviteUserId = client.userId, date = dateTime.getMillis, randomId = randomId)
          val serviceMessage = GroupServiceMessages.groupCreated

          for {
            _ ← persist.Group.create(group, randomId)
            _ ← persist.GroupUser.create(group.id, groupUserIds, client.userId, dateTime, None)
            _ ← persist.User.create(bot)
            _ ← persist.GroupBot.create(group.id, bot.id, botToken)
            _ ← HistoryUtils.writeHistoryMessage(
              models.Peer.privat(client.userId),
              models.Peer.group(group.id),
              dateTime,
              randomId,
              serviceMessage.header,
              serviceMessage.toByteArray
            )
            _ ← DBIO.sequence(userIds.map(userId ⇒ broadcastUserUpdate(userId, update, Some("You are invited to a group"))).toSeq)
            seqstate ← broadcastClientUpdate(update, None)
          } yield {
            Ok(ResponseCreateGroup(
              groupPeer = GroupOutPeer(group.id, group.accessHash),
              seq = seqstate._1,
              state = seqstate._2,
              users = groupUserIds.toVector,
              date = dateTime.getMillis
            ))
          }
        }
      }
    }

    db.run(toDBIOAction(authorizedAction map (_.transactionally)))
  }

  override def jhandleInviteUser(groupOutPeer: GroupOutPeer, randomId: Long, userOutPeer: UserOutPeer, clientData: ClientData): Future[HandlerResult[ResponseSeqDate]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client ⇒
      withOwnGroupMember(groupOutPeer, client.userId) { fullGroup ⇒
        withUserOutPeer(userOutPeer) {
          handleInvite(fullGroup, userOutPeer.userId, randomId) {
            case (seqstate, dateMillis) ⇒
              GroupPresenceManager.notifyGroupUserAdded(fullGroup.id, userOutPeer.userId)
              Ok(ResponseSeqDate(seqstate._1, seqstate._2, dateMillis))
          }
        }
      }
    }

    db.run(toDBIOAction(authorizedAction map (_.transactionally)))
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

        val join = GroupPeerManager.joinGroup(
          group = group,
          joiningUserId = client.userId,
          joiningUserAuthId = client.authId,
          invitingUserId = token.creatorId
        )
        for {
          optJoin ← DBIO.from(join)
          result ← optJoin.map {
            case (seqstate, userIds, dateMillis, randomId) ⇒
              for {
                users ← persist.User.findByIds(userIds.toSet)
                userStructs ← DBIO.sequence(users.map(userStruct(_, client.userId, client.authId)))

                groupStruct ← GroupUtils.getGroupStructUnsafe(group)
              } yield Ok(ResponseJoinGroup(groupStruct, seqstate._1, seqstate._2, dateMillis, userStructs.toVector, randomId))
          }.getOrElse(DBIO.successful(Error(GroupErrors.UserAlreadyInvited)))
        } yield result
      }
    }
    db.run(toDBIOAction(authorizedAction))
  }

  override def jhandleJoinGroupDirect(peer: GroupOutPeer, clientData: ClientData): Future[HandlerResult[ResponseJoinGroupDirect]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client ⇒
      withPublicGroup(peer) { fullGroup ⇒
        persist.GroupUser.find(fullGroup.id, client.userId) flatMap {
          case Some(_) ⇒ DBIO.successful(Error(GroupErrors.UserAlreadyInvited))
          case None ⇒
            val group = models.Group.fromFull(fullGroup)
            for {
              optJoin ← DBIO.from(GroupPeerManager.joinGroup(group, client.userId, client.authId, fullGroup.creatorUserId))
              result ← optJoin.map {
                case (seqstate, userIds, dateMillis, randomId) ⇒
                  for {
                    users ← persist.User.findByIds(userIds.toSet)
                    userStructs ← DBIO.sequence(users.map(userStruct(_, client.userId, client.authId)))
                    groupStruct ← GroupUtils.getGroupStructUnsafe(group)
                  } yield Ok(ResponseJoinGroupDirect(groupStruct, userStructs.toVector, randomId, seqstate._1, seqstate._2, dateMillis))
              }.getOrElse(DBIO.successful(Error(GroupErrors.UserAlreadyInvited)))
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
