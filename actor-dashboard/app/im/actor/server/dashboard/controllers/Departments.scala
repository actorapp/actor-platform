package im.actor.server.dashboard.controllers

import scala.concurrent.Future

import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json._
import play.api.mvc.{ BodyParsers, Controller }
import slick.dbio.DBIO

import im.actor.server.dashboard.controllers.utils.DepartmentUtils._
import im.actor.server.dashboard.controllers.utils.json.DepartmentsJsonImplicits._
import im.actor.server.dashboard.controllers.utils.{ AuthAction, Db }
import im.actor.server.{ models, persist }

class Departments extends Controller {

  protected val db = Db.db

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
    request.body.validate[models.Department].fold(
      errors ⇒ Future.successful(NotAcceptable(Json.toJson(JsError.toFlatJson(errors)))),
      department ⇒ db.run {
        for {
          _ ← persist.Department.create(department)
        } yield Created(Json.toJson(Map("id" → department.id)))
      }
    )
  }

  def get(struct: String) = AuthAction.async { request ⇒
    db.run {
      for (depts ← persist.Department.deptAndChildren(struct)) yield nestDepartments(depts).map(Json.toJson(_))
    }.map(e ⇒ Ok(Json.toJson(e)))
  }

  def update(struct: String) = AuthAction.async(BodyParsers.parse.json) { request ⇒
    request.body.validate[DepartmentUpdate].fold(
      errors ⇒ Future.successful(NotAcceptable(Json.toJson(JsError.toFlatJson(errors)))),
      update ⇒ db.run {
        for {
          _ ← persist.Department.setName(struct, update.title)
        } yield Accepted
      }
    )
  }

  def delete(struct: String) = AuthAction.async { _ ⇒
    db.run {
      for {
        _ ← persist.Department.setDeletedAt(struct)
      } yield Accepted
    }
  }

}
