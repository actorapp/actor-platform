package im.actor.server.gelf

import akka.actor.Actor
import akka.event.Logging._
import im.actor.config.ActorConfig
import org.slf4j.{ LoggerFactory ⇒ SLFLoggerFactory }
import com.typesafe.config.{ Config, ConfigFactory }

object LogType extends Enumeration {
  type LogType = Value
  val RequestReceived: LogType = Value(1)
  val SendResponse: LogType = Value(2)
  val ErrorResponse: LogType = Value(3)
  val BadRequest: LogType = Value(4)
  val BadToken: LogType = Value(5)
  val Exception: LogType = Value(6)
}

//use this in akka.logging configuration (akka logging does not accept class with default param in constructor)
class GelfLogger extends GelfLoggerInternal

//use this in manually creation, like testing
class GelfLoggerInternal(actorConfig: Config = ActorConfig.load(ConfigFactory.empty())) extends Actor {

  val logger = SLFLoggerFactory getLogger this.getClass

  def receive = {
    case InitializeLogger(_) ⇒
      logger.info("GelfLogger started")
      sender() ! LoggerInitialized
    case e @ Error(cause, logSource, logClass, message) ⇒
    case w @ Warning(logSource, logClass, message)      ⇒
    case i @ Info(logSource, logClass, message)         ⇒
    case d @ Debug(logSource, logClass, message)        ⇒
  }

}