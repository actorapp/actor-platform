package im.actor.server.api.frontend

import java.util.concurrent.atomic.AtomicLong

import scala.collection.JavaConversions._

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.github.kxbmap.configs._
import com.typesafe.config.Config
import slick.driver.PostgresDriver.api._

import im.actor.server.session.SessionRegion
import im.actor.tls.TlsContext

sealed trait EndpointType

object EndpointType {
  def fromString(str: String): Either[Exception, EndpointType] with Product with Serializable =
    str match {
      case "tcp"       ⇒ Right(EndpointTypes.Tcp)
      case "websocket" ⇒ Right(EndpointTypes.WebSocket)
      case unsupported ⇒ Left(new Exception(s"Unsupported endpoint type ${unsupported}"))
    }
}

object EndpointTypes {

  case object Tcp extends EndpointType

  case object WebSocket extends EndpointType

}

object Endpoint {
  def fromConfig(config: Config) = {
    for {
      typ ← config.get[Either[Throwable, String]]("type").right.flatMap(EndpointType.fromString).right
      host ← config.get[Either[Throwable, String]]("interface").right
      port ← config.get[Either[Throwable, Int]]("port").right
      keystore ← Right(config.opt[String]("keystore")).right
    } yield Endpoint(
      typ, host, port, keystore
    )
  }
}

case class Endpoint(typ: EndpointType, host: String, port: Int, keystore: Option[String])

object Frontend {

  import EndpointTypes._

  def start(serverConfig: Config)(
    implicit
    sessionRegion: SessionRegion,
    db:            Database,
    system:        ActorSystem,
    mat:           Materializer
  ): Unit = {
    serverConfig.getConfigList("endpoints") map Endpoint.fromConfig foreach {
      case Right(e) ⇒ startEndpoint(e, serverConfig)
      case Left(e)  ⇒ throw e
    }
  }

  def startEndpoint(endpoint: Endpoint, serverConfig: Config)(
    implicit
    sessionRegion: SessionRegion,
    db:            Database,
    system:        ActorSystem,
    mat:           Materializer
  ): Unit = {
    val kssConfig = serverConfig.getConfig("tls.keystores")

    endpoint match {
      case Endpoint(Tcp, host, port, keystore) ⇒
        val tlsContext = keystore map (TlsContext.load(kssConfig, _)) map {
          case Left(err)  ⇒ throw err
          case Right(ctx) ⇒ ctx
        }

        TcpFrontend.start(host, port, tlsContext)

      case Endpoint(WebSocket, host, port, keystore) ⇒
        val tlsContext = keystore map (TlsContext.load(kssConfig, _)) map {
          case Left(err)  ⇒ throw err
          case Right(ctx) ⇒ ctx
        }

        WsFrontend.start(host, port, tlsContext)
    }
  }
}

trait Frontend {

  private val connCounter = new AtomicLong(0L)

  protected val connIdPrefix: String

  protected def nextConnId(): String = s"conn-${connIdPrefix}-${connCounter.incrementAndGet()}"
}