package im.actor.server.persist.webrtc

import im.actor.server.db.ActorPostgresDriver.api._
import im.actor.server.webrtc.WebrtcCall

final class WebrtcCallTable(tag: Tag) extends Table[WebrtcCall](tag, "webrtc_calls") {
  def id = column[Long]("id", O.PrimaryKey)

  def initiatorUserId = column[Int]("initiator_user_id")

  def receiverUserId = column[Int]("receiver_user_id")

  def * = (id, initiatorUserId, receiverUserId) <> ((WebrtcCall.apply _).tupled, WebrtcCall.unapply)
}

object WebrtcCallRepo {
  val webrtcCalls = TableQuery[WebrtcCallTable]

  val byPKC = Compiled { id: Rep[Long] ⇒
    webrtcCalls filter (_.id === id)
  }

  def create(call: WebrtcCall) = webrtcCalls += call

  def find(id: Long) = byPKC(id).result.headOption

  def delete(id: Long) = byPKC(id).delete
}