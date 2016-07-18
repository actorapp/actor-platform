package im.actor.server.persist

import java.time.{ Instant, LocalDateTime, ZonedDateTime }

import im.actor.server.db.ActorPostgresDriver.api._
import im.actor.server.model.GroupUser
import org.joda.time.DateTime
import slick.dbio.Effect.Write
import slick.profile.FixedSqlAction

final class GroupUsersTable(tag: Tag) extends Table[GroupUser](tag, "group_users") {
  def groupId = column[Int]("group_id", O.PrimaryKey)

  def userId = column[Int]("user_id", O.PrimaryKey)

  def inviterUserId = column[Int]("inviter_user_id")

  def invitedAt = column[Instant]("invited_at")

  def joinedAt = column[Option[LocalDateTime]]("joined_at")

  def isAdmin = column[Boolean]("is_admin")

  def * = (groupId, userId, inviterUserId, invitedAt, joinedAt, isAdmin) <> (GroupUser.tupled, GroupUser.unapply)
}

object GroupUserRepo {

  private val groupUsers = TableQuery[GroupUsersTable]
  private val groupUsersC = Compiled(groupUsers)

  private def byPK(groupId: Rep[Int], userId: Rep[Int]) = groupUsers filter (g ⇒ g.groupId === groupId && g.userId === userId)
  private def byGroupId(groupId: Rep[Int]) = groupUsers filter (_.groupId === groupId)
  private def byUserId(userId: Rep[Int]) = groupUsers filter (_.userId === userId)

  private def joinedAtByPK(groupId: Rep[Int], userId: Rep[Int]) = byPK(groupId, userId) map (_.joinedAt)
  private def userIdByGroupId(groupId: Rep[Int]) = byGroupId(groupId) map (_.userId)

  private val byPKC = Compiled(byPK _)
  private val byGroupIdC = Compiled(byGroupId _)
  private val byUserIdC = Compiled(byUserId _)

  private val userIdByGroupIdC = Compiled(userIdByGroupId _)
  private val joinedAtByPKC = Compiled(joinedAtByPK _)

  @deprecated("Duplication of event-sourced groups logic", "2016-06-05")
  def create(groupId: Int, userId: Int, inviterUserId: Int, invitedAt: Instant, joinedAt: Option[LocalDateTime], isAdmin: Boolean): DBIO[Int] = {
    groupUsersC += GroupUser(groupId, userId, inviterUserId, invitedAt, joinedAt, isAdmin)
  }

  @deprecated("Used for migrations only", "2016-06-05")
  def find(groupId: Int) =
    byGroupIdC(groupId).result

  @deprecated("Compatibility with old group API, remove when possible", "2016-06-05")
  def find(groupId: Int, userId: Int) =
    byPKC((groupId, userId)).result.headOption

  //TODO: remove
  def exists(groupId: Int, userId: Int) =
    byPKC.applied((groupId, userId)).exists.result

  //TODO: revisit later
  def findUserIds(groupId: Int) =
    userIdByGroupIdC(groupId).result

  @deprecated("Duplication of event-sourced groups logic", "2016-06-05")
  def delete(groupId: Int, userId: Int): FixedSqlAction[Int, NoStream, Write] =
    byPKC.applied((groupId, userId)).delete

  @deprecated("Duplication of event-sourced groups logic", "2016-06-05")
  def makeAdmin(groupId: Int, userId: Int) =
    byPKC.applied((groupId, userId)).map(_.isAdmin).update(true)

  @deprecated("Duplication of event-sourced groups logic", "2016-06-05")
  def dismissAdmin(groupId: Int, userId: Int) =
    byPKC.applied((groupId, userId)).map(_.isAdmin).update(false)

}
