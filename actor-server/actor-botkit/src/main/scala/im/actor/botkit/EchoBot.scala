package im.actor.botkit

import akka.actor.{ActorSystem, Props}
import im.actor.bot.BotMessages.TextMessage

object EchoBotApp extends App {
  implicit val system = ActorSystem()

  val token = "68d336e59a378d405e652274c36f383835e9b21f"

  system.actorOf(
    EchoBot.props(token, RemoteBot.DefaultEndpoint),
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