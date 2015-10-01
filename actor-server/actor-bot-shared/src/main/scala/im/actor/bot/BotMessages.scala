package im.actor.bot

import derive.key

sealed trait BotMessage
sealed trait BotMessageIn extends BotMessage
sealed trait BotMessageOut extends BotMessage

final object BotMessages {
  final object OutPeer {
    def privat(id: Int, accessHash: Long) = OutPeer(1, id, accessHash)
    def group(id: Int, accessHash: Long) = OutPeer(2, id, accessHash)
  }
  final case class OutPeer(`type`: Int, id: Int, accessHash: Long)
  final case class UserOutPeer(id: Int, accessHash: Long)
  final case class Peer(`type`: Int, id: Int)

  sealed trait RequestBody

  final case class BotRequest(id: Long, body: RequestBody) extends BotMessageIn

  @key("SendMessage")
  final case class SendTextMessage(peer: OutPeer, randomId: Long, text: String) extends RequestBody

  sealed trait ResponseBody

  @key("Response")
  final case class BotResponse(id: Long, body: ResponseBody) extends BotMessageOut

  @key("MessageSent")
  final case class MessageSent(date: Long) extends ResponseBody

  @key("Update")
  sealed trait BotUpdate extends BotMessageOut

  @key("TextMessage")
  final case class TextMessage(peer: OutPeer, sender: UserOutPeer, date: Long, randomId: Long, text: String) extends BotUpdate

  final def isPrivate(peer: OutPeer) = peer.`type` == 1
  final def isGroup(peer: OutPeer) = peer.`type` == 2
}