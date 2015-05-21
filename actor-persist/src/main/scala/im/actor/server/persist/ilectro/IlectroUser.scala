package im.actor.server.persist.ilectro

import slick.driver.PostgresDriver.api._

import im.actor.server.models

class IlectroUserTable(tag: Tag) extends Table[models.ilectro.IlectroUser](tag, "ilectro_users") {
  def userId = column[Int]("user_id", O.PrimaryKey)
  def uuid = column[Int]("uuid")
  def name = column[String]("name")

  def * = (userId, uuid, name) <>(models.ilectro.IlectroUser.tupled, models.ilectro.IlectroUser.unapply)
}

object IlectroUser {
  val users = TableQuery[IlectroUserTable]
}
