package im.actor.server.activation.common

import akka.actor.ActorSystem
import im.actor.config.ActorConfig

import scala.collection.JavaConversions._
import scala.util.{ Failure, Success, Try }

object ActivationProviders {
  val Sms = "sms"
  val Smtp = "smtp"
  val Call = "call"
  val InApp = "in-app"

  /**
   * TODO: describe doc
   *
   * @param system
   * @return
   */
  def getProviders()(implicit system: ActorSystem): Map[String, ActivationProvider] = {
    val providersConfig = ActorConfig.load().getConfig("services.activation.providers")
    val configMap = providersConfig.root.unwrapped.toMap

    val reverseAcc = Map.empty[String, List[String]].withDefaultValue(List.empty[String])
    // this is made to avoid duplicate instantiation of same providers
    val reverseMap = (configMap foldLeft reverseAcc) {
      case (acc, (activationType, value)) ⇒
        val className = value.toString
        acc.updated(className, activationType :: acc(className))
    }

    reverseMap flatMap {
      case (className, activationTypes) ⇒
        providerOf(className, system) match {
          case Success(instance) ⇒ (activationTypes map { _ → instance }).toMap
          case Failure(e) ⇒
            system.log.warning("Failed to instantiate code provider: {}", className)
            Map.empty[String, ActivationProvider]
        }
    }
  }

  private def providerOf(fqcn: String, system: ActorSystem): Try[ActivationProvider] = {
    for {
      constructor ← Try(Class.forName(fqcn).asSubclass(classOf[ActivationProvider]).getConstructor(classOf[ActorSystem]))
    } yield constructor.newInstance(system)
  }
}
