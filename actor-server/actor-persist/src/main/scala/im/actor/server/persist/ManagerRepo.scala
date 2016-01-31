package im.actor.server.persist

import im.actor.server.model.Manager
import slick.driver.PostgresDriver.api._

final class ManagerTable(tag: Tag) extends Table[Manager](tag, "managers") {
  def id = column[Int]("id", O.PrimaryKey)
  def name = column[String]("name")
  def lastName = column[String]("last_name")
  def domain = column[String]("domain")
  def authToken = column[String]("auth_token")
  def email = column[String]("email")
  def emailUnique = index("manager_email_idx", email, unique = true) //way to keep email unique

  def * = (id, name, lastName, domain, authToken, email) <> (Manager.tupled, Manager.unapply)
}

object ManagerRepo {
  val managers = TableQuery[ManagerTable]

  def create(manager: Manager) =
    managers += manager

  def findByEmail(email: String) =
    managers.filter(_.email === email).result.headOption

}