package im.actor.server.api.rpc.service.groups

import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.concurrent.{ ExecutionContext, Future }

import akka.actor.{ ActorRef, ActorSystem }
import org.joda.time.DateTime
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc._
import im.actor.api.rpc.Implicits._
import im.actor.api.rpc.files.FileLocation
import im.actor.api.rpc.groups._
import im.actor.api.rpc.misc.ResponseSeqDate
import im.actor.api.rpc.peers.{ GroupOutPeer, UserOutPeer }
import im.actor.server.api.util.PeerUtils._
import im.actor.server.push.SeqUpdatesManager._
import im.actor.server.push.SeqUpdatesManagerRegion
import im.actor.server.{ models, persist }

class GroupsServiceImpl(seqUpdManagerRegion: SeqUpdatesManagerRegion)(
  implicit val db: Database, val actorSystem: ActorSystem
  ) extends GroupsService {
  override implicit val ec: ExecutionContext = actorSystem.dispatcher

  override def jhandleEditGroupAvatar(groupPeer: GroupOutPeer, randomId: Long, fileLocation: FileLocation, clientData: ClientData): Future[HandlerResult[ResponseEditGroupAvatar]] = throw new NotImplementedError

  override def jhandleKickUser(groupPeer: GroupOutPeer, randomId: Long, user: UserOutPeer, clientData: ClientData): Future[HandlerResult[ResponseSeqDate]] = throw new NotImplementedError

  override def jhandleLeaveGroup(groupPeer: GroupOutPeer, randomId: Long, clientData: ClientData): Future[HandlerResult[ResponseSeqDate]] = throw new NotImplementedError

  override def jhandleCreateGroup(randomId: Long, title: String, users: Vector[UserOutPeer], clientData: ClientData): Future[HandlerResult[ResponseCreateGroup]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client =>
      withUserOutPeers(users) {
        val dateTime = new DateTime()
        val rnd = ThreadLocalRandom.current()

        val group = models.Group(
          id = rnd.nextInt(),
          creatorUserId = client.userId,
          accessHash = rnd.nextLong(),
          title = title,
          createdAt = dateTime)

        val userIds = users.map(_.userId).toSet
        val groupUserIds = userIds + client.userId

        val update = UpdateGroupInvite(groupId = group.id, inviteUserId = client.userId, date = dateTime.getMillis, randomId = randomId)

        for {
          _ <- persist.Group.create(group, randomId)
          _ <- persist.GroupUser.create(group.id, groupUserIds, client.userId, dateTime)
          // TODO: write service message groupCreated
          _ <- DBIO.sequence(userIds.map(userId => broadcastUserUpdate(seqUpdManagerRegion, userId, update)).toSeq)
          seqstate <- broadcastClientUpdate(seqUpdManagerRegion, update)
        } yield {
          Ok(ResponseCreateGroup(
            groupPeer = GroupOutPeer(group.id, group.accessHash),
            seq = seqstate._1,
            state = seqstate._2,
            users = groupUserIds.toVector,
            date = dateTime.getMillis))
        }
      }
    }

    db.run(toDBIOAction(authorizedAction map (_.transactionally)))
  }

  override def jhandleRemoveGroupAvatar(groupPeer: GroupOutPeer, randomId: Long, clientData: ClientData): Future[HandlerResult[ResponseSeqDate]] = throw new NotImplementedError

  override def jhandleInviteUser(groupOutPeer: GroupOutPeer, randomId: Long, userOutPeer: UserOutPeer, clientData: ClientData): Future[HandlerResult[ResponseSeqDate]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client =>
      withGroupOutPeer(groupOutPeer) { fullGroup => withUserOutPeer(userOutPeer) {
        persist.GroupUser.find(fullGroup.id).flatMap { groupUsers =>
          val userIds = groupUsers.map(_.userId)

          if (!userIds.contains(userOutPeer.userId)) {
            val date = new DateTime
            val dateMillis = date.getMillis

            val newGroupMembers = groupUsers.map(_.toMember) :+ Member(userOutPeer.userId, client.userId, dateMillis)

            val invitingUserUpdates = Seq(
              UpdateGroupInvite(groupId = fullGroup.id, randomId = randomId, inviteUserId = client.userId, date = dateMillis),
              UpdateGroupTitleChanged(groupId = fullGroup.id, randomId = fullGroup.titleChangeRandomId, userId = fullGroup.titleChangerUserId, title = fullGroup.title, date = dateMillis),
              // TODO: put avatar here
              UpdateGroupAvatarChanged(groupId = fullGroup.id, randomId = fullGroup.avatarChangeRandomId, userId = fullGroup.avatarChangerUserId, avatar = None, date = dateMillis),
              UpdateGroupMembersUpdate(groupId = fullGroup.id, members = newGroupMembers.toVector)
            )

            val userAddedUpdate = UpdateGroupUserAdded(groupId = fullGroup.id, userId = userOutPeer.userId, inviterUserId = client.userId, date = dateMillis, randomId = randomId)

            for {
              _ <- persist.GroupUser.create(fullGroup.id, userOutPeer.userId, client.userId, date)
              _ <- DBIO.sequence(invitingUserUpdates map (broadcastUserUpdate(seqUpdManagerRegion, userOutPeer.userId, _)))
              // TODO: #perf the following broadcasts do update serializing per each user
              _ <- DBIO.sequence(userIds.filterNot(_ == client.userId).map(broadcastUserUpdate(seqUpdManagerRegion, _, userAddedUpdate)))
              seqstate <- broadcastClientUpdate(seqUpdManagerRegion, userAddedUpdate)
              // TODO: write service message
            } yield {
              Ok(ResponseSeqDate(seqstate._1, seqstate._2, dateMillis))
            }
          } else {
            DBIO.successful(Error(RpcError(400, "USER_ALREADY_INVITED", "User is already a member of the group.", false, None)))
          }
        }
      }}
    }

    db.run(toDBIOAction(authorizedAction map (_.transactionally)))
  }

  override def jhandleEditGroupTitle(groupOutPeer: GroupOutPeer, randomId: Long, title: String, clientData: ClientData): Future[HandlerResult[ResponseSeqDate]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client =>
      withGroupOutPeer(groupOutPeer) { fullGroup =>
        val date = new DateTime
        val dateMillis = date.getMillis

        val update = UpdateGroupTitleChanged(groupId = fullGroup.id, userId = client.userId, title = title, date = dateMillis, randomId = randomId)

        for {
          _ <- persist.Group.updateTitle(fullGroup.id, title, client.userId, randomId, date)
          // TODO: write service message
          userIds <- persist.GroupUser.findUserIds(fullGroup.id)
          (seqstate, _) <- broadcastUpdateAll(seqUpdManagerRegion, userIds.toSet, update)
        } yield Ok(ResponseSeqDate(seqstate._1, seqstate._2, dateMillis))
      }
    }

    db.run(toDBIOAction(authorizedAction map (_.transactionally)))
  }
}
