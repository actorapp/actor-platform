package controllers

import im.actor.server.{models, persist}
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.mvc.{Action, BodyParsers, Controller}
import utils.Db.db
import utils.JsonConstructors.length

import scala.concurrent.Future

class Application extends Controller {

  case class LoginForm(email:String, passphrase:String)

  implicit val loginFormReads: Reads[LoginForm] = (
    (JsPath \ "email").read[String](email) and
    (JsPath \ "passphrase").read[String](length)
  )(LoginForm)

  def forbidden(message:String) = Forbidden(Json.toJson(Map("message" -> message)))

  def login = Action.async(BodyParsers.parse.json) { request =>
    request.body.validate[LoginForm].map { form =>
      db.run(persist.Manager.findByEmail(form.email).headOption).flatMap { mOpt =>
//        db.run(persist.AuthSmsCode.findByEmail(form.email).headOption).map { cOpt =>
        Future(Option(models.AuthSmsCode(123, "xx", "test"))).map { cOpt => //dummy implementation, TODO remove
          mOpt.flatMap { manager =>
            cOpt.map { code =>
              if (code.smsCode == form.passphrase)
                Ok.withSession("auth_token" -> manager.authToken)
              else
              forbidden("wrong passphrase")
            }.orElse(Some(forbidden("wrong passphrase")))
          }.getOrElse(forbidden("wrong email"))
        }
      }
    }.getOrElse(Future(BadRequest))
  }

  def logout() = Action(BodyParsers.parse.json) { request =>
    request.session.get("auth_token").
      map { token => Ok.withNewSession } getOrElse (forbidden("No auth token provided"))
  }
}
