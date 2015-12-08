package im.actor.server.persist

import slick.driver.PostgresDriver.api._

import im.actor.server.model

final class ManagerTable(tag: Tag) extends Table[model.Manager](tag, "managers") {
  def id = column[Int]("id", O.PrimaryKey)
  def name = column[String]("name")
  def lastName = column[String]("last_name")
  def domain = column[String]("domain")
  def authToken = column[String]("auth_token")
  def email = column[String]("email")
  def emailUnique = index("manager_email_idx", email, unique = true) //way to keep email unique

  def * = (id, name, lastName, domain, authToken, email) <> (model.Manager.tupled, model.Manager.unapply)
}

object ManagerRepo {
  val managers = TableQuery[ManagerTable]

  def create(manager: model.Manager) =
    managers += manager

  def findByEmail(email: String) =
    managers.filter(_.email === email).result.headOption

}