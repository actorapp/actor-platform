package im.actor.server.persist.ilectro

import java.util.UUID

import slick.driver.PostgresDriver.api._

import im.actor.server.models

class ILectroUserTable(tag: Tag) extends Table[models.ilectro.ILectroUser](tag, "ilectro_users") {
  def userId = column[Int]("user_id", O.PrimaryKey)
  def uuid = column[UUID]("uuid")
  def name = column[String]("name")

  def * = (userId, uuid, name) <> (models.ilectro.ILectroUser.tupled, models.ilectro.ILectroUser.unapply)
}

object ILectroUser {
  val users = TableQuery[ILectroUserTable]

  def findByUserId(userId: Int) =
    users.filter(_.userId === userId).result.headOption

  def findByUuid(uuid: UUID) =
    users.filter(_.uuid === uuid).result.headOption
}
