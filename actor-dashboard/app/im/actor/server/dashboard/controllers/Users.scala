package im.actor.server.dashboard.controllers

import scala.concurrent.Future

import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.mvc.{ BodyParsers, Controller }
import slick.dbio.DBIO
import slick.driver.PostgresDriver.api._

import im.actor.server.dashboard.controllers.utils._
import im.actor.server.dashboard.controllers.utils.json.UsersJsonImplicits._
import im.actor.server.{ models, persist }

class Users extends Controller {

  protected val db = Db.db

  def get(id: Int) = AuthAction.async { request ⇒
    db.run {
      for {
        optUser ← persist.User.find(id).headOption
        phones ← persist.UserPhone.findByUserId(id)
        user ← optUser.map { u ⇒
          DBIO.successful(Ok(Json.toJson(u).as[JsObject] + ("phones" → Json.toJson(phones))))
        } getOrElse DBIO.successful(NotFound(Json.toJson(Map("message" → "No such user found"))))
      } yield user
    }
  }

  def create = AuthAction.async(BodyParsers.parse.json) { request ⇒
    request.body.validate[CompleteUser](userReads(request.acceptLanguages.headOption)).fold(
      errors ⇒ Future.successful(NotAcceptable(Json.toJson(JsError.toFlatJson(errors)))),
      completeUser ⇒ db.run {
        for {
          dept ← persist.Department.find(completeUser.struct).headOption
          result ← dept.map { d ⇒
            for {
              optPhone ← persist.UserPhone.findByPhoneNumber(completeUser.phone.number).headOption
              result ← optPhone.map { phone ⇒
                DBIO.successful(
                  NotAcceptable(Json.toJson(Map("message" → s"User with phone ${phone.number} already exists")))
                )
              } getOrElse {
                for {
                  _ ← persist.User.create(completeUser.user)
                  _ ← persist.UserPhone.create(completeUser.phone)
                  _ ← persist.UserDepartment.create(completeUser.user.id, d.id)
                } yield Created(Json.toJson(Map("id" → completeUser.user.id)))
              }
            } yield result
          } getOrElse DBIO.successful(NotAcceptable(Json.toJson(Map("message" → "User was not created because no such department exist"))))
        } yield result
      }
    )
  }

  def update(id: Int) = AuthAction.async(BodyParsers.parse.json) { request ⇒
    request.body.validate[UserUpdate].fold(
      errors ⇒ Future.successful(NotAcceptable(Json.toJson(JsError.toFlatJson(errors)))),
      update ⇒ db.run {
        for (_ ← persist.User.setName(id, update.name)) yield Accepted
      }
    )
  }

  def delete(id: Int) = AuthAction.async { request ⇒
    db.run {
      for {
        _ ← persist.User.setDeletedAt(id)
      } yield Accepted
    }
  }

  def list(page: Int, perPage: Int) = AuthAction.async { request ⇒
    db.run {
      for {
        usersAndPhones ← (for {
          (u, up) ← persist.User.page(page, perPage) joinLeft persist.UserPhone.phones on (_.id === _.userId)
        } yield (u, up)).result
        result ← DBIO.successful(
          usersAndPhones.
            foldLeft(Map[Int, (models.User, List[models.UserPhone])]()) { (acc, el) ⇒
              val (user, optPhone) = el
              optPhone.map { phone ⇒
                acc.get(user.id).map { tuple ⇒
                  acc.updated(user.id, (tuple._1, phone :: tuple._2))
                } getOrElse acc.updated(user.id, (user, List(phone)))
              } getOrElse acc.updated(user.id, (user, List()))
            }.values.map { e ⇒ Json.toJson(e._1).as[JsObject] + ("phones" → Json.toJson(e._2)) }
        )
      } yield Ok(Json.toJson(result))
    }
  }

}