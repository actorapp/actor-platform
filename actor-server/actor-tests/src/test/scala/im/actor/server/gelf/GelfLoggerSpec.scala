package im.actor.server.gelf

import java.net.InetSocketAddress
import akka.actor._
import akka.event.Logging._
import akka.io.{ IO, Tcp }
import akka.testkit.{ TestActorRef, TestKit }
import com.typesafe.config.{ Config, ConfigFactory }
import im.actor.api.rpc.auth.RequestValidateCode
import im.actor.api.rpc.messaging.{ ApiTextMessage, RequestSendMessage }
import im.actor.api.rpc.misc.ResponseBool
import im.actor.api.rpc.peers.{ ApiOutPeer, ApiPeerType }
import im.actor.api.rpc.{ RpcError, RpcRequest, RpcOk, Request }
import im.actor.config.ActorConfig
import im.actor.server.gelf.DummyGraylogListener.{ TActor, GelfMessage }
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import play.api.libs.json._
import play.api.libs.functional.syntax._

object DummyGraylogListener {

  case class GelfMessage(short_message: String, level: Int,
                         request_name:   Option[String],
                         response_name:  Option[String],
                         exception_name: Option[String],
                         fields:         Option[Map[String, String]])

  case class TActor(testActor: ActorRef)

}

class DummyGraylogListener(host: String, port: Int) extends Actor with ActorLogging {

  import Tcp._
  import context.system

  IO(Tcp) ! Bind(self, new InetSocketAddress(host, port))

  var tester: Option[ActorRef] = None

  implicit val gelfReader: Reads[GelfMessage] = (
    (__ \ "short_message").read[String] and
    (__ \ "level").read[Int] and
    (__ \ "_request_name").readNullable[String] and
    (__ \ "_response_name").readNullable[String] and
    (__ \ "_exception_name").readNullable[String] and
    (__ \ "").readNullable[Map[String, String]] // wil be set later
  )(GelfMessage.apply _)

  def receive = {
    case TActor(actor)           ⇒ tester = Option(actor)

    case b @ Bound(localAddress) ⇒

    case CommandFailed(_: Bind)  ⇒ context stop self

    case c @ Connected(remote, local) ⇒
      val connection = sender()
      connection ! Register(self)

    case Received(data) ⇒
      tester match {
        case Some(tActor) ⇒
          //convert byte to string and remove control characters
          val body = data.decodeString("US-ASCII").replaceAll("\\p{C}", "")
          log.debug(body)
          val msg = Json.parse(body).as[JsObject].fieldSet.toMap.map(i ⇒
            i._2 match {
              case _: JsString ⇒ i._1 → i._2.as[String] //String
              case _           ⇒ i._1 → i._2.toString //Maybe it was integer
            })
          val gelfMessage: GelfMessage = Json.parse(body).as[GelfMessage]
          tActor ! gelfMessage.copy(fields = Some(msg))
        case None ⇒ log.error("testActor was not set")
      }
    case PeerClosed ⇒ context stop self
  }

}

//This subscriber avoid akka DefaultLogger to subscribe so we will not see the DummyActor test log in the console
class NoLogger extends Actor {
  override def receive: Receive = {
    case InitializeLogger(_) ⇒ sender() ! LoggerInitialized
    case event: LogEvent     ⇒
  }
}

object ActorSpec {

  private[this] def defaultSystemName = "actor-server-test"

  val maxPort = 65535
  val minPort = 1025
  val port = scala.util.Random.nextInt(maxPort - minPort + 1) + minPort
  val dummyServer = ActorSystem().actorOf(Props(classOf[DummyGraylogListener], "localhost", port))

  def createSystem(systemName: String = defaultSystemName): ActorSystem = {
    createSystem(systemName, createConfig(systemName, ConfigFactory.empty()))
  }

  def createSystem(config: Config): ActorSystem = {
    createSystem(defaultSystemName, createConfig(defaultSystemName, config))
  }

  def createSystem(systemName: String, config: Config): ActorSystem = {
    ActorSystem(systemName, config)
  }

  def createConfig(systemName: String, initialConfig: Config): Config = {

    val logger = "im.actor.server.gelf.NoLogger"

    initialConfig
      .withFallback(
        ConfigFactory.parseString(
          s"""
              akka.loggers = [$logger]
        """
        )
      )
  }
}

abstract class ActorSuite(system: ActorSystem = {
                            ActorSpec.createSystem()
                          })
  extends TestKit(system)
  with Suite
  with FlatSpecLike
  with BeforeAndAfterAll
  with Matchers
  with ScalaFutures {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }
}

final class UnsupportedRequest extends RpcRequest {
  override def toByteArray: Array[Byte] = Array.empty

  override def getSerializedSize: Int = 0

  override val header: Int = 0
}

class GelfLoggerSpec extends ActorSuite {
  behavior of "GelfLogger"

  "Exception detail with default 0" should "not send any exception log" in service.exceptionOff()
  "Exception detail with default 1" should "send only exception name" in service.exceptionNoParam
  "Exception detail with default 2" should "send only exception name and stacktrace" in service.exceptionWithParam()
  "IllegalArgumentException with custom detail 0 and default 1" should "not send this exception but send other exception name" in service.illegalExceptionOff()
  "RequestValidateCode with WARNING level" should "respond level 4" in service.requestValidateCode()
  "Request detail with default 0" should "not send any logs" in service.requestOff()
  "Request with {type1:app1} additional fields" should "send app1" in service.additionalFields()
  "RequestNotSupported RpcError with level NOTICE" should "send BadRequest log with level 5 and empty request_name" in service.notSupportedError()
  "UnsupportedRequest with level WARNING" should "send BadRequest log with level 4 and UnsupportedRequest request name" in service.unsupportedReq()
  "BadToken with level WARNING" should "send BadToken log with level 4" in service.badToken()
  "Error with level ALERT" should "send Response Error log with level 1 and RpcInternalError response name" in service.error()

  val actorRef = TestActorRef(new Actor with DiagnosticActorLogging {

    //These logs will be send to GrayLogger subscriber with eventStream
    def receive = {
      case "RequestValidateCode" ⇒
        aroundLog(
          "type" → LogType.RequestReceived,
          "request" → Request(RequestValidateCode("", ""))
        ) {
            log.debug("RequestValidateCode")
          }
      case "RequestSendMessage" ⇒
        aroundLog(
          "type" → LogType.RequestReceived,
          "request" → Request(RequestSendMessage(ApiOutPeer(ApiPeerType.Private, 0, 0L), 0, ApiTextMessage("test", Vector.empty, None), None, None))
        ) {
            log.debug("RequestSendMessage")
          }
      case "ResponseBool" ⇒
        aroundLog(
          "type" → LogType.SendResponse,
          "response" → RpcOk(ResponseBool(true))
        ) {
            log.debug("ResponseBool")
          }
      case "NotSupportedError" ⇒
        aroundLog(
          "type" → LogType.SendResponse,
          "response" → RpcErrors.RequestNotSupported
        ) {
            log.debug("NotSupportedError")
          }
      case "UnsupportedReq" ⇒
        aroundLog(
          "type" → LogType.BadRequest,
          "request" → Request(new UnsupportedRequest())
        ) {
            log.error("Failed to process request")
          }
      case "BadToken" ⇒
        aroundLog(
          "client_address" → "",
          "type" → LogType.BadToken,
          "authId" → 1111L,
          "sessionId" → 2222L
        ) {
            log.warning("authId has changed")
          }
      case "ErrorResponse" ⇒
        aroundLog(
          "type" → LogType.SendResponse,
          "response" → RpcError(405, "Error", "Some Error", canTryAgain = true, data = None)
        ) {
            log.debug("ErrorResponse")
          }
      case "IllegalArgumentException" ⇒
        log.error(new IllegalArgumentException(), "Log Exception")

      case "NullPointerException" ⇒
        log.error(new NullPointerException(), "Log Exception")
    }

    def aroundLog(mdc: (String, Any)*)(logger: ⇒ Unit): Unit = {
      try {
        log.mdc(log.mdc ++ mdc)
        logger
      } finally {
        log.mdc(log.mdc -- mdc.map(_._1))
      }
    }
  })

  override def beforeAll(): Unit = {
    ActorSpec.dummyServer ! TActor(testActor)
  }

  //DEBUG = 7
  //INFO = 6
  //NOTICE = 5
  //WARNING = 4
  //ERROR = 3
  //CRITICAL = 2
  //ALERT = 1
  //EMERGENCY = 0

  //TODO refactor: create object for each category
  object service {
    def gelfContext(conf: String)(command: ⇒ Unit): Unit = {
      val listener = TestActorRef(new GelfLoggerInternal(
        ConfigFactory.parseString(
          s"""
             modules.soc.gelf.type="TCP"
             modules.soc.gelf.host="localhost"
             modules.soc.gelf.port= ${ActorSpec.port}
        """
        )
          .withFallback(ConfigFactory.parseString(conf))
          .withFallback(ActorConfig.load())
      ))
      system.eventStream.subscribe(listener, classOf[InitializeLogger])
      system.eventStream.subscribe(listener, classOf[Debug])
      system.eventStream.subscribe(listener, classOf[Info])
      system.eventStream.subscribe(listener, classOf[Error])
      system.eventStream.subscribe(listener, classOf[Warning])

      command

      system.eventStream.unsubscribe(listener)
    }

    def requestValidateCode(): Unit = {
      val settings =
        s"""
           modules.soc.gelf.level.RequestReceived=WARNING
           modules.soc.gelf.request-detail.default=2

        """

      gelfContext(settings) {
        actorRef ! "RequestValidateCode"

        expectMsgPF() {
          case GelfMessage("Request Received", 4, Some("RequestValidateCode"), _, _, _) ⇒
        }

      }
    }

    def requestOff(): Unit = {
      val settings =
        s"""
           modules.soc.gelf.request-detail.default=0
        """

      gelfContext(settings) {
        actorRef ! "RequestValidateCode"
        expectNoMsg()
      }
    }

    def requestNoParam(): Unit = {
      val settings =
        s"""
           modules.soc.gelf.request-detail.default=1
        """

      gelfContext(settings) {
        actorRef ! "RequestValidateCode"
        expectNoMsg()
      }
    }

    def requestValidateCodeOff(): Unit = {
      val settings =
        s"""
           modules.soc.gelf.request-detail.customized:{RequestValidateCode: 0}
        """

      gelfContext(settings) {
        actorRef ! "RequestValidateCode"
        expectNoMsg()

        actorRef ! "RequestSendMessage"
        expectMsgPF() {
          case GelfMessage("Request Received", 6, Some("RequestSendMessage"), _, _, _) ⇒
        }
      }
    }

    def requestSendMessageNoParam(): Unit = {
      val settings =
        s"""
           modules.soc.gelf.request-detail.customized:{RequestSendMessage: 1}
        """

      gelfContext(settings) {
        actorRef ! "RequestSendMessage"
        expectNoMsg()
      }
    }

    def additionalFields(): Unit = {
      val settings =
        s"""
           modules.soc.gelf.additional-fields:{type1:"app1", type2:"app2" }
        """
      gelfContext(settings) {
        actorRef ! "RequestSendMessage"
        val gelfMesaage = expectMsgClass(classOf[GelfMessage])

        gelfMesaage.fields.foreach(_.get("_type1") should be(Some("app1")))
        gelfMesaage.fields.foreach(_.get("_type2") should be(Some("app2")))

      }
    }

    def responseOff(): Unit = {
      val settings =
        s"""
           modules.soc.gelf.response-detail.default=0
        """

      gelfContext(settings) {
        actorRef ! "ResponseBool"
        expectNoMsg()
      }
    }

    //TODO
    def responseNoParam(): Unit = {

    }

    //TODO
    def responseWithParam(): Unit = {

    }

    //TODO
    def customResponseOff(): Unit = {

    }

    //TODO
    def customResponseNoParam(): Unit = {

    }

    //TODO
    def customResponseWithParam(): Unit = {

    }

    def notSupportedError(): Unit = {
      val settings =
        s"""
           modules.soc.gelf.level.BadRequest=NOTICE
        """

      gelfContext(settings) {
        actorRef ! "NotSupportedError"
        expectMsgPF() {
          case GelfMessage("Bad Request", 5, Some(""), _, _, _) ⇒
        }

      }
    }

    def unsupportedReq(): Unit = {
      val settings =
        s"""
           modules.soc.gelf.level.BadRequest=WARNING
        """

      gelfContext(settings) {
        actorRef ! "UnsupportedReq"
        expectMsgPF() {
          case GelfMessage("Bad Request", 4, Some("UnsupportedRequest"), _, _, _) ⇒
        }

      }
    }

    def badToken(): Unit = {
      val settings =
        s"""
           modules.soc.gelf.level.BadToken=WARNING
        """

      gelfContext(settings) {
        actorRef ! "BadToken"
        expectMsgPF() {
          case GelfMessage("Bad Token", 4, _, _, _, fields) ⇒
            fields.foreach(_.get("_auth_id") should be(Some("1111")))
            fields.foreach(_.get("_session_id") should be(Some("2222")))
        }

      }
    }

    def error(): Unit = {
      val settings =
        s"""
           modules.soc.gelf.level.ErrorResponse=ALERT
        """

      gelfContext(settings) {
        actorRef ! "ErrorResponse"
        expectMsgPF() {
          case GelfMessage("Error Response", 1, _, Some("RpcError"), _, fields) ⇒
            fields.foreach {
              _.get("_response") should not be None
            }
        }
      }
    }

    //TODO
    def internalError(): Unit = {

    }

    def exceptionOff(): Unit = {
      val settings =
        s"""
           modules.soc.gelf.exception-detail.default=0
        """

      gelfContext(settings) {
        actorRef ! "IllegalArgumentException"
        expectNoMsg()
      }
    }

    def exceptionNoParam(): Unit = {
      val settings =
        s"""
           modules.soc.gelf.exception-detail.default=1
           modules.soc.gelf.level.Exception=INFO
        """

      gelfContext(settings) {
        actorRef ! "IllegalArgumentException"
        expectMsgPF() {
          case GelfMessage("Exception", 6, _, _, Some("IllegalArgumentException"), fields) ⇒
            fields.foreach(_.get("_stacktrace") should be(None))
        }

      }
    }

    def exceptionWithParam(): Unit = {
      val settings =
        s"""
           modules.soc.gelf.exception-detail.default=2
           modules.soc.gelf.level.Exception=ERROR
        """

      gelfContext(settings) {
        actorRef ! "IllegalArgumentException"
        expectMsgPF() {
          case GelfMessage("Exception", 3, _, _, Some("IllegalArgumentException"), fields) ⇒
            fields.foreach(_.get("_stacktrace") should not be None)
        }

      }
    }

    def illegalExceptionOff(): Unit = {
      val settings =
        s"""
           modules.soc.gelf.exception-detail.customized:{IllegalArgumentException: 0}
           modules.soc.gelf.exception-detail.default=1
        """

      gelfContext(settings) {
        actorRef ! "IllegalArgumentException"
        expectNoMsg()

        actorRef ! "NullPointerException"
        expectMsgPF() {
          case GelfMessage("Exception", _, _, _, Some("NullPointerException"), fields) ⇒
            fields.foreach(_.get("_stacktrace") should be(None))
        }
      }
    }

    //TODO
    def customeExceptionNoParam(): Unit = {

    }

    //TODO
    def customExceptionWithParam(): Unit = {

    }

    //TODO send bad log and see result of GelfLogger(e.g. don't set response mdc field or set wrong one, etc )

  }

}
