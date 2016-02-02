package im.actor.server.activation.gate

import im.actor.server.activation.common._
import play.api.libs.json._

trait JsonFormatters {

  implicit val codeWrites = Writes[Code] {
    case smsCode: SmsCode     ⇒ Json.writes[SmsCode].writes(smsCode)
    case callCode: CallCode   ⇒ Json.writes[CallCode].writes(callCode)
    case emailCode: EmailCode ⇒ Json.writes[EmailCode].writes(emailCode)
  }

  implicit val codeResponseReads: Reads[CodeResponse] =
    (JsPath \ "$type").read[String].flatMap[CodeResponse] {
      case "CodeHash"    ⇒ (JsPath \ "hash").read[String] map { hash ⇒ CodeHash(hash): CodeResponse }
      case "BadRequest"  ⇒ (JsPath \ "message").read[String] map { message ⇒ BadRequest(message): CodeResponse }
      case "SendFailure" ⇒ (JsPath \ "message").read[String] map { message ⇒ SendFailure(message): CodeResponse }
    }

  implicit val validationResponseReads: Reads[ValidationResponse] =
    (JsPath \ "$type").read[String].map[ValidationResponse] {
      case "Validated"     ⇒ Validated
      case "ExpiredCode"   ⇒ ExpiredCode
      case "InvalidCode"   ⇒ InvalidCode
      case "InvalidHash"   ⇒ InvalidHash
      case "InternalError" ⇒ InternalError
      case _               ⇒ InvalidResponse
    }

}
