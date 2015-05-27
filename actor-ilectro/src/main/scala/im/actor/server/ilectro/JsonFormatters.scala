package im.actor.server.ilectro

import play.api.libs.json._
import play.api.libs.functional.syntax._
import im.actor.server.ilectro.results.{ Errors, Banner }
import im.actor.server.models.ilectro._

trait JsonReads {
  implicit val interestReads: Reads[Interest] = (
    (JsPath \ "id").read[Int] and
    (JsPath \ "name").read[String] and
    (JsPath \ "parent_id").read[Int] and
    (JsPath \ "full_path").read[String] and
    (JsPath \ "level").read[Int]
  )(Interest)

  implicit val bannerReads: Reads[Banner] = (
    (JsPath \ "advertUrl").read[String] and
    (JsPath \ "imageUrl").read[String]
  )(Banner)

  implicit val errorsReads: Reads[Errors] = (
    (JsPath \ "errors").read[String] and
    (JsPath \ "status").readNullable[Int]
  )(Errors)
}

trait JsonWrites {
  implicit val userWrites: Writes[ILectroUser] = new Writes[ILectroUser] {
    def writes(user: ILectroUser) = Json.obj(
      "uuid" → user.uuid,
      "name" → user.name
    )
  }
}

object JsonReads extends JsonReads
object JsonWrites extends JsonWrites
object JsonFormatters extends JsonReads with JsonWrites

