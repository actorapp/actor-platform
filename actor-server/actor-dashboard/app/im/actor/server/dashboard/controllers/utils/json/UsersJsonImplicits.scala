package im.actor.server.dashboard.controllers.utils.json

import im.actor.server.acl.ACLUtils

import scala.concurrent.forkjoin.ThreadLocalRandom

import play.api.data.validation.ValidationError
import play.api.i18n.Lang
import play.api.libs.functional.syntax._
import play.api.libs.json._

import im.actor.server.dashboard.controllers.utils.json.Common._
import im.actor.server.models

object UsersJsonImplicits {

  implicit val userWrites = Common.userWrites

  implicit val userPhoneWrites = new Writes[models.UserPhone] {
    def writes(phone: models.UserPhone): JsValue = Json.obj(
      "id" → phone.id,
      "number" → phone.number
    )
  }

  def userReads(lang: Option[Lang]): Reads[CompleteUser] = {
    val language = lang.getOrElse(Lang("RU")).language.toUpperCase
    ((JsPath \ "name").read[String](length) and
      (JsPath \ "dept").read[String](length) and
      (JsPath \ "phone").read[String](validPhone(language)))(makeUserAndPhone(language) _)
  }

  def validPhone(language: String)(implicit reads: Reads[String]): Reads[String] =
    Reads[String](js ⇒ reads.reads(js).filter(ValidationError("error.invalidPhone", js)) { phone ⇒
      PhoneNumberUtils.isValid(phone, language)
    })

  case class UserUpdate(name: String)

  implicit val userUpdateReads: Reads[UserUpdate] = (JsPath \ "name").read[String](length).map { UserUpdate }

  case class CompleteUser(user: models.User, struct: String, phone: models.UserPhone)

  private def makeUserAndPhone(language: String)(name: String, struct: String, phone: String): CompleteUser = {
    val rnd = ThreadLocalRandom.current()
    val (userId, phoneId) = (nextIntId(rnd), nextIntId(rnd))
    val normalizedPhone = PhoneNumberUtils.tryNormalize(phone.toLong, language)
    val user = models.User(userId, ACLUtils.nextAccessSalt(rnd), name, language, models.NoSex, models.UserState.Registered)
    val userPhone = models.UserPhone(phoneId, userId, ACLUtils.nextAccessSalt(rnd), normalizedPhone, "Mobile phone")
    CompleteUser(user, struct, userPhone)
  }

}
