package im.actor.server.api.rpc.service.auth

import im.actor.api.rpc.RpcError

object AuthErrors {
  val AuthSessionNotFound = RpcError(404, "AUTH_SESSION_NOT_FOUND", "Auth session not found.", false, None)
  val InvalidKey = RpcError(400, "INVALID_KEY", "Invalid key.", false, None)
  val PhoneNumberInvalid = RpcError(400, "PHONE_NUMBER_INVALID", "Invalid phone number.", false, None)
  val PhoneNumberUnoccupied = RpcError(400, "PHONE_NUMBER_UNOCCUPIED", "", false, None)
  val PhoneCodeEmpty = RpcError(400, "PHONE_CODE_EMPTY", "Code is empty.", false, None)
  val PhoneCodeExpired = RpcError(400, "PHONE_CODE_EXPIRED", "Code is expired.", false, None)
  val PhoneCodeInvalid = RpcError(400, "PHONE_CODE_INVALID", "Invalid code.", false, None)

  val InvalidAuthTransaction = RpcError(400, "TRANSACTION_HASH_INVALID", "", false, None)
  val EmailUnoccupied = RpcError(400, "EMAIL_UNOCCUPIED", "", false, None)
  val RedirectUrlInvalid = RpcError(400, "REDIRECT_URL_INVALID", "", false, None)
  val NotValidated = RpcError(400, "NOT_VALIDATED", "", false, None) //todo: proper name
  val FailedToGetOAuth2Token = RpcError(400, "FAILED_GET_OAUTH2_TOKEN", "", false, None)
}
