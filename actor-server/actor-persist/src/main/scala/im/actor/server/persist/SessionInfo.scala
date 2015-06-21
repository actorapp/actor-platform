package im.actor.server.persist

import slick.driver.PostgresDriver.api._

import im.actor.server.models

class SessionInfoTable(tag: Tag) extends Table[models.SessionInfo](tag, "session_infos") {
  def authId = column[Long]("auth_id", O.PrimaryKey)

  def sessionId = column[Long]("session_id", O.PrimaryKey)

  def optUserId = column[Option[Int]]("user_id")

  def * = (authId, sessionId, optUserId) <> (models.SessionInfo.tupled, models.SessionInfo.unapply)
}

object SessionInfo {
  val infos = TableQuery[SessionInfoTable]

  def create(authId: Long, sessionId: Long, optUserId: Option[Int]) =
    infos += models.SessionInfo(authId, sessionId, optUserId)

  def create(info: models.SessionInfo) =
    infos += info

  def createOrUpdate(info: models.SessionInfo) =
    infos.insertOrUpdate(info)

  def find(authId: Long, sessionId: Long) =
    infos.filter(i ⇒ i.authId === authId && i.sessionId === sessionId).result.headOption

  def updateUserId(authId: Long, sessionId: Long, optUserId: Option[Int]) =
    infos.filter(i ⇒ i.authId === authId && i.sessionId === sessionId).map(_.optUserId).update(optUserId)
}