package im.actor.server.dashboard.controllers

import scala.concurrent.Future

import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json._
import play.api.mvc.{ Action, BodyParsers, Controller }
import slick.dbio._

import im.actor.server.dashboard.controllers.utils.AuthAction
import im.actor.server.dashboard.controllers.utils.Db._
import im.actor.server.dashboard.controllers.utils.json.ApplicationJsonImplicits._
import im.actor.server.persist

class Application extends Controller {

  def forbidden(message: String) = Forbidden(Json.toJson(Map("message" → message))) //TODO: find better way to pass json body

  def login = Action.async(BodyParsers.parse.json) { request ⇒
    request.body.validate[LoginForm].map { form ⇒
      db.run {
        (for {
          optManager ← persist.Manager.findByEmail(form.email)
          optAuthCode ← persist.AuthSmsCodeObsolete.findByPhoneNumber(form.email.toLong).headOption //TODO: write persist.AuthSmsCode.findByEmail(form.email) implementation
        } yield (optManager, optAuthCode)).flatMap {
          case (Some(manager), Some(authCode)) ⇒
            DBIO.successful {
              if (form.passphrase == authCode.smsCode)
                Ok(Json.toJson(Map("auth-token" → manager.authToken)))
              else
                BadRequest(Json.toJson(Map("message" → "wrong authCode")))
            }
          case (None, _) ⇒ DBIO.successful(BadRequest(Json.toJson(Map("message" → "no such account"))))
          case (_, None) ⇒ DBIO.successful(BadRequest(Json.toJson(Map("message" → "auth code error"))))
        }
      }
    }.getOrElse(Future(BadRequest))
  }

  def logout(email: String) = AuthAction { request ⇒
    //    nextAuthToken(email)
    Ok
  }

}
