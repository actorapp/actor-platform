package im.actor.server.persist

import slick.dbio.Effect.{ Read, Write }
import slick.driver.PostgresDriver.api._
import slick.profile.{ FixedSqlAction, SqlAction }

import im.actor.server.model

final class GroupBotTable(tag: Tag) extends Table[model.GroupBot](tag, "groups_bots") {

  def groupId = column[Int]("group_id", O.PrimaryKey)
  def userId = column[Int]("user_id", O.PrimaryKey)
  def token = column[String]("token")
  def tokenUnique = index("bot_token_idx", token, unique = true)

  def * = (groupId, userId, token) <> (model.GroupBot.tupled, model.GroupBot.unapply)
}

object GroupBotRepo {
  val groupBots = TableQuery[GroupBotTable]

  def create(groupId: Int, userId: Int, token: String): FixedSqlAction[Int, NoStream, Write] =
    groupBots += model.GroupBot(groupId, userId, token)

  def findByToken(token: String): SqlAction[Option[model.GroupBot], NoStream, Read] =
    groupBots.filter(_.token === token).result.headOption

  def findByGroup(groupId: Int): SqlAction[Option[model.GroupBot], NoStream, Read] =
    groupBots.filter(b ⇒ b.groupId === groupId).result.headOption

  def find(groupId: Int, botId: Int): SqlAction[Option[model.GroupBot], NoStream, Read] =
    groupBots.filter(b ⇒ b.groupId === groupId && b.userId === botId).result.headOption

  def updateToken(groupId: Int, newToken: String): FixedSqlAction[Int, NoStream, Write] =
    groupBots.filter(b ⇒ b.groupId === groupId).map(_.token).update(newToken)

}