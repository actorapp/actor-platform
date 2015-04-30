package im.actor.server.dashboard.controllers.utils

import im.actor.server.dashboard.controllers.Lang2UserAndPhone
import im.actor.server.models
import im.actor.server.util.{ ACL, PhoneNumber }
import org.apache.commons.codec.digest.DigestUtils
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads
import play.api.libs.json.Reads._

import scala.concurrent.forkjoin.ThreadLocalRandom

object JsonConstructors {

  val length: Reads[String] = minLength[String](1) keepAnd maxLength[String](255)

  private val rnd = ThreadLocalRandom.current()

  def nextAuthToken(email: String) = "TODO" //TODO: replace authToken in db

  def makeManager(name: String, lastName: String, domain: String, email: String): models.Manager = {
    val id = rnd.nextInt(Int.MaxValue) + 1
    val authToken = DigestUtils.sha256(name + lastName)
    models.Manager(id, name, lastName, domain, new String(authToken), email)
  }

  def makeUserAndPhone(name: String, phone: String): Lang2UserAndPhone = { lang ⇒
    val (userId, phoneId) = (rnd.nextInt(Int.MaxValue) + 1, rnd.nextInt(Int.MaxValue) + 1)
    (for {
      code ← lang.map { _.language.toUpperCase }
      normalizedPhone ← PhoneNumber.normalizeStr(phone, code)
      user = models.User(userId, ACL.nextAccessSalt(rnd), name, code, models.NoSex, models.UserState.Registered)
      userPhone = models.UserPhone(phoneId, userId, ACL.nextAccessSalt(rnd), normalizedPhone, "Mobile phone")
    } yield (user, userPhone)) match {
      case Some((user, userPhone)) ⇒ (Some(user), Some(userPhone))
      case None                    ⇒ (None, None)
    }
  }

}
