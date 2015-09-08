package im.actor.server.api.http.webhooks

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import akka.util.Timeout
import im.actor.server.KeyValueMappings
import im.actor.server.api.http.RoutesHandler
import im.actor.server.dialog.group.GroupDialogRegion
import im.actor.server.group.GroupViewRegion
import shardakka.{ IntCodec, ShardakkaExtension }

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class WebhooksHandler()(
  implicit
  val system:               ActorSystem,
  val ec:                   ExecutionContext,
  val groupProcessorRegion: GroupViewRegion,
  val groupDialogRegion:    GroupDialogRegion,
  val materializer:         Materializer
) extends RoutesHandler with OutgoingHooks with IngoingHooks with TokenStatus {

  implicit val timeout: Timeout = Timeout(5.seconds)

  protected val integrationTokensKv = ShardakkaExtension(system).simpleKeyValue[Int](KeyValueMappings.IntegrationTokens, IntCodec)

  override def routes: Route = pathPrefix("webhooks") {
    outgoing ~ ingoing ~ status
  }

}
