package im.actor.server.persist.llectro

import java.util.UUID

import slick.driver.PostgresDriver.api._

import im.actor.server.models

class LlectroUserTable(tag: Tag) extends Table[models.llectro.LlectroUser](tag, "llectro_users") {
  def userId = column[Int]("user_id", O.PrimaryKey)
  def uuid = column[UUID]("uuid")
  def name = column[String]("name")

  def * = (userId, uuid, name) <> (models.llectro.LlectroUser.tupled, models.llectro.LlectroUser.unapply)
}

object LlectroUser {
  val users = TableQuery[LlectroUserTable]

  def create(user: models.llectro.LlectroUser) =
    users += user

  def findByUserId(userId: Int) =
    users.filter(_.userId === userId).result.headOption

  def findByUuid(uuid: UUID) =
    users.filter(_.uuid === uuid).result.headOption

  def findIds() =
    users.map(_.userId).result
}
