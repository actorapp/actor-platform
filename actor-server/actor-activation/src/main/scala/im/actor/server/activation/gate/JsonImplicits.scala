package im.actor.server.activation.gate

import akka.http.scaladsl.unmarshalling._
import akka.stream.Materializer
import play.api.libs.json._
import play.api.libs.functional.syntax._

import akka.http.scaladsl.unmarshalling.PredefinedFromEntityUnmarshallers._

import im.actor.server.activation.Activation.{ EmailCode, SmsCode, Code }
import im.actor.server.activation._

trait JsonImplicits {
  implicit val materializer: Materializer

  val smsCodeWrites: Writes[SmsCode] = new Writes[SmsCode] {
    override def writes(code: SmsCode): JsValue = Json.obj("phone" → code.phone, "code" → code.code)
  }

  implicit val codeWrites = Writes[Code] {
    case smsCode: SmsCode     ⇒ smsCodeWrites.writes(smsCode)
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

  implicit val toCodeResponse: FromResponseUnmarshaller[CodeResponse] = Unmarshaller { implicit ec ⇒ resp ⇒
    Unmarshal(resp.entity).to[String].map { body ⇒ Json.parse(body).as[CodeResponse] }
  }

  implicit val toValidationResponse: FromResponseUnmarshaller[ValidationResponse] = Unmarshaller { implicit ec ⇒ resp ⇒
    Unmarshal(resp.entity).to[String].map { body ⇒ Json.parse(body).as[ValidationResponse] }
  }

}
