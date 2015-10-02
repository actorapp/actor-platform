package im.actor.botkit

import java.net.URI

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
import scala.util.control.NoStackTrace

private[botkit] final case object ConnectionClosed

private[botkit] object WebsocketClient {
  def sourceAndSink(url: String)(implicit context: ActorRefFactory) = {
    val actor = context.actorOf(props(url))

    (Source(ActorPublisher[String](actor)), Sink(ActorSubscriber[String](actor)))
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
      case _: Http.ConnectionClosed ⇒
        onClose()
        context.stop(self)
      case e: Http.ConnectionException ⇒
        onFailure(e)
        context.stop(self)
    }

    def onMessage(frame: Frame): Unit

    def onClose(): Unit

    def onFailure(e: Http.ConnectionException): Unit
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

      override def onClose() = {
        context.parent ! ConnectionClosed
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
      log.info("Received {}", str)
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
      client ! textToSend
    case ConnectionClosed ⇒
      log.error("Connection closed")
      onErrorThenStop(new RuntimeException("Connection closed") with NoStackTrace)
    case Terminated(`client`) ⇒
      onErrorThenStop(new RuntimeException("Failed to connect") with NoStackTrace)
    case unmatched ⇒
      log.error("Unmatched {}", unmatched)
  }

  override val requestStrategy = new WatermarkRequestStrategy(Int.MaxValue)

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