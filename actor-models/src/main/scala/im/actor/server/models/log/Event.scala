package im.actor.server.models.log

import spray.json._

object EventKind extends Enumeration {
  type EventKind = Value
  val AuthCode = Value("auth_code")
  val SignIn = Value("sign_in")
  val SignUp = Value("sign_up")
}

object Event extends DefaultJsonProtocol {
  sealed trait EventMessage {
    val klass: Int
    def asJson: String
  }
  case class RpcError(kind: EventKind.EventKind, code: Int, message: String) extends EventMessage {
    val klass = RpcError.klass
    def asJson = RpcErrorWrites.write(this).compactPrint
  }
  case class SmsSentSuccessfully(body: String, gateResponse: String) extends EventMessage {
    val klass = SmsSentSuccessfully.klass
    def asJson = SmsSentSuccessfullyWrites.write(this).compactPrint
  }
  case class SmsFailure(body: String, gateResponse: String) extends EventMessage {
    val klass = SmsFailure.klass
    def asJson = SmsFailureWrites.write(this).compactPrint
  }
  case class AuthCodeSent(smsHash: String, smsCode: String) extends EventMessage {
    val klass = AuthCodeSent.klass
    def asJson = AuthCodeSentWrites.write(this).compactPrint
  }
  case class SignedIn(smsHash: String, smsCode: String) extends EventMessage {
    val klass = SignedIn.klass
    def asJson = SignedInWrites.write(this).compactPrint
  }
  case class SignedUp(smsHash: String, smsCode: String) extends EventMessage {
    val klass = SignedUp.klass
    def asJson = SignedUpWrites.write(this).compactPrint
  }

  object RpcError { val klass = 0 }
  object SmsSentSuccessfully { val klass = 1 }
  object SmsFailure { val klass = 2 }
  object AuthCodeSent { val klass = 3 }
  object SignedIn { val klass = 4 }
  object SignedUp { val klass = 5 }

  implicit object RpcErrorWrites extends RootJsonWriter[RpcError] {
    def write(e: RpcError) = JsObject(
      "kind" → JsString(e.kind.toString),
      "code" → JsNumber(e.code),
      "message" → JsString(e.message)
    )
  }
  implicit object SmsSentSuccessfullyWrites extends RootJsonWriter[SmsSentSuccessfully] {
    def write(e: SmsSentSuccessfully) = JsObject(
      "body" → JsString(e.body),
      "gateResponse" → JsString(e.gateResponse)
    )
  }
  implicit object SmsFailureWrites extends RootJsonWriter[SmsFailure] {
    def write(e: SmsFailure) = JsObject(
      "body" → JsString(e.body),
      "gateResponse" → JsString(e.gateResponse)
    )
  }
  implicit object AuthCodeSentWrites extends RootJsonWriter[AuthCodeSent] {
    def write(e: AuthCodeSent) = JsObject(
      "smsHash" → JsString(e.smsHash),
      "smsCode" → JsString(e.smsCode)
    )
  }
  implicit object SignedInWrites extends RootJsonWriter[SignedIn] {
    def write(e: SignedIn) = JsObject(
      "smsHash" → JsString(e.smsHash),
      "smsCode" → JsString(e.smsCode)
    )
  }
  implicit object SignedUpWrites extends RootJsonWriter[SignedUp] {
    def write(e: SignedUp) = JsObject(
      "smsHash" → JsString(e.smsHash),
      "smsCode" → JsString(e.smsCode)
    )
  }
}
