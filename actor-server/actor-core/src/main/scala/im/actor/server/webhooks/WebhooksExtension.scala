package im.actor.server.webhooks

import akka.actor._
import im.actor.server.api.http.HttpApi
import im.actor.server.webhooks.http.routes.WebhooksHttpHandler

sealed trait WebhooksExtension extends Extension

/**
 * This extension only serves purpose of registering webhooks http hook
 *
 * @param system actor system
 */
final class WebhooksExtensionImpl(system: ActorSystem) extends WebhooksExtension {

  HttpApi(system).registerRoute("webhooks") { implicit system ⇒
    new WebhooksHttpHandler().routes
  }

}

object WebhooksExtension extends ExtensionId[WebhooksExtensionImpl] with ExtensionIdProvider {
  override def lookup = WebhooksExtension

  override def createExtension(system: ExtendedActorSystem) = new WebhooksExtensionImpl(system)
}