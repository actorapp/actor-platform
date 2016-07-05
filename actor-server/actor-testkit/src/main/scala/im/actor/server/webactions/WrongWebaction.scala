package im.actor.server.webactions

import akka.actor.ActorSystem
import akka.http.scaladsl.util.FastFuture
import im.actor.api.rpc.collections.ApiMapValue

import scala.concurrent.Future

//MUST fail
class WrongWebaction(system: ActorSystem) extends Webaction(system) {
  val importantVal = 2 / 0

  override def uri(params: ApiMapValue): String = ""
  override def regex: String = ""
  override def complete(userId: Int, url: String): Future[WebactionResult] = FastFuture.successful(Webaction.failure("", None))
}
