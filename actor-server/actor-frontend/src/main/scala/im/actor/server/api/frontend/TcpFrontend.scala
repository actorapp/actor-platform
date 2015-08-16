package im.actor.server.api.frontend

import akka.actor._
import akka.event.Logging
import akka.stream.Materializer
import akka.stream.scaladsl._
import slick.driver.PostgresDriver.api.Database

import im.actor.server.session.SessionRegion
import im.actor.server.tls.{ Tls, TlsContext }

object TcpFrontend extends Frontend {
  override protected val connIdPrefix = "tcp"

  def start(host: String, port: Int, tlsContext: Option[TlsContext])(
    implicit
    sessionRegion: SessionRegion,
    system:        ActorSystem,
    mat:           Materializer
  ): Unit = {
    val log = Logging.getLogger(system, this)

    Tcp().bind(host, port)
      .to(Sink.foreach {
        case (Tcp.IncomingConnection(localAddress, remoteAddress, flow)) ⇒
          log.debug("New TCP connection from {}", localAddress)

          val mtProto = MTProtoBlueprint(nextConnId())
          val connFlow = tlsContext map (Tls.connection(_, flow)) getOrElse (flow) join mtProto
          connFlow.run()
      })
      .run()

  }
}
