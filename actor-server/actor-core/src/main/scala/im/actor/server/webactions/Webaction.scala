package im.actor.server.webactions

import akka.actor.ActorSystem
import im.actor.api.rpc.collections.{ ApiStringValue, ApiMapValueItem, ApiMapValue }
import im.actor.config.ActorConfig

import scala.concurrent.Future
import scala.collection.JavaConversions._
import scala.util.Try

object Webaction {

  /**
   * Retrieves list of all available webactions
   * @param path path ofconfig, containing list of registered webactions
   * @return mapping from webaction name to its FQN
   */
  def list(path: String): Map[String, String] =
    ActorConfig.load().getConfig(path).root.unwrapped.toMap map { case (k, v) ⇒ k → v.toString }

  def list: Map[String, String] = list("enabled-modules.webactions")

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

  def failure(error: String, optCause: Option[String]): ApiMapValue = {
    val m = ApiMapValueItem("error", ApiStringValue(error))
    val items = optCause map { cause ⇒
      Vector(m, ApiMapValueItem("cause", ApiStringValue(cause)))
    } getOrElse Vector(m)
    ApiMapValue(items)
  }

  def success(message: String): ApiMapValue =
    ApiMapValue(Vector(ApiMapValueItem("success", ApiStringValue(message))))
}

abstract class Webaction(system: ActorSystem) {
  def uri(params: ApiMapValue): String
  def regex: String
  def complete(userId: Int, url: String): Future[ApiMapValue]
}