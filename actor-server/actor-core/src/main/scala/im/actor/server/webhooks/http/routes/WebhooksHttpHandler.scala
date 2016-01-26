package im.actor.server.webhooks.http.routes

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.{ ActorMaterializer, Materializer }
import akka.util.Timeout
import im.actor.server.KeyValueMappings
import im.actor.server.api.http.HttpHandler
import shardakka.{ IntCodec, ShardakkaExtension }

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

final class WebhooksHttpHandler()(implicit val system: ActorSystem)
  extends HttpHandler
  with OutgoingHooks
  with IngoingHooks
  with TokenStatus {

  protected implicit val ec: ExecutionContext = system.dispatcher
  implicit val materializer: Materializer = ActorMaterializer()

  implicit val timeout: Timeout = Timeout(5.seconds)

  protected val log = Logging(system, getClass)
  protected val integrationTokensKv = ShardakkaExtension(system).simpleKeyValue[Int](KeyValueMappings.IntegrationTokens, IntCodec)

  override def routes: Route =
    defaultVersion {
      pathPrefix("webhooks") {
        outgoing ~ ingoing ~ status
      }
    }

}
