package im.actor.server.util

import scala.concurrent.ExecutionContext

import slick.dbio.Effect.Read
import slick.dbio.{ DBIO, DBIOAction, NoStream }

import im.actor.api.rpc.AuthorizedClientData
import im.actor.api.rpc.groups.{ Group, Member }
import im.actor.api.rpc.pubgroups.PublicGroup
import im.actor.server.{ models, persist }

object GroupUtils {

  import ImageUtils._

  private def getGroupStructOption(groupId: Int, senderUserId: Int)(implicit ec: ExecutionContext): DBIOAction[Option[Group], NoStream, Read with Read] = {
    persist.Group.find(groupId) flatMap {
      case Some(group) ⇒ getGroupStructUnsafe(group, senderUserId).map(Some(_))
      case None        ⇒ DBIO.successful(None)
    }
  }

  private def getGroupStructOption(groupId: Int)(implicit clientData: AuthorizedClientData, ec: ExecutionContext): DBIOAction[Option[Group], NoStream, Read with Read] = {
    getGroupStructOption(groupId, clientData.userId)
  }

  def getGroupStructUnsafe(group: models.Group, senderUserId: Int)(implicit ec: ExecutionContext): DBIOAction[Group, NoStream, Read with Read] = {
    for {
      groupUsers ← persist.GroupUser.find(group.id)
      isMember ← DBIO.successful(groupUsers.map(_.userId).contains(senderUserId))
      groupAvatarModelOpt ← persist.AvatarData.findByGroupId(group.id)
    } yield {
      val (userIds, members) =
        if (isMember) {
          groupUsers.foldLeft(Vector.empty[Int], Vector.empty[Member]) {
            case ((userIdsAcc, membersAcc), groupUser) ⇒
              val member = Member(groupUser.userId, groupUser.inviterUserId, groupUser.invitedAt.getMillis)

              (userIdsAcc :+ groupUser.userId, membersAcc :+ member)
          }
        } else (Vector.empty[Int], Vector.empty[Member])

      Group(group.id, group.accessHash, group.title, groupAvatarModelOpt map getAvatar, isMember, group.creatorUserId, members, group.createdAt.getMillis)
    }
  }

  def getPubgroupStructUnsafe(group: models.Group, senderUserId: Int)(implicit ec: ExecutionContext): DBIOAction[PublicGroup, NoStream, Read with Read] = {
    for {
      membersIds ← persist.GroupUser.findUserIds(group.id)
      userContactsIds ← persist.contact.UserContact.findNotDeletedIds(senderUserId)
      friendsCount = (membersIds intersect userContactsIds).length
      groupAvatarModelOpt ← persist.AvatarData.findByGroupId(group.id)
    } yield {
      PublicGroup(group.id, group.accessHash, group.title, membersIds.length, friendsCount, group.description, groupAvatarModelOpt map getAvatar)
    }
  }

  def getGroupStructUnsafe(group: models.Group)(implicit clientData: AuthorizedClientData, ec: ExecutionContext): DBIOAction[Group, NoStream, Read with Read] = {
    getGroupStructUnsafe(group, clientData.userId)
  }

  def getPubgroupStructUnsafe(group: models.Group)(implicit clientData: AuthorizedClientData, ec: ExecutionContext): DBIOAction[PublicGroup, NoStream, Read with Read] = {
    getPubgroupStructUnsafe(group, clientData.userId)
  }

  // TODO: #perf eliminate lots of sql queries
  def getGroupsStructs(groupIds: Set[Int])(implicit clientData: AuthorizedClientData, ec: ExecutionContext): DBIOAction[Seq[Group], NoStream, Read with Read] = {
    DBIO.sequence(groupIds.toSeq map getGroupStructOption) map (_.flatten)
  }

  def getGroupStructs(groupIds: Set[Int], senderUserId: Int)(implicit ec: ExecutionContext) = {
    DBIO.sequence(groupIds.toSeq map (getGroupStructOption(_, senderUserId))) map (_.flatten)
  }

  def withGroup[A](groupId: Int)(f: models.Group ⇒ DBIO[A])(implicit ec: ExecutionContext): DBIO[A] = {
    persist.Group.find(groupId) flatMap {
      case Some(group) ⇒ f(group)
      case None        ⇒ DBIO.failed(new Exception(s"Group ${groupId} not found"))
    }
  }

  def withGroupUserIds[A](groupId: Int)(f: Seq[Int] ⇒ DBIO[A])(implicit ec: ExecutionContext): DBIO[A] = {
    persist.GroupUser.findUserIds(groupId) flatMap f
  }
}