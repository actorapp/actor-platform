package im.actor.server.persist

import im.actor.server.model.SessionInfo
import slick.driver.PostgresDriver.api._

final class SessionInfoTable(tag: Tag) extends Table[SessionInfo](tag, "session_infos") {
  def authId = column[Long]("auth_id", O.PrimaryKey)

  def sessionId = column[Long]("session_id", O.PrimaryKey)

  def optUserId = column[Option[Int]]("user_id")

  def * = (authId, sessionId, optUserId) <> (SessionInfo.tupled, SessionInfo.unapply)
}

object SessionInfoRepo {
  private val infos = TableQuery[SessionInfoTable]

  def create(authId: Long, sessionId: Long, optUserId: Option[Int]) =
    infos += SessionInfo(authId, sessionId, optUserId)

  def create(info: SessionInfo) =
    infos += info

  def createOrUpdate(info: SessionInfo) =
    infos.insertOrUpdate(info)

  def find(authId: Long, sessionId: Long) =
    infos.filter(i ⇒ i.authId === authId && i.sessionId === sessionId).result.headOption

  def updateUserId(authId: Long, sessionId: Long, optUserId: Option[Int]) =
    infos.filter(i ⇒ i.authId === authId && i.sessionId === sessionId).map(_.optUserId).update(optUserId)
}