package im.actor.server.persist

import im.actor.server.db.ActorPostgresDriver.api._
import im.actor.server.model.ClientStats

final class ClientStatsTable(tag: Tag) extends Table[ClientStats](tag, "client_stats") {
  def id = column[Long]("id", O.PrimaryKey)
  def userId = column[Int]("user_id")
  def authId = column[Long]("auth_id")
  def eventType = column[String]("event_type")
  def eventData = column[String]("event_data")

  def * = (id, userId, authId, eventType, eventData) <> (ClientStats.tupled, ClientStats.unapply)
}

object ClientStatsRepo {
  val stats = TableQuery[ClientStatsTable]

  private def byUserId(userId: Rep[Int]) = stats.filter(_.userId === userId)
  private def byAuthId(authId: Rep[Long]) = stats.filter(_.authId === authId)

  private val byUserIdC = Compiled(byUserId _)
  private val byAuthIdC = Compiled(byAuthId _)

  def create(s: Seq[ClientStats]) = stats ++= s

  def findByUserId(userId: Int): DBIO[Seq[ClientStats]] = byUserIdC(userId).result
  def findByAuthId(authId: Long): DBIO[Seq[ClientStats]] = byAuthIdC(authId).result
}
