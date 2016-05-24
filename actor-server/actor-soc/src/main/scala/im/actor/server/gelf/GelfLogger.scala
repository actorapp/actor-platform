package im.actor.server.gelf

import java.net.{ InetAddress, InetSocketAddress }

import akka.actor.Actor
import akka.event.Logging._
import im.actor.config.ActorConfig
import org.graylog2.gelfclient.transport.GelfTransport
import org.graylog2.gelfclient._
import collection.JavaConversions._
import org.slf4j.{ LoggerFactory ⇒ SLFLoggerFactory }
import com.typesafe.config.{ Config, ConfigFactory }
import play.api.libs.json.{ JsObject, Json }

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

  val typ = actorConfig.getConfig("modules.logging.gelf").getString("type")
  val host = actorConfig.getConfig("modules.logging.gelf").getString("host")
  val port = actorConfig.getConfig("modules.logging.gelf").getInt("port")
  val rdelay = actorConfig.getConfig("modules.logging.gelf").getInt("rdelay")
  val badTokenLevel = actorConfig.getConfig("modules.logging.gelf.level").getString("BadToken")
  val badRequestLevel = actorConfig.getConfig("modules.logging.gelf.level").getString("BadRequest")
  val requestReceivedLevel = actorConfig.getConfig("modules.logging.gelf.level").getString("RequestReceived")
  val sendResponseLevel = actorConfig.getConfig("modules.logging.gelf.level").getString("SendResponse")
  val errorResponseLevel = actorConfig.getConfig("modules.logging.gelf.level").getString("ErrorResponse")
  val exceptionLevel = actorConfig.getConfig("modules.logging.gelf.level").getString("Exception")
  val addFields = actorConfig.getConfig("modules.logging.gelf.additional-fields").entrySet()
  val requestDefault = actorConfig.getConfig("modules.logging.gelf.request-detail").getInt("default")
  val responseDefault = actorConfig.getConfig("modules.logging.gelf.response-detail").getInt("default")
  val exceptionDefault = actorConfig.getConfig("modules.logging.gelf.exception-detail").getInt("default")

  val requestCustomized = actorConfig.getConfig("modules.logging.gelf.request-detail.customized").entrySet()
    .map(m ⇒ (m.getKey, m.getValue.unwrapped().asInstanceOf[Int])).toMap

  val responseCustomized = actorConfig.getConfig("modules.logging.gelf.response-detail.customized").entrySet()
    .map(m ⇒ (m.getKey, m.getValue.unwrapped().asInstanceOf[Int])).toMap

  val exceptionCustomized = actorConfig.getConfig("modules.logging.gelf.exception-detail.customized").entrySet()
    .map(m ⇒ (m.getKey, m.getValue.unwrapped().asInstanceOf[Int])).toMap

  val config: GelfConfiguration = new GelfConfiguration(new InetSocketAddress(host, port))
    .transport(GelfTransports.valueOf(typ))
    .queueSize(512)
    .connectTimeout(5000)
    .reconnectDelay(rdelay)
    .tcpNoDelay(true)
    .sendBufferSize(32768)

  var transport: GelfTransport = GelfTransports.create(config)
  val builder: CGMessageBuilder = new CGMessageBuilder("", InetAddress.getLocalHost.getHostName)
    .additionalFields(addFields.map(m ⇒ (m.getKey, m.getValue.unwrapped().toString)).toMap)

  def receive = {
    case InitializeLogger(_) ⇒
      logger.info("GelfLogger started")
      sender() ! LoggerInitialized
    case e @ Error(cause, logSource, logClass, message) ⇒
    case w @ Warning(logSource, logClass, message)      ⇒
    case i @ Info(logSource, logClass, message)         ⇒
    case d @ Debug(logSource, logClass, message)        ⇒
  }



  def sendRPCLog(level: String, detail: Int, fields: Map[String, String], params: Map[String, String]): Unit = {
    detail match {
      case 0 ⇒
      case 1 ⇒
        sendMsg(level, fields) //TODO read name from params
      case 2 ⇒ sendMsg(level, fields ++ params)
      case _ ⇒ logger.warn("Out of range")
    }
  }

  def sendMsg(level: String, additionalFields: Map[String, String]): Unit = {
    val msg = builder
      .level(GelfMessageLevel.valueOf(level))
      .additionalFields(mapAsJavaMap(additionalFields))
      .build()

    if (!transport.trySend(msg)) {
      logger.warn("There isn't enough room in the queue for sending gelf log message")
    }

    //Remove the last added fields because builder is shared between all log events
    builder.removeFields(additionalFields.keySet)
  }

  //TODO make two method. 1: return map of params, 2: create gelf format from that map
  def fetchPar(input: String): Map[String, String] = {
    val jsonValue = try {
      Some(Json.parse(input))
    } catch {
      case e: Exception ⇒
        None
    }
    jsonValue match {
      case Some(jsObj) ⇒
        jsObj.asOpt[JsObject] match {
          case Some(json) ⇒
            var i = 0
            val r = for ((k, v) ← json.fieldSet)
              yield ("request_param" + {
                i += 1
                i
              }, s"{$k : $v}")
            r.toMap
          case None ⇒ Map()
        }
      case None ⇒ Map()
    }
  }


}

class CGMessageBuilder(val message: String, val host: String) extends GelfMessageBuilder(message, host) {
  val logger = SLFLoggerFactory getLogger classOf[CGMessageBuilder]

  /**
   * fields property is private so we have to use reflection
   */
  def getFields: java.util.Map[String, Object] = {
    try {
      val field = this.getClass.getSuperclass.getDeclaredField("fields")
      field.setAccessible(true)
      field.get(this).asInstanceOf[java.util.Map[String, Object]]
    } catch {
      case e: Exception ⇒
        logger.error(e.getMessage)
        new java.util.HashMap[String, Object]()
    }
  }

  /**
   * Remove additional fields
   */
  def removeFields(keys: Set[String]): Unit = {
    getFields.keySet().removeAll(setAsJavaSet(keys))
  }

  /**
   * Scala version of additionalFields
   */
  def additionalFields(additionalFields: Map[String, Object]): CGMessageBuilder = {
    super.additionalFields(mapAsJavaMap(additionalFields)).asInstanceOf[CGMessageBuilder]
  }
}