package im.actor.server.api.rpc.service.groups

import scala.concurrent.forkjoin.ThreadLocalRandom
import scala.concurrent.{ ExecutionContext, Future }

import akka.actor.{ ActorRef, ActorSystem }
import org.joda.time.DateTime
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc._
import im.actor.api.rpc.files.FileLocation
import im.actor.api.rpc.groups.{ GroupsService, ResponseCreateGroup, ResponseEditGroupAvatar, UpdateGroupInvite }
import im.actor.api.rpc.misc.ResponseSeqDate
import im.actor.api.rpc.peers.{ GroupOutPeer, UserOutPeer }
import im.actor.server.api.util.PeerUtils._
import im.actor.server.push.SeqUpdatesManager._
import im.actor.server.{ models, persist }

class GroupsServiceImpl(seqUpdManagerRegion: ActorRef)(
  implicit val db: Database, val actorSystem: ActorSystem
  ) extends GroupsService {
  override implicit val ec: ExecutionContext = actorSystem.dispatcher

  override def jhandleEditGroupAvatar(groupPeer: GroupOutPeer, randomId: Long, fileLocation: FileLocation, clientData: ClientData): Future[HandlerResult[ResponseEditGroupAvatar]] = ???

  override def jhandleKickUser(groupPeer: GroupOutPeer, randomId: Long, user: UserOutPeer, clientData: ClientData): Future[HandlerResult[ResponseSeqDate]] = ???

  override def jhandleLeaveGroup(groupPeer: GroupOutPeer, randomId: Long, clientData: ClientData): Future[HandlerResult[ResponseSeqDate]] = ???

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
          _ <- persist.GroupUser.create(group.id, userIds, client.userId, dateTime)
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

  override def jhandleRemoveGroupAvatar(groupPeer: GroupOutPeer, randomId: Long, clientData: ClientData): Future[HandlerResult[ResponseSeqDate]] = ???

  override def jhandleInviteUser(groupPeer: GroupOutPeer, randomId: Long, user: UserOutPeer, clientData: ClientData): Future[HandlerResult[ResponseSeqDate]] = ???

  override def jhandleEditGroupTitle(groupPeer: GroupOutPeer, randomId: Long, title: String, clientData: ClientData): Future[HandlerResult[ResponseSeqDate]] = ???
}
