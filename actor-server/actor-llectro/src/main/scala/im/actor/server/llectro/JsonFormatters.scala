package im.actor.server.llectro

import play.api.libs.json._
import play.api.libs.functional.syntax._
import im.actor.server.llectro.results.{ UserBalance, Errors, Banner }
import im.actor.server.models.llectro._

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

  implicit val userBalanceReads: Reads[UserBalance] = (
    (JsPath \ "name").read[String] and
    (JsPath \ "balance").read[BigDecimal]
  )(UserBalance)

}

trait JsonWrites {
  implicit val userWrites: Writes[LlectroUser] = new Writes[LlectroUser] {
    def writes(user: LlectroUser) = Json.obj(
      "uuid" → user.uuid,
      "name" → user.name
    )
  }
}

object JsonReads extends JsonReads
object JsonWrites extends JsonWrites
object JsonFormatters extends JsonReads with JsonWrites

