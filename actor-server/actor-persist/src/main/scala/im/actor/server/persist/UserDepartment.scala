package im.actor.server.persist

import im.actor.server.db.ActorPostgresDriver.api._
import im.actor.server.models

class UserDepartmentTable(tag: Tag) extends Table[models.UserDepartment](tag, "user_department") {
  def userId = column[Int]("user_id", O.PrimaryKey)
  def departmentId = column[Int]("department_id", O.PrimaryKey)

  def * = (userId, departmentId) <> (models.UserDepartment.tupled, models.UserDepartment.unapply)
}

object UserDepartment {

  val userDepartments = TableQuery[UserDepartmentTable]

  def create(userId: Int, departmentId: Int) =
    userDepartments += models.UserDepartment(userId, departmentId)

  def userIdsByDepartmentId(deptId: Int) =
    userDepartments.filter { _.departmentId === deptId }.map { _.userId }.result

}