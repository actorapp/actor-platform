package im.actor.server.persist

import java.time.LocalDateTime

import im.actor.server.db.ActorPostgresDriver.api._
import im.actor.server.models

class DashboardSessionTable(tag: Tag) extends Table[models.DashboardSession](tag, "dashboard_sessions") {
  def id = column[Long]("id", O.PrimaryKey)
  def userId = column[Int]("user_id")
  def passcode = column[String]("passcode")
  def authToken = column[String]("auth_token")
  def isActive = column[Boolean]("is_active")
  def createdAt = column[LocalDateTime]("created_at")

  def * = (id, userId, passcode, authToken, isActive, createdAt) <> (models.DashboardSession.tupled, models.DashboardSession.unapply)
}

object DashboardSession {
  val sessions = TableQuery[DashboardSessionTable]

  val active = sessions.filter(_.isActive)

  def create(session: models.DashboardSession) =
    sessions += session

  def findActiveByUserId(userId: Int) =
    active.filter(_.userId === userId).result.headOption

  def findByUserId(userId: Int) =
    sessions.filter(_.userId === userId).result.headOption

  def markActive(id: Long) =
    sessions.filter(_.id === id).map(_.isActive).update(true)

  def markInactive(id: Long) =
    sessions.filter(_.id === id).map(_.isActive).update(false)

  def exists(token: String) =
    active.filter(_.authToken === token).exists.result

}
