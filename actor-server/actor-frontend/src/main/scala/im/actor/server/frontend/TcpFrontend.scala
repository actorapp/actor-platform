package im.actor.server.frontend

import java.net.InetAddress

import akka.actor._
import akka.event.Logging
import akka.stream.Materializer
import akka.stream.scaladsl._
import im.actor.server.session.SessionRegion

import scala.concurrent.duration._

object TcpFrontend extends Frontend("tcp") {
  val IdleTimeout = 30.minutes

  def start(host: String, port: Int, serverKeys: Seq[ServerKey])(
    implicit
    sessionRegion: SessionRegion,
    system:        ActorSystem,
    mat:           Materializer
  ): Unit = {
    val log = Logging.getLogger(system, this)

    Tcp().bind(host, port, idleTimeout = IdleTimeout)
      .to(Sink.foreach {
        case (conn @ Tcp.IncomingConnection(localAddress, remoteAddress, flow)) ⇒
          log.debug("New TCP connection from {}", localAddress)

          val mtProto = mtProtoBlueprint(serverKeys, remoteAddress.getAddress())
          flow.joinMat(mtProto)(Keep.right).run()
      })
      .run()
  }
}
