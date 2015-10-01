package im.actor.bot

trait BotBase {
  import BotMessages._

  protected def onTextMessage(tm: TextMessage): Unit

  protected def sendTextMessage(peer: OutPeer, text: String): Unit

  protected def outPeer(userOutPeer: UserOutPeer) = OutPeer(1, userOutPeer.id, userOutPeer.accessHash)
}