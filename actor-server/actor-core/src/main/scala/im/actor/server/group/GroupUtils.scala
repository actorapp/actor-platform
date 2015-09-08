package im.actor.server.group

import akka.util.Timeout
import im.actor.api.rpc.AuthorizedClientData
import im.actor.api.rpc.groups.ApiGroup
import im.actor.api.rpc.pubgroups.ApiPublicGroup
import im.actor.api.rpc.users.ApiUser
import im.actor.server.file.ImageUtils
import im.actor.server.user.{ UserViewRegion, UserOffice }
import im.actor.server.{ models, persist }
import slick.dbio.Effect.Read
import slick.dbio.{ DBIO, DBIOAction, NoStream }

import scala.concurrent.{ Future, ExecutionContext }

object GroupUtils {

  import ImageUtils._

  def getPubgroupStructUnsafe(group: models.Group, senderUserId: Int)(implicit ec: ExecutionContext): DBIOAction[ApiPublicGroup, NoStream, Read with Read] = {
    for {
      membersIds ← persist.GroupUser.findUserIds(group.id)
      userContactsIds ← persist.contact.UserContact.findNotDeletedIds(senderUserId)
      friendsCount = (membersIds intersect userContactsIds).length
      groupAvatarModelOpt ← persist.AvatarData.findByGroupId(group.id)
    } yield {
      ApiPublicGroup(group.id, group.accessHash, group.title, membersIds.length, friendsCount, group.about.getOrElse(""), groupAvatarModelOpt map getAvatar)
    }
  }

  def getPubgroupStructUnsafe(group: models.Group)(implicit clientData: AuthorizedClientData, ec: ExecutionContext): DBIOAction[ApiPublicGroup, NoStream, Read with Read] = {
    getPubgroupStructUnsafe(group, clientData.userId)
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

  def getUserIds(groups: Seq[ApiGroup]): Seq[Int] =
    groups.map(g ⇒ g.members.map(m ⇒ Seq(m.userId, m.inviterUserId)).flatten :+ g.creatorUserId).flatten

  def getGroupsUsers(groupIds: Seq[Int], userIds: Seq[Int], clientUserId: Int, clientAuthId: Long)(
    implicit
    ec:              ExecutionContext,
    timeout:         Timeout,
    userViewRegion:  UserViewRegion,
    groupViewRegion: GroupViewRegion
  ): Future[(Seq[ApiGroup], Seq[ApiUser])] = {
    for {
      groups ← Future.sequence(groupIds map (GroupOffice.getApiStruct(_, clientUserId)))
      memberIds = getUserIds(groups)
      users ← Future.sequence((userIds.toSet ++ memberIds.toSet) map (UserOffice.getApiStruct(_, clientUserId, clientAuthId)))
    } yield (groups, users.toSeq)
  }
}