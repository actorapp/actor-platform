package controllers

import controllers.utils.Db._
import controllers.utils.JsonConstructors._
import im.actor.server.{ models, persist }
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.mvc.{ Action, BodyParsers, Controller }
import slick.dbio.DBIO
import slick.driver.PostgresDriver.api._

import scala.concurrent.Future

class Users extends Controller {

  val auth_token = "auth_token"

  implicit val userPhoneWrites = new Writes[models.UserPhone] {
    def writes(phone: models.UserPhone): JsValue = Json.obj(
      "id" → phone.id,
      "number" → phone.number
    )
  }

  implicit val userWrites = new Writes[models.User] {
    def writes(user: models.User): JsValue = Json.obj(
      "id" → user.id,
      "name" → user.name,
      "sex" → user.sex.toInt
    )
  }

  implicit val userReads: Reads[Lang2UserAndPhone] = (
    (JsPath \ "name").read[String](length) and
    (JsPath \ "phone").read[String](length)
  )(makeUserAndPhone _)

  implicit val userUpdateReads: Reads[Option[String]] = (JsPath \ "name").readNullable[String](length)

  def get(id: Int) = Action.async { request ⇒
    db.run {
      for {
        optUser ← persist.User.find(id).headOption
        phones ← persist.UserPhone.findByUserId(id)
        user ← optUser.map { u ⇒
          DBIO.successful(Ok(Json.toJson(u).as[JsObject] + ("phones" → Json.toJson(phones))))
        } getOrElse DBIO.successful(NotFound("No such user found"))
      } yield user
    }
  }

  def create = Action.async(BodyParsers.parse.json) { request ⇒
    request.body.validate[Lang2UserAndPhone].map { userAndPhone ⇒
      db.run {
        userAndPhone(request.acceptLanguages.headOption) match {
          case (Some(user), Some(phone)) ⇒
            for {
              _ ← persist.User.create(user)
              _ ← persist.UserPhone.create(phone)
            } yield Ok(Json.toJson(Map("id" → user.id)))
          ///are those possible?
          case (Some(user), _) ⇒
            for {
              _ ← persist.User.create(user)
            } yield Ok(Json.toJson(Map("id" → user.id)))
          ///are those possible?
          case (_, Some(phone)) ⇒ DBIO.successful(BadRequest(Json.toJson(Map("message" → "No user name provided"))))
          case _                ⇒ DBIO.successful(BadRequest(Json.toJson(Map("message" → "No name and phone provided"))))
        }
      }
    } getOrElse Future(BadRequest)
  }

  def update(id: Int) = Action.async(BodyParsers.parse.json) { request ⇒
    request.body.validate[Option[String]].map { optName ⇒
      db.run {
        for {
          _ ← optName.map { persist.User.updateName(id, _) } getOrElse DBIO.successful(Ok)
        } yield Accepted
      }
    } getOrElse Future(BadRequest)
  }

  def delete(id: Int) = Action.async { request ⇒
    db.run {
      for {
        _ ← persist.User.markDeleted(id)
      } yield Accepted
    }
  }

  def list(page: Int, perPage: Int) = Action.async { request ⇒
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
