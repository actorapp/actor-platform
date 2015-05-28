package im.actor.server.persist

import com.github.tototoshi.slick.PostgresJodaSupport._
import org.joda.time.DateTime
import slick.dbio.Effect.Write
import slick.driver.PostgresDriver.api._
import slick.profile.FixedSqlAction

import im.actor.server.models

class GroupUsersTable(tag: Tag) extends Table[models.GroupUser](tag, "group_users") {
  def groupId = column[Int]("group_id", O.PrimaryKey)

  def userId = column[Int]("user_id", O.PrimaryKey)

  def inviterUserId = column[Int]("inviter_user_id")

  def invitedAt = column[DateTime]("invited_at")

  def * = (groupId, userId, inviterUserId, invitedAt) <> (models.GroupUser.tupled, models.GroupUser.unapply)
}

object GroupUser {
  val groupUsers = TableQuery[GroupUsersTable]

  def create(groupId: Int, userId: Int, inviterUserId: Int, invitedAt: DateTime) =
    groupUsers += models.GroupUser(groupId, userId, inviterUserId, invitedAt)

  def create(groupId: Int, userIds: Set[Int], inviterUserId: Int, invitedAt: DateTime) =
    groupUsers ++= userIds.map(models.GroupUser(groupId, _, inviterUserId, invitedAt))

  def find(groupId: Int) =
    groupUsers.filter(g ⇒ g.groupId === groupId).result

  def find(groupId: Int, userId: Int) =
    groupUsers.filter(g ⇒ g.groupId === groupId && g.userId === userId).result.headOption

  def findByUserId(userId: Int) =
    groupUsers.filter(_.userId === userId).result

  def findUserIds(groupId: Int) =
    groupUsers.filter(g ⇒ g.groupId === groupId).map(_.userId).result

  def findUserIds(groupIds: Set[Int]) =
    groupUsers.filter(_.groupId inSet groupIds).map(_.userId).result

  def delete(groupId: Int, userId: Int): FixedSqlAction[Int, NoStream, Write] =
    groupUsers.filter(g ⇒ g.groupId === groupId && g.userId === userId).delete
}
