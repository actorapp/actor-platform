package im.actor.server.api.util

import scala.concurrent.ExecutionContext

import slick.dbio.Effect.Read
import slick.dbio.{ DBIO, DBIOAction, NoStream }

import im.actor.api.rpc.AuthorizedClientData
import im.actor.api.rpc.groups.{ Group, Member }
import im.actor.server.persist

object GroupUtils {
  import AvatarUtils._

  def getGroupStructOption(groupId: Int)(implicit clientData: AuthorizedClientData, ec: ExecutionContext): DBIOAction[Option[Group], NoStream, Read with Read] = {
    persist.Group.find(groupId).headOption flatMap {
      case Some(group) ⇒
        for {
          groupUsers ← persist.GroupUser.find(groupId)
          groupAvatarModelOpt ← persist.AvatarData.findByGroupId(groupId)
        } yield {
          val (userIds, members) = groupUsers.foldLeft(Vector.empty[Int], Vector.empty[Member]) {
            case ((userIdsAcc, membersAcc), groupUser) ⇒
              val member = Member(groupUser.userId, groupUser.inviterUserId, groupUser.invitedAt.getMillis)

              (userIdsAcc :+ groupUser.userId, membersAcc :+ member)
          }

          val isMember = userIds.contains(clientData.userId)

          Some(Group(group.id, group.accessHash, group.title, groupAvatarModelOpt map getAvatar, isMember, group.creatorUserId, members, group.createdAt.getMillis))
        }
      case None ⇒ DBIO.successful(None)
    }
  }

  // TODO: #perf eliminate lots of sql queries
  def getGroupsStructs(groupIds: Set[Int])(implicit clientData: AuthorizedClientData, ec: ExecutionContext): DBIOAction[Seq[Group], NoStream, Read with Read] = {
    DBIO.sequence(groupIds.toSeq map getGroupStructOption) map (_.flatten)
  }
}