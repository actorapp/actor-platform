package im.actor.server.persist

import java.time.{ LocalDateTime, ZonedDateTime }

import com.github.tototoshi.slick.PostgresJodaSupport._
import org.joda.time.DateTime
import slick.dbio.Effect.Write
import slick.profile.FixedSqlAction

import im.actor.server.models
import im.actor.server.db.ActorPostgresDriver.api._

class GroupUsersTable(tag: Tag) extends Table[models.GroupUser](tag, "group_users") {
  def groupId = column[Int]("group_id", O.PrimaryKey)

  def userId = column[Int]("user_id", O.PrimaryKey)

  def inviterUserId = column[Int]("inviter_user_id")

  def invitedAt = column[DateTime]("invited_at")

  def joinedAt = column[Option[LocalDateTime]]("joined_at")

  def * = (groupId, userId, inviterUserId, invitedAt, joinedAt) <> (models.GroupUser.tupled, models.GroupUser.unapply)
}

object GroupUser {

  val groupUsers = TableQuery[GroupUsersTable]

  def create(groupId: Int, userId: Int, inviterUserId: Int, invitedAt: DateTime, joinedAt: Option[LocalDateTime]) =
    groupUsers += models.GroupUser(groupId, userId, inviterUserId, invitedAt, joinedAt)

  def create(groupId: Int, userIds: Set[Int], inviterUserId: Int, invitedAt: DateTime, joinedAt: Option[LocalDateTime]) =
    groupUsers ++= userIds.map(models.GroupUser(groupId, _, inviterUserId, invitedAt, joinedAt))

  def find(groupId: Int) =
    groupUsers.filter(g ⇒ g.groupId === groupId).result

  def find(groupId: Int, userId: Int) =
    groupUsers.filter(g ⇒ g.groupId === groupId && g.userId === userId).result.headOption

  def isJoined(groupId: Int, userId: Int) =
    groupUsers.filter(g ⇒ g.groupId === groupId && g.userId === userId).map(_.joinedAt.isDefined).result.headOption

  def findByUserId(userId: Int) =
    groupUsers.filter(_.userId === userId).result

  def findUserIds(groupId: Int) =
    groupUsers.filter(g ⇒ g.groupId === groupId).map(_.userId).result

  def findUserIds(groupIds: Set[Int]) =
    groupUsers.filter(_.groupId inSet groupIds).map(_.userId).result

  def setJoined(groupId: Int, userId: Int, date: LocalDateTime) =
    groupUsers.filter(g ⇒ g.groupId === groupId && g.userId === userId).map(_.joinedAt).update(Some(date))

  def delete(groupId: Int, userId: Int): FixedSqlAction[Int, NoStream, Write] =
    groupUsers.filter(g ⇒ g.groupId === groupId && g.userId === userId).delete
}
