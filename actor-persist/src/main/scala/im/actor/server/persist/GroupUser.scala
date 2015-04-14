package im.actor.server.persist

import com.github.tototoshi.slick.PostgresJodaSupport._
import org.joda.time.DateTime
import slick.driver.PostgresDriver.api._

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

  def findUserIds(groupId: Int) =
    groupUsers.filter(g => g.groupId === groupId).map(_.userId).result
}
