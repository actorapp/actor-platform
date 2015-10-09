package im.actor.botkit

import akka.actor.{ ActorSystem, Props }
import im.actor.bots.BotMessages.TextMessage

object EchoBotApp extends App {
  implicit val system = ActorSystem()

  val token = "d201a56591397a03b61a5f7a803cf5020d6661c7"

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
    val name = getUser(tm.sender.id).name

    requestSendTextMessage(tm.sender.asOutPeer, nextRandomId(), s"Hey $name, here is your reply: ${tm.text}")
  }
}