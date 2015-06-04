package im.actor.server.api.rpc.service.webhooks

import im.actor.api.rpc.RpcError
import im.actor.server.api.http.HttpApiConfig

object IntegrationServiceHelpers {
  val TokenNotFound = RpcError(404, "TOKEN_NOT_FOUND", "", false, None)

  def makeUrl(config: HttpApiConfig, token: String): String = s"${config.baseUrl}/v1/webhooks/$token"
}
