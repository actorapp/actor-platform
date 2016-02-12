package im.actor.botkit

import java.net.URI

import akka.NotUsed
import akka.actor._
import akka.io.IO
import akka.stream.actor._
import akka.stream.scaladsl.{ Sink, Source }
import spray.can.server.UHttp
import spray.can.websocket.frame.{ Frame, TextFrame }
import spray.can.{ Http, websocket }
import spray.http.{ HttpHeaders, HttpMethods, HttpRequest }
import spray.io.ServerSSLEngineProvider

import scala.annotation.tailrec
import scala.concurrent.duration._
import scala.util.control.NoStackTrace

private[botkit] case object ConnectionClosed

private[botkit] object WebsocketClient {
  def sourceAndSink(url: String)(implicit context: ActorRefFactory): (Source[String, NotUsed], Sink[String, NotUsed]) = {
    val actor = context.actorOf(props(url))

    (Source.fromPublisher(ActorPublisher[String](actor)), Sink.fromSubscriber(ActorSubscriber[String](actor)))
  }

  def props(url: String) = Props(classOf[WebsocketClient], url)
}

private[botkit] final class WebsocketClient(url: String)
  extends ActorPublisher[String]
  with ActorSubscriber
  with ActorLogging {
  import context.system

  implicit def sslEngineProvider: ServerSSLEngineProvider = {
    ServerSSLEngineProvider { engine ⇒
      engine.setEnabledCipherSuites(Array("TLS_RSA_WITH_AES_256_CBC_SHA"))
      engine.setEnabledProtocols(Array("SSLv3", "TLSv1"))
      engine
    }
  }

  abstract class WebSocketClient(connect: Http.Connect, val upgradeRequest: HttpRequest) extends websocket.WebSocketClientWorker {
    IO(UHttp) ! connect

    def businessLogic: Receive = {
      case frame: Frame ⇒
        onMessage(frame)
      case str: String ⇒
        connection ! TextFrame(str)
      case spray.can.websocket.UpgradedToWebSocket ⇒
        self ! spray.io.ConnectionTimeouts.SetIdleTimeout(1.hour)
      case event: Http.ConnectionClosed ⇒
        onClose(event)
        context.stop(self)
      case e: Http.ConnectionException ⇒
        onFailure(e)
        context.stop(self)
    }

    def onMessage(frame: Frame): Unit

    def onClose(event: Http.ConnectionClosed): Unit

    def onFailure(e: Http.ConnectionException): Unit

    override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
      log.error(reason, "actor failed, message: {}", message)
      super.preRestart(reason, message)
    }
  }

  val uri = new URI(url)
  val ssl = uri.getScheme == "wss"
  val host = uri.getHost
  val port = uri.getPort match {
    case -1 ⇒ if (ssl) 443 else 80
    case p  ⇒ p
  }

  val agent = "spray-websocket-client" + (if (ssl) "-ssl" else "-basic")
  val headers = List(
    HttpHeaders.Host(host, port),
    HttpHeaders.Connection("Upgrade"),
    HttpHeaders.RawHeader("Upgrade", "websocket"),
    HttpHeaders.RawHeader("Sec-WebSocket-Version", "13"),
    HttpHeaders.RawHeader("Sec-WebSocket-Key", "x3JJHMbDL1EzLkh9GBhXDw=="),
    HttpHeaders.RawHeader("Sec-WebSocket-Extensions", "permessage-deflate")
  )

  val connect = Http.Connect(host, port, ssl)

  log.info("Connecting to {}", url)

  val req = HttpRequest(HttpMethods.GET, spray.http.Uri(Option(uri.getPath).getOrElse("/")), headers)
  val client = context.actorOf(Props(
    new WebSocketClient(connect, req) {
      override def onMessage(frame: Frame) = {
        context.parent ! frame
      }

      override def onClose(e: Http.ConnectionClosed) = {
        context.parent ! e
      }

      override def onFailure(e: Http.ConnectionException) = {
        context.parent ! Status.Failure(e)
      }
    }
  ), "connector")
  context watch client

  private var receivedBuf = Vector.empty[String]

  def receive = {
    case TextFrame(text) ⇒
      val str = text.decodeString("UTF-8")
      log.info("<< {}", str)
      if (receivedBuf.isEmpty && totalDemand > 0)
        onNext(str)
      else {
        receivedBuf :+= str
        deliverBuf()
      }
    case frame: Frame ⇒
      log.info("Unsupported frame {}", frame)
    case ActorPublisherMessage.Request(_) ⇒
      deliverBuf()
    case ActorPublisherMessage.Cancel ⇒
      context.stop(self)
    case ActorSubscriberMessage.OnNext(textToSend: String) ⇒
      log.info(">> {}", textToSend)
      client ! textToSend
    case e: Http.ConnectionClosed ⇒
      onErrorThenStop(WebsocketClientEvents.ConnectionClosed(e))
    case Terminated(`client`) ⇒
      onErrorThenStop(WebsocketClientEvents.FailedToConnect)
    case unmatched ⇒
      log.error("Unmatched {}", unmatched)
  }

  override val requestStrategy = new WatermarkRequestStrategy(Int.MaxValue)

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    log.error(reason, "actor failed, message: {}", message)
    super.preRestart(reason, message)
  }

  @tailrec final def deliverBuf(): Unit =
    if (totalDemand > 0) {
      /*
       * totalDemand is a Long and could be larger than
       * what buf.splitAt can accept
       */
      if (totalDemand <= Int.MaxValue) {
        val (use, keep) = receivedBuf.splitAt(totalDemand.toInt)
        receivedBuf = keep
        use foreach onNext
      } else {
        val (use, keep) = receivedBuf.splitAt(Int.MaxValue)
        receivedBuf = keep
        use foreach onNext
        deliverBuf()
      }
    }
}

abstract class WebsocketClientException(message: String) extends RuntimeException(message) with NoStackTrace

object WebsocketClientEvents {
  case object FailedToConnect extends WebsocketClientException("Failed to connect")
  case class ConnectionClosed(e: Http.ConnectionClosed) extends WebsocketClientException(s"Connection closed: ${e.getErrorCause}")
}