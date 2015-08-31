package im.actor.server.group

import im.actor.api.rpc.AuthorizedClientData
import im.actor.api.rpc.pubgroups.PublicGroup
import im.actor.server.file.ImageUtils
import im.actor.server.{ models, persist }
import slick.dbio.Effect.Read
import slick.dbio.{ DBIO, DBIOAction, NoStream }

import scala.concurrent.ExecutionContext

object GroupUtils {

  import ImageUtils._

  def getPubgroupStructUnsafe(group: models.Group, senderUserId: Int)(implicit ec: ExecutionContext): DBIOAction[PublicGroup, NoStream, Read with Read] = {
    for {
      membersIds ← persist.GroupUser.findUserIds(group.id)
      userContactsIds ← persist.contact.UserContact.findNotDeletedIds(senderUserId)
      friendsCount = (membersIds intersect userContactsIds).length
      groupAvatarModelOpt ← persist.AvatarData.findByGroupId(group.id)
    } yield {
      PublicGroup(group.id, group.accessHash, group.title, membersIds.length, friendsCount, group.about.getOrElse(""), groupAvatarModelOpt map getAvatar)
    }
  }

  def getPubgroupStructUnsafe(group: models.Group)(implicit clientData: AuthorizedClientData, ec: ExecutionContext): DBIOAction[PublicGroup, NoStream, Read with Read] = {
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
}