package controllers

import java.sql.SQLException

import controllers.utils.Db._
import controllers.utils.JsonConstructors._
import im.actor.server.{ models, persist }
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.mvc.{ Action, BodyParsers, Controller }

import scala.concurrent.Future

class Managers extends Controller {

  implicit val managerReads: Reads[models.Manager] = (
    (JsPath \ "name").read[String](length) and
    (JsPath \ "lastName").read[String](length) and
    (JsPath \ "domain").read[String](length) and
    (JsPath \ "email").read[String](email)
  )(makeManager _)

  def create = Action.async(BodyParsers.parse.json) { request ⇒
    request.body.validate[models.Manager] map { value ⇒
      db.run(persist.Manager.create(value)).
        map { res ⇒ Created }.
        recover { case e: SQLException ⇒ BadRequest(e.getMessage) }
    } getOrElse Future(BadRequest)
  }

}
