package im.actor.server.webhooks

import akka.actor._
import im.actor.server.api.http.HttpApi
import im.actor.server.webhooks.http.routes.WebhooksHttpHandler

import scala.concurrent.Future

sealed trait WebhooksExtension extends Extension

/**
 * This extension only serves purpose of registering webhooks http hook
 *
 * @param system actor system
 */
final class WebhooksExtensionImpl(system: ActorSystem) extends WebhooksExtension {

  HttpApi(system).registerHook("webhooks") { implicit system ⇒
    Future.successful(new WebhooksHttpHandler().routes)
  }

}

object WebhooksExtension extends ExtensionId[WebhooksExtensionImpl] with ExtensionIdProvider {
  override def lookup = WebhooksExtension

  override def createExtension(system: ExtendedActorSystem) = new WebhooksExtensionImpl(system)
}