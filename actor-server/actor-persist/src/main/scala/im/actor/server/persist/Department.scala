package im.actor.server.persist

import com.github.tminglei.slickpg.LTree
import com.github.tototoshi.slick.PostgresJodaSupport._
import org.joda.time.DateTime

import im.actor.server._
import im.actor.server.db.ActorPostgresDriver.api._

class DepartmentTable(tag: Tag) extends Table[models.Department](tag, "departments") {
  def id = column[Int]("id", O.PrimaryKey)
  def name = column[String]("name")
  def struct = column[LTree]("struct")
  def deletedAt = column[Option[DateTime]]("deleted_at")
  def structUnique = index("department_struct_idx", struct, unique = true)

  def * = (id, name, struct, deletedAt) <> (models.Department.tupled, models.Department.unapply)
}

object Department {

  val departments = TableQuery[DepartmentTable]

  def create(department: models.Department) =
    departments += department

  def find(struct: String) =
    departments.filter(_.struct === LTree(struct)).result

  def setName(struct: String, name: String) =
    departments.filter(_.struct === LTree(struct)).map(_.name).update(name)

  def setDeletedAt(struct: String) =
    departments.filter(_.struct === LTree(struct)).map(_.deletedAt).update(Some(new DateTime))

  def deptAndChildren(struct: String) = {
    departments.
      filter { e ⇒ LTree(struct).bind @> e.struct }.
      sortBy { _.struct }.
      result
  }

}