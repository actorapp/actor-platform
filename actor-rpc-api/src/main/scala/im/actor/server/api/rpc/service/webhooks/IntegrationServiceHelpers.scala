package im.actor.server.api.rpc.service.webhooks

import im.actor.api.rpc.RpcError
import im.actor.server.webhooks.WebhooksConfig

object IntegrationServiceHelpers {
  val TokenNotFound = RpcError(404, "TOKEN_NOT_FOUND", "", false, None)

  def makeUrl(config: WebhooksConfig, token: String): String = s"${config.protocol}://${config.interface}:${config.port}${config.path}/$token"
}
