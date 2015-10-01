package im.actor.bot.remote

import akka.actor.{ Props, ActorSystem }
import im.actor.bot.BotMessages.TextMessage

object EchoBotApp extends App {
  implicit val system = ActorSystem()

  val token = "e296e29479a6933923119bec40ff46b408ff4d34"

  system.actorOf(
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
  override protected def onTextMessage(tm: TextMessage): Unit = {
    sendTextMessage(outPeer(tm.sender), s"Hey, here is your reply: ${tm.text}")
  }
}