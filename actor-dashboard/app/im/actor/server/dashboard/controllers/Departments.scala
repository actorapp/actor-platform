package im.actor.server.dashboard.controllers

import scala.concurrent.Future

import com.github.tminglei.slickpg.LTree
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.mvc.{ BodyParsers, Controller }
import slick.dbio.DBIO

import im.actor.server.dashboard.controllers.utils.DepartmentUtils._
import im.actor.server.dashboard.controllers.utils.JsonConstructors._
import im.actor.server.dashboard.controllers.utils.{ AuthAction, Db, NestedDept }
import im.actor.server.{ models, persist }

class Departments extends Controller {

  protected val db = Db.db

  implicit val ltreeWrites = new Writes[LTree] {
    def writes(tree: LTree) = JsNumber(tree.value.mkString.toInt)
  }

  implicit val departmentWrites: Writes[NestedDept] = (
    (__ \ "id").write[LTree] and
    (__ \ "title").write[String] and
    (__ \ "items").lazyWrite(Writes.traversableWrites[NestedDept](departmentWrites))
  )(unlift(NestedDept.unapply))

  implicit val departmentReads: Reads[models.Department] = (
    (JsPath \ "title").read[String](length) and
    (JsPath \ "struct").read[String](length)
  )(makeDepartment _)

  implicit val deptUpdateReads: Reads[Option[String]] = (JsPath \ "title").readNullable[String](length)
  //TODO: users, but no phones
  def users(struct: String, page: Int, perPage: Int) = AuthAction.async {
    db.run {
      for {
        dept ← persist.Department.find(struct).headOption
        users ← dept.map { d ⇒
          for {
            userIds ← persist.UserDepartment.userIdsByDepartmentId(d.id)
            users ← persist.User.findByIdsPaged(userIds.toSet, page, perPage)
          } yield users
        } getOrElse DBIO.successful(Seq())
      } yield users.map { Json.toJson(_) }
    }.map { ul ⇒ Ok(Json.toJson(ul)) }
  }

  def create = AuthAction.async(BodyParsers.parse.json) { request ⇒
    request.body.validate[models.Department].map { department ⇒
      db.run {
        for {
          _ ← persist.Department.create(department)
        } yield Created(Json.toJson(Map("id" → department.id)))
      }
    } getOrElse Future(BadRequest)
  }

  def get(struct: String) = AuthAction.async { request ⇒
    db.run {
      for (depts ← persist.Department.deptAndChildren(struct)) yield nestDepartments(depts).map(Json.toJson(_))
    }.map(e ⇒ Ok(Json.toJson(e)))
  }

  def update(struct: String) = AuthAction.async(BodyParsers.parse.json) { request ⇒
    request.body.validate[Option[String]].map { optName ⇒
      db.run {
        for {
          _ ← optName.map {
            persist.Department.setName(struct, _)
          } getOrElse DBIO.successful(Ok)
        } yield Accepted
      }
    } getOrElse Future(BadRequest)
  }

  def delete(struct: String) = AuthAction.async { _ ⇒
    db.run {
      for {
        _ ← persist.Department.setDeletedAt(struct)
      } yield Accepted
    }
  }

}
