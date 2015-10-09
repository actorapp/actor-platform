package im.actor.server.webactions

import akka.actor.ActorSystem
import im.actor.api.rpc.collections.{ ApiStringValue, ApiMapValueItem, ApiMapValue }
import im.actor.config.ActorConfig

import scala.concurrent.Future
import scala.collection.JavaConversions._
import scala.util.Try

sealed trait WebactionResult {
  def isSuccess: Boolean
  def content: ApiMapValue
}

case class WebactionSuccess(content: ApiMapValue) extends WebactionResult {
  override def isSuccess: Boolean = true
}

case class WebactionFailure(content: ApiMapValue) extends WebactionResult {
  override def isSuccess: Boolean = false
}

object Webaction {

  /**
   * Retrieves list of all available webactions
   * @param path path ofconfig, containing list of registered webactions
   * @return mapping from webaction name to its FQN
   */
  def list(path: String): Map[String, String] =
    ActorConfig.load().getConfig(path).root.unwrapped.toMap map { case (k, v) ⇒ k → v.toString }

  def list: Map[String, String] = list("modules.webactions")

  /**
   * Instantiates webaction by fully qualified class name
   * @param actionFQN fully qualified class name
   * @param system actor system, required to execute actions inside webaction
   * @return `Success(webaction)` when webaction instantiation succeeds,
   *        and `Failure(exception)` when instantiation failed by various reasons(no such class/wrong constuctor)
   */
  def webactionOf(actionFQN: String, system: ActorSystem): Try[Webaction] = Try {
    val constructor = Class.forName(actionFQN).getConstructors()(0)
    constructor.newInstance(system).asInstanceOf[Webaction]
  }

  def failure(error: String, optCause: Option[String]): WebactionFailure = {
    val m = ApiMapValueItem("error", ApiStringValue(error))
    val items = optCause map { cause ⇒
      Vector(m, ApiMapValueItem("cause", ApiStringValue(cause)))
    } getOrElse Vector(m)
    WebactionFailure(ApiMapValue(items))
  }

  def success(message: String): WebactionSuccess =
    success(ApiMapValue(Vector(ApiMapValueItem("success", ApiStringValue(message)))))

  def success(map: ApiMapValue): WebactionSuccess = WebactionSuccess(map)
}

abstract class Webaction(system: ActorSystem) {
  def uri(params: ApiMapValue): String
  def regex: String
  def complete(userId: Int, url: String): Future[WebactionResult]
}