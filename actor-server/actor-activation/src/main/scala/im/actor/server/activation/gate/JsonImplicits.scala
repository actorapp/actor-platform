package im.actor.server.activation.gate

import im.actor.server.activation.Activation.{ CallCode, Code, EmailCode, SmsCode }
import im.actor.server.activation._
import play.api.libs.functional.syntax._
import play.api.libs.json._

trait JsonImplicits {

  val smsCodeWrites: Writes[SmsCode] = new Writes[SmsCode] {
    override def writes(code: SmsCode): JsValue = Json.obj("phone" → code.phone, "code" → code.code)
  }

  implicit val codeWrites = Writes[Code] {
    case smsCode: SmsCode     ⇒ smsCodeWrites.writes(smsCode)
    case callCode: CallCode   ⇒ Json.writes[CallCode].writes(callCode)
    case emailCode: EmailCode ⇒ Json.writes[EmailCode].writes(emailCode)
  }

  implicit val codeResponseReads: Reads[CodeResponse] = {
    val sr = Json.reads[CodeHash]
    val er = Json.reads[CodeError]
    __.read[CodeHash](sr).map(x ⇒ x.asInstanceOf[CodeResponse]) |
      __.read[CodeError](er).map(x ⇒ x.asInstanceOf[CodeResponse])
  }

  implicit val validationResponseReads: Reads[ValidationResponse] = {
    (JsPath \ "status").read[String].map[ValidationResponse] {
      case s if s == "validated"    ⇒ Validated
      case s if s == "expired code" ⇒ ExpiredCode
      case s if s == "invalid code" ⇒ InvalidCode
      case s if s == "invalid hash" ⇒ InvalidHash
      case _                        ⇒ InvalidResponse
    }
  }

}
