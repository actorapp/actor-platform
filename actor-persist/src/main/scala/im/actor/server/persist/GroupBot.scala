package im.actor.server.persist

import slick.driver.PostgresDriver.api._
import slick.lifted.Tag

import im.actor.server.models

class GroupBotTable(tag: Tag) extends Table[models.GroupBot](tag, "groups_bots") {

  def groupId = column[Int]("group_id", O.PrimaryKey)
  def userId = column[Int]("user_id", O.PrimaryKey)
  def token = column[String]("token")
  def tokenUnique = index("bot_token_idx", token, unique = true)

  def * = (groupId, userId, token) <> (models.GroupBot.tupled, models.GroupBot.unapply)
}

object GroupBot {
  val groupBots = TableQuery[GroupBotTable]

  def create(groupId: Int, userId: Int, token: String) =
    groupBots += models.GroupBot(groupId, userId, token)

  def findByToken(token: String) = groupBots.filter(_.token === token).result.headOption

  def findByGroup(groupId: Int) =
    groupBots.filter(b ⇒ b.groupId === groupId).result.headOption

  def find(groupId: Int, botId: Int) =
    groupBots.filter(b ⇒ b.groupId === groupId && b.userId === botId).result.headOption

}