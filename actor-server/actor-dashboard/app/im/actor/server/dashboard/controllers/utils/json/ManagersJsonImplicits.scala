package im.actor.server.dashboard.controllers.utils.json

import im.actor.server.acl.ACLUtils

import scala.concurrent.forkjoin.ThreadLocalRandom

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{ JsPath, Reads }

import im.actor.server.dashboard.controllers.utils.json.Common._
import im.actor.server.models

object ManagersJsonImplicits {

  implicit val managerReads: Reads[models.Manager] = (
    (JsPath \ "name").read[String](length) and
    (JsPath \ "lastName").read[String](length) and
    (JsPath \ "domain").read[String](length) and
    (JsPath \ "email").read[String](email)
  )(makeManager _)

  private def makeManager(name: String, lastName: String, domain: String, email: String): models.Manager = {
    val rnd = ThreadLocalRandom.current()
    val authToken = ACLUtils.accessToken(rnd)
    models.Manager(nextIntId(rnd), name, lastName, domain, new String(authToken), email)
  }
}
