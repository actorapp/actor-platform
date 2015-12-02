package im.actor.botkit

import akka.actor.{ ActorSystem, Props }
import im.actor.bots.BotMessages.{ RawUpdate, Message, TextMessage }

import scala.concurrent.Await
import scala.concurrent.duration._

object EchoBotApp extends App {
  implicit val system = ActorSystem()

  val token = "f31922d943395309b61def3dfb7fd43b9158717c"

  system.actorOf(
    //EchoBot.props(token, RemoteBot.DefaultEndpoint),
    EchoBot.props(token, "ws://localhost:9090"),
    "EchoBot"
  )

  Await.result(system.whenTerminated, Duration.Inf)
}

object EchoBot {
  def props(token: String, endpoint: String = "wss://front1-ws-mtproto-api-rev2-dev1.actor.im:443") =
    Props(classOf[EchoBot], token, endpoint)
}

final class EchoBot(token: String, endpoint: String) extends RemoteBot(token, endpoint) {
  override def onMessage(m: Message): Unit = {
    m.message match {
      case TextMessage(text, ext) ⇒
        val name = getUser(m.sender.id).name

        requestSendMessage(m.sender, nextRandomId(), TextMessage(s"Hey $name, here is your reply: $text", ext))
      case notAText ⇒ requestSendMessage(m.sender, nextRandomId(), notAText)
    }
  }

  override def onRawUpdate(u: RawUpdate): Unit = {}
}