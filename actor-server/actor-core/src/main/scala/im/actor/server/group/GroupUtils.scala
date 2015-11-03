package im.actor.server.group

import akka.actor.ActorSystem
import im.actor.api.rpc.AuthorizedClientData
import im.actor.api.rpc.groups.ApiGroup
import im.actor.api.rpc.pubgroups.ApiPublicGroup
import im.actor.api.rpc.users.ApiUser
import im.actor.server.file.ImageUtils
import im.actor.server.user.UserUtils
import im.actor.server.{ ApiConversions, model, persist }
import slick.dbio.Effect.Read
import slick.dbio.{ DBIO, DBIOAction, NoStream }

import scala.concurrent.{ Future, ExecutionContext }

object GroupUtils {

  import ApiConversions._
  import ImageUtils._

  def getPubgroupStructUnsafe(group: model.Group, senderUserId: Int)(implicit ec: ExecutionContext): DBIOAction[ApiPublicGroup, NoStream, Read with Read] = {
    for {
      membersIds ← persist.GroupUserRepo.findUserIds(group.id)
      userContactsIds ← persist.contact.UserContactRepo.findNotDeletedIds(senderUserId)
      friendsCount = (membersIds intersect userContactsIds).length
      groupAvatarModelOpt ← persist.AvatarDataRepo.findByGroupId(group.id)
    } yield {
      ApiPublicGroup(group.id, group.accessHash, group.title, membersIds.length, friendsCount, group.about.getOrElse(""), groupAvatarModelOpt map getAvatar)
    }
  }

  def getPubgroupStructUnsafe(group: model.Group)(implicit clientData: AuthorizedClientData, ec: ExecutionContext): DBIOAction[ApiPublicGroup, NoStream, Read with Read] = {
    getPubgroupStructUnsafe(group, clientData.userId)
  }

  def withGroup[A](groupId: Int)(f: model.Group ⇒ DBIO[A])(implicit ec: ExecutionContext): DBIO[A] = {
    persist.GroupRepo.find(groupId) flatMap {
      case Some(group) ⇒ f(group)
      case None        ⇒ DBIO.failed(new Exception(s"Group $groupId not found"))
    }
  }

  //todo: use GroupExtension.getMembers instead
  def withGroupUserIds[A](groupId: Int)(f: Seq[Int] ⇒ DBIO[A])(implicit ec: ExecutionContext): DBIO[A] = {
    persist.GroupUserRepo.findUserIds(groupId) flatMap f
  }

  def getUserIds(group: ApiGroup): Set[Int] =
    group.members.flatMap(m ⇒ Seq(m.userId, m.inviterUserId)).toSet + group.creatorUserId

  def getUserIds(groups: Seq[ApiGroup]): Set[Int] =
    groups.foldLeft(Set.empty[Int])(_ ++ getUserIds(_))

  def getGroupsUsers(groupIds: Seq[Int], userIds: Seq[Int], clientUserId: Int, clientAuthId: Long)(implicit system: ActorSystem): Future[(Seq[ApiGroup], Seq[ApiUser])] = {
    import system.dispatcher
    for {
      groups ← Future.sequence(groupIds map (GroupExtension(system).getApiStruct(_, clientUserId)))
      memberIds = getUserIds(groups)
      users ← Future.sequence((userIds.toSet ++ memberIds.toSet).filterNot(_ == 0) map (UserUtils.safeGetUser(_, clientUserId, clientAuthId))) map (_.flatten)
    } yield (groups, users.toSeq)
  }
}