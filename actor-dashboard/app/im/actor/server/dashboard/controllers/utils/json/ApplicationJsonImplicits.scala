package im.actor.server.dashboard.controllers.utils.json

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{ JsPath, Reads }

import im.actor.server.dashboard.controllers.utils.json.Common._

object ApplicationJsonImplicits {

  case class LoginForm(email: String, passphrase: String)

  implicit val loginFormReads: Reads[LoginForm] = (
    (JsPath \ "email").read[String](email) and
    (JsPath \ "passphrase").read[String](length)
  )(LoginForm)

}
