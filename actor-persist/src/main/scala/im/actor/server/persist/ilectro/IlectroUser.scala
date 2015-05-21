package im.actor.server.persist.ilectro

import slick.driver.PostgresDriver.api._

import im.actor.server.models

class ILectroUserTable(tag: Tag) extends Table[models.ilectro.ILectroUser](tag, "ilectro_users") {
  def userId = column[Int]("user_id", O.PrimaryKey)
  def uuid = column[String]("uuid")
  def name = column[String]("name")

  def * = (userId, uuid, name) <> (models.ilectro.ILectroUser.tupled, models.ilectro.ILectroUser.unapply)
}

object IlectroUser {
  val users = TableQuery[ILectroUserTable]
}
