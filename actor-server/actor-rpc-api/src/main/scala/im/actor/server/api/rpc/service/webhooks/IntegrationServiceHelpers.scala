package im.actor.server.api.rpc.service.webhooks

import im.actor.api.rpc.RpcError

object IntegrationServiceHelpers {
  val TokenNotFound = RpcError(404, "TOKEN_NOT_FOUND", "", false, None)
  val FailedToRevokeToken = RpcError(404, "FAILED_TO_REVOKE_TOKEN", "", false, None)

  def makeUrl(baseUrl: String, token: String): String = s"${baseUrl}/v1/webhooks/$token"
}
