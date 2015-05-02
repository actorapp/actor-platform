package im.actor.server.dashboard.controllers.utils.json

import scala.concurrent.forkjoin.ThreadLocalRandom

import play.api.libs.functional.syntax._
import play.api.libs.json._

import im.actor.server.dashboard.controllers._
import Common._
import im.actor.server.models
import im.actor.server.util.IdUtils._
import im.actor.server.util.{ ACLUtils, PhoneNumber }

object UsersJsonImplicits {

  implicit val userWrites = Common.userWrites

  implicit val userPhoneWrites = new Writes[models.UserPhone] {
    def writes(phone: models.UserPhone): JsValue = Json.obj(
      "id" → phone.id,
      "number" → phone.number
    )
  }

  implicit val userReads: Reads[Lang2UserAndPhone] = (
    (JsPath \ "name").read[String](length) and
    (JsPath \ "phone").read[String](length)
  )(makeUserAndPhone _)

  implicit val userUpdateReads: Reads[Option[String]] = (JsPath \ "name").readNullable[String](length)

  private def makeUserAndPhone(name: String, phone: String): Lang2UserAndPhone = { lang ⇒
    val rnd = ThreadLocalRandom.current()
    val (userId, phoneId) = (nextIntId(rnd), nextIntId(rnd))
    (for {
      code ← lang.map { _.language.toUpperCase }
      normalizedPhone ← PhoneNumber.normalizeStr(phone, code)
      user = models.User(userId, ACLUtils.nextAccessSalt(rnd), name, code, models.NoSex, models.UserState.Registered)
      userPhone = models.UserPhone(phoneId, userId, ACLUtils.nextAccessSalt(rnd), normalizedPhone, "Mobile phone")
    } yield (user, userPhone)) match {
      case Some((user, userPhone)) ⇒ (Some(user), Some(userPhone))
      case None                    ⇒ (None, None)
    }
  }

}
