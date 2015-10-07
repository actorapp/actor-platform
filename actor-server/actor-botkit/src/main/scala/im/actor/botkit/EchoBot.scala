package im.actor.botkit

import akka.actor.{ ActorSystem, Props }
import im.actor.bots.BotMessages.TextMessage

object EchoBotApp extends App {
  implicit val system = ActorSystem()

  val token = "dccac82e017ba4f2d267303e795ca689e5b2d1e8"

  system.actorOf(
    //EchoBot.props(token, RemoteBot.DefaultEndpoint),
    EchoBot.props(token, "ws://localhost:9090"),
    "EchoBot"
  )

  system.awaitTermination()
}

object EchoBot {
  def props(token: String, endpoint: String = "wss://front1-ws-mtproto-api-rev2-dev1.actor.im:443") =
    Props(classOf[EchoBot], token, endpoint)
}

final class EchoBot(token: String, endpoint: String) extends RemoteBot(token, endpoint) {
  override def onTextMessage(tm: TextMessage): Unit = {
    requestSendTextMessage(tm.sender.asOutPeer, nextRandomId(), s"Hey, here is your reply: ${tm.text}")
  }
}