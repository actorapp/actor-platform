package im.actor.server.dashboard.controllers

import scala.concurrent.Future

import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.{ JsError, Json }
import play.api.mvc.{ Action, BodyParsers, Controller }
import slick.dbio.DBIO

import im.actor.server.dashboard.controllers.utils.Db._
import im.actor.server.dashboard.controllers.utils.json.ManagersJsonImplicits._
import im.actor.server.{ models, persist }

class Managers extends Controller {

  def create = Action.async(BodyParsers.parse.json) { request ⇒
    request.body.validate[models.Manager].fold(
      errors ⇒ Future.successful(NotAcceptable(Json.toJson(JsError.toFlatJson(errors)))),
      fromRequest ⇒ db.run {
        for {
          duplicate ← persist.Manager.findByEmail(fromRequest.email)
          result ← duplicate.map { d ⇒
            DBIO.successful(
              NotAcceptable(Json.toJson(Map("message" → s"Manager with email ${fromRequest.email} already exists")))
            )
          } getOrElse {
            for {
              _ ← persist.Manager.create(fromRequest)
            } yield Created
          }
        } yield result
      }
    )
  }

}
