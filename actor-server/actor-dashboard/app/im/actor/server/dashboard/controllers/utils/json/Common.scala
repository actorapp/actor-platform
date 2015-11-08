package im.actor.server.dashboard.controllers.utils.json

import play.api.libs.json.Reads._
import play.api.libs.json.{ Reads, Json, JsValue, Writes }
import play.api.libs.functional.syntax._

import im.actor.server.model

object Common {

  val length: Reads[String] = minLength[String](1) keepAnd maxLength[String](255)

  val userWrites = new Writes[model.User] {
    def writes(user: model.User): JsValue = Json.obj(
      "id" → user.id,
      "name" → user.name,
      "sex" → user.sex.toInt
    )
  }
}
