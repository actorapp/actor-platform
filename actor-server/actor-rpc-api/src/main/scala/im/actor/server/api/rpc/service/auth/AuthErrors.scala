package im.actor.server.api.rpc.service.auth

import im.actor.api.rpc.{ CommonRpcErrors, RpcError }
import im.actor.server.acl.ACLUtils
import im.actor.server.activation.common.{ BadRequest, SendFailure, CodeFailure }

object AuthErrors {
  val AuthSessionNotFound = RpcError(404, "AUTH_SESSION_NOT_FOUND", "Auth session not found.", false, None)
  val CurrentSessionTermination = RpcError(400, "CURRENT_SESSION_TERMINATION", "You tried to terminate current auth session.", false, None)
  val InvalidKey = RpcError(400, "INVALID_KEY", "Invalid key.", false, None)
  val PhoneNumberInvalid = RpcError(400, "PHONE_NUMBER_INVALID", "Invalid phone number.", false, None)
  val PhoneNumberUnoccupied = RpcError(400, "PHONE_NUMBER_UNOCCUPIED", "", false, None)
  val PhoneCodeEmpty = RpcError(400, "PHONE_CODE_EMPTY", "Code is empty.", false, None)
  val PhoneCodeExpired = RpcError(400, "PHONE_CODE_EXPIRED", "Code is expired.", false, None)
  val PhoneCodeInvalid = RpcError(400, "PHONE_CODE_INVALID", "Invalid code.", false, None)
  val EmailUnoccupied = RpcError(400, "EMAIL_UNOCCUPIED", "", false, None)
  val EmailCodeExpired = RpcError(400, "EMAIL_CODE_EXPIRED", "Code is expired.", false, None)
  val EmailCodeInvalid = RpcError(400, "EMAIL_CODE_INVALID", "Invalid code.", false, None)
  val UsernameCodeExpired = RpcError(400, "USERNAME_CODE_EXPIRED", "Codeode is expired.", false, None)
  val UsernameCodeInvalid = RpcError(400, "USERNAME_CODE_INVALID", "Invalid code.", false, None)
  val UsernameUnoccupied = RpcError(400, "USERNAME_UNOCCUPIED", "", false, None)
  val RedirectUrlInvalid = RpcError(400, "REDIRECT_URL_INVALID", "", false, None)
  val NotValidated = RpcError(400, "NOT_VALIDATED", "", false, None) //todo: proper name
  val FailedToGetOAuth2Token = RpcError(400, "FAILED_GET_OAUTH2_TOKEN", "Authorization server error.", false, None)
  val OAuthUserIdDoesNotMatch = RpcError(400, "WRONG_OAUTH2_USER_ID", "Email does not match one provided on the first step.", false, None)
  val ActivationServiceError = RpcError(500, "ACTIVATION_SERVICE_ERROR", "Error occured in activation service. Try again later.", true, None)
  val InvalidAuthCodeHash = RpcError(400, "CODE_HASH_INVALID", "", false, None)
  val PasswordInvalid = RpcError(400, "PASSWORD_INVALID", s"Password have to be more than ${ACLUtils.PasswordMinLength} and less than ${ACLUtils.PasswordMaxLength}", false, None)
  val UserDeleted = CommonRpcErrors.forbidden("Unable to log in, your account is deleted")

  def activationFailure(failure: CodeFailure): RpcError = failure match {
    case SendFailure(message) ⇒ RpcError(500, "GATE_ERROR", message, true, None)
    case BadRequest(message)  ⇒ RpcError(400, "CODE_WAIT", message, false, None)
  }
}
