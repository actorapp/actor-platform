package im.actor.server.api.http.dashboard

import play.api.libs.json.{ Json, JsValue, Writes }

import im.actor.server.models

object JsonFormatters {

  implicit val userWrites = new Writes[models.User] {
    def writes(user: models.User): JsValue = Json.obj(
      "id" → user.id,
      "name" → user.name,
      "sex" → user.sex.toInt
    )
  }

  implicit val userPhoneWrites = new Writes[models.UserPhone] {
    def writes(phone: models.UserPhone): JsValue = Json.obj(
      "id" → phone.id,
      "number" → phone.number
    )
  }

  implicit val userEmailWrites = new Writes[models.UserEmail] {
    def writes(email: models.UserEmail): JsValue = Json.obj(
      "id" → email.id,
      "email" → email.email
    )
  }

  implicit val completeUserWrites = Json.writes[CompleteUser]

  implicit val dashboardErrorWrites = Json.writes[DashboardError]

  implicit val createdUserWrites = Json.writes[CreatedUser]

  implicit val authTokenWrites = Json.writes[AuthToken]

  implicit val userFormReads = Json.reads[UserForm]

  implicit val updateFormReads = Json.reads[UpdateForm]

  implicit val loginFormReads = Json.reads[LoginForm]
}