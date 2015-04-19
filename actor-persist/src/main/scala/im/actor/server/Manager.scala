package im.actor.server

import slick.driver.PostgresDriver.api._

class ManagerTable(tag: Tag) extends Table[models.Manager](tag, "managers") {
  def id = column[Int]("id", O.PrimaryKey)
  def domain = column[String]("domain")
  def authToken = column[String]("auth_token")

  def * = (id, domain, authToken) <>(models.Manager.tupled, models.Manager.unapply)
}

object Manager {

}