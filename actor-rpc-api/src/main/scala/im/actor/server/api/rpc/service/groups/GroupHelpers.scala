package im.actor.server.api.rpc.service.groups

import scala.concurrent.ExecutionContext
import scalaz.\/

import org.joda.time.DateTime
import slick.dbio.DBIO

import im.actor.api.rpc.Implicits._
import im.actor.api.rpc.groups._
import im.actor.api.rpc.{ AuthorizedClientData, Error, RpcError, RpcResponse }
import im.actor.server.push.SeqUpdatesManager._
import im.actor.server.push.SeqUpdatesManagerRegion
import im.actor.server.util.{ GroupServiceMessages, HistoryUtils }
import im.actor.server.{ models, persist }

object GroupHelpers {

  def handleInvite[R <: RpcResponse](fullGroup: models.FullGroup, inviteeId: Int, randomId: Long)(
    f: ((Int, Array[Byte]), Long) ⇒ \/[RpcError, R]
  )(
    implicit
    seqUpdManagerRegion: SeqUpdatesManagerRegion,
    ec:                  ExecutionContext,
    clientData:          AuthorizedClientData
  ) = {
    persist.GroupUser.find(fullGroup.id).flatMap { groupUsers ⇒
      val userIds = groupUsers.map(_.userId)

      if (!userIds.contains(inviteeId)) {
        val date = new DateTime
        val dateMillis = date.getMillis

        val newGroupMembers = groupUsers.map(_.toMember) :+ Member(inviteeId, clientData.userId, dateMillis)

        val inviteeUserUpdates = Seq(
          UpdateGroupInvite(groupId = fullGroup.id, randomId = randomId, inviteUserId = clientData.userId, date = dateMillis),
          UpdateGroupTitleChanged(groupId = fullGroup.id, randomId = fullGroup.titleChangeRandomId, userId = fullGroup.titleChangerUserId, title = fullGroup.title, date = dateMillis),
          // TODO: put avatar here
          UpdateGroupAvatarChanged(groupId = fullGroup.id, randomId = fullGroup.avatarChangeRandomId, userId = fullGroup.avatarChangerUserId, avatar = None, date = dateMillis),
          UpdateGroupMembersUpdate(groupId = fullGroup.id, members = newGroupMembers.toVector)
        )

        val userAddedUpdate = UpdateGroupUserInvited(groupId = fullGroup.id, userId = inviteeId, inviterUserId = clientData.userId, date = dateMillis, randomId = randomId)
        val serviceMessage = GroupServiceMessages.userInvited(inviteeId)

        for {
          _ ← persist.GroupUser.create(fullGroup.id, inviteeId, clientData.userId, date, None)

          _ ← DBIO.sequence(inviteeUserUpdates map (broadcastUserUpdate(inviteeId, _, Some(PushTexts.Invited))))
          // TODO: #perf the following broadcasts do update serializing per each user
          _ ← DBIO.sequence(userIds.filterNot(_ == clientData.userId).map(broadcastUserUpdate(_, userAddedUpdate, Some(PushTexts.Added)))) // use broadcastUsersUpdate maybe?
          seqstate ← broadcastClientUpdate(userAddedUpdate, None)
          _ ← HistoryUtils.writeHistoryMessage(
            models.Peer.privat(clientData.userId),
            models.Peer.group(fullGroup.id),
            date,
            randomId,
            serviceMessage.header,
            serviceMessage.toByteArray
          )
        } yield f(seqstate, dateMillis)
      } else {
        DBIO.successful(Error(GroupErrors.UserAlreadyInvited))
      }
    }
  }
}
