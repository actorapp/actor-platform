package controllers

import im.actor.server._
import play.api.libs.json.{JsValue, Json, Writes}
import play.api.mvc.Controller

class Users extends Controller {

  def create = TODO

  implicit val userPhoneWrites = new Writes[models.UserPhone] {
    def writes(phone: models.UserPhone): JsValue = Json.obj(
      "id" -> phone.id,
      "number" -> phone.number
    )
  }

  def update(id: Int) = TODO

  def delete(id: Int) = TODO

  def get(id: Int) = TODO

  def list(page: Int, perPage: Int) = TODO

}
