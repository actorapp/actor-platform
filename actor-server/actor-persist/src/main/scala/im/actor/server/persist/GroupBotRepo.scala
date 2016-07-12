package im.actor.server.persist

import im.actor.server.model.GroupBot
import slick.dbio.Effect.{ Read, Write }
import slick.driver.PostgresDriver.api._
import slick.profile.{ FixedSqlAction, SqlAction }

final class GroupBotTable(tag: Tag) extends Table[GroupBot](tag, "groups_bots") {

  def groupId = column[Int]("group_id", O.PrimaryKey)
  def userId = column[Int]("user_id", O.PrimaryKey)
  def token = column[String]("token")
  def tokenUnique = index("bot_token_idx", token, unique = true)

  def * = (groupId, userId, token) <> (GroupBot.tupled, GroupBot.unapply)
}

object GroupBotRepo {
  private val groupBots = TableQuery[GroupBotTable]

  @deprecated("Duplication of event-sourced groups logic", "2016-06-05")
  def create(groupId: Int, userId: Int, token: String): FixedSqlAction[Int, NoStream, Write] =
    groupBots += GroupBot(groupId, userId, token)

  @deprecated("Used for migrations only", "2016-06-05")
  def findByGroup(groupId: Int): SqlAction[Option[GroupBot], NoStream, Read] =
    groupBots.filter(b ⇒ b.groupId === groupId).result.headOption

  @deprecated("Duplication of event-sourced groups logic", "2016-06-05")
  def updateToken(groupId: Int, newToken: String): FixedSqlAction[Int, NoStream, Write] =
    groupBots.filter(b ⇒ b.groupId === groupId).map(_.token).update(newToken)

}
