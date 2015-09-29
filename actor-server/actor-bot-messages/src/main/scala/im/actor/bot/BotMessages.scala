package im.actor.bot

import derive.key

sealed trait BotMessage

final object BotMessages {
  final object OutPeer {
    def privat(id: Int, accessHash: Long) = OutPeer(1, id, accessHash)
    def group(id: Int, accessHash: Long) = OutPeer(2, id, accessHash)
  }
  final case class OutPeer(`type`: Int, id: Int, accessHash: Long)
  final case class Peer(`type`: Int, id: Int)

  sealed trait RequestBody

  final case class BotRequest(id: Long, body: RequestBody) extends BotMessage

  @key("SendMessage")
  final case class SendTextMessage(peer: OutPeer, randomId: Long, message: String) extends RequestBody

  sealed trait ResponseBody

  final case class BotResponse(id: Long, body: ResponseBody) extends BotMessage

  @key("MessageSent")
  final case class MessageSent(date: Long) extends ResponseBody

  sealed trait BotUpdate extends BotMessage

  @key("TextMessage")
  final case class TextMessage(peer: Peer, senderUserId: Int, date: Long, randomId: Long, message: String) extends BotUpdate
}