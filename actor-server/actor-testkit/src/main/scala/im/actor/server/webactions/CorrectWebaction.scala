package im.actor.server.webactions

import akka.actor.ActorSystem
import im.actor.api.rpc.collections.{ ApiStringValue, ApiInt32Value, ApiMapValueItem, ApiMapValue }

import scala.concurrent.Future

object CorrectWebaction {
  val uri = "https://google.com/"
  val regex = "https://mail.google.com"
  val completeUri = "https://mail.google.com/mail/u/0/#inbox"
}

class CorrectWebaction(system: ActorSystem) extends Webaction(system) {
  import system.dispatcher

  override def uri(params: ApiMapValue): String = CorrectWebaction.uri
  override def regex: String = CorrectWebaction.regex
  override def complete(userId: Int, url: String): Future[WebactionResult] = Future {
    Webaction.success(ApiMapValue(Vector(
      ApiMapValueItem("userId", ApiInt32Value(userId)),
      ApiMapValueItem("url", ApiStringValue(url.reverse))
    )))
  }
}
