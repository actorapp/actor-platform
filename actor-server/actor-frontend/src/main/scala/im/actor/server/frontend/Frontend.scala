package im.actor.server.frontend

import java.net.InetAddress
import java.util.concurrent.atomic.AtomicLong

import kamon.Kamon

import scala.collection.JavaConversions._

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.github.kxbmap.configs.syntax._
import com.typesafe.config.Config
import slick.driver.PostgresDriver.api._

import im.actor.server.session.SessionRegion

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
      keystore ← Right(config.getOpt[String]("keystore")).right
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
    val serverKeys = ServerKey.loadKeysFromConfig(serverConfig).get

    endpoint match {
      case Endpoint(Tcp, host, port, keystore) ⇒
        TcpFrontend.start(host, port, serverKeys)
      case Endpoint(WebSocket, host, port, keystore) ⇒
        WsFrontend.start(host, port, serverKeys)
    }
  }
}

abstract class Frontend(connIdPrefix: String) {
  private val metricTags = Map("type" → connIdPrefix)

  private val connectionTime = Kamon.metrics.histogram("connection.time", metricTags, kamon.metric.instrument.Time.Milliseconds)
  private val connectionCount = Kamon.metrics.minMaxCounter("connection_count", metricTags)

  private val connCounter = new AtomicLong(0L)

  private def nextConnId(): String = s"conn-$connIdPrefix-${connCounter.incrementAndGet()}"

  protected def mtProtoBlueprint(serverKeys: Seq[ServerKey], remoteAddress: InetAddress)(implicit sessionRegion: SessionRegion, system: ActorSystem): MTProtoBlueprint.MTProtoFlow =
    MTProtoBlueprint(nextConnId(), connectionTime, connectionCount, serverKeys, remoteAddress)
}