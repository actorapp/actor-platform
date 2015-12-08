package im.actor.server.persist

import slick.driver.PostgresDriver.api._

import im.actor.server.model

final class SessionInfoTable(tag: Tag) extends Table[model.SessionInfo](tag, "session_infos") {
  def authId = column[Long]("auth_id", O.PrimaryKey)

  def sessionId = column[Long]("session_id", O.PrimaryKey)

  def optUserId = column[Option[Int]]("user_id")

  def * = (authId, sessionId, optUserId) <> (model.SessionInfo.tupled, model.SessionInfo.unapply)
}

object SessionInfoRepo {
  val infos = TableQuery[SessionInfoTable]

  def create(authId: Long, sessionId: Long, optUserId: Option[Int]) =
    infos += model.SessionInfo(authId, sessionId, optUserId)

  def create(info: model.SessionInfo) =
    infos += info

  def createOrUpdate(info: model.SessionInfo) =
    infos.insertOrUpdate(info)

  def find(authId: Long, sessionId: Long) =
    infos.filter(i ⇒ i.authId === authId && i.sessionId === sessionId).result.headOption

  def updateUserId(authId: Long, sessionId: Long, optUserId: Option[Int]) =
    infos.filter(i ⇒ i.authId === authId && i.sessionId === sessionId).map(_.optUserId).update(optUserId)
}