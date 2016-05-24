package im.actor.server.gelf

import java.net.InetSocketAddress
import akka.actor._
import akka.event.Logging._
import akka.io.{ IO, Tcp }
import akka.testkit.{ TestKit }
import com.typesafe.config.{ Config, ConfigFactory }
import im.actor.api.rpc.{ RpcRequest }
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
    // do some logging or setup ...

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
              case _           ⇒ i._1 → i._2.toString //Maybe it is integer
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

}
