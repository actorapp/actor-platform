package im.actor.server.activation.common

trait CodeResponse

sealed trait CodeFailure

final case class CodeHash(hash: String) extends CodeResponse
//activation server failure. client should retry to send activation code
final case class SendFailure(message: String) extends CodeResponse with CodeFailure
//invalid request. User should react to message provided in error response
final case class BadRequest(message: String) extends CodeResponse with CodeFailure

sealed trait ValidationResponse

case object Validated extends ValidationResponse
case object ExpiredCode extends ValidationResponse
case object InvalidCode extends ValidationResponse
case object InvalidHash extends ValidationResponse
case object InternalError extends ValidationResponse
case object InvalidResponse extends ValidationResponse