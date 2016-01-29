package im.actor.server.persist

import im.actor.server.db.ActorPostgresDriver.api._
import im.actor.server.model.UserDepartment

final class UserDepartmentTable(tag: Tag) extends Table[UserDepartment](tag, "user_department") {
  def userId = column[Int]("user_id", O.PrimaryKey)
  def departmentId = column[Int]("department_id", O.PrimaryKey)

  def * = (userId, departmentId) <> (UserDepartment.tupled, UserDepartment.unapply)
}

object UserDepartmentRepo {

  val userDepartments = TableQuery[UserDepartmentTable]

  def create(userId: Int, departmentId: Int) =
    userDepartments += UserDepartment(userId, departmentId)

  def userIdsByDepartmentId(deptId: Int) =
    userDepartments.filter { _.departmentId === deptId }.map { _.userId }.result

}