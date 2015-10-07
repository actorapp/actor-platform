package im.actor.bots

import derive.key

sealed trait BotMessage
sealed trait BotMessageIn extends BotMessage
sealed trait BotMessageOut extends BotMessage

object BotMessages {

  final object OutPeer {
    def privat(id: Int, accessHash: Long) = OutPeer(1, id, accessHash)

    def user(id: Int, accessHash: Long) = privat(id, accessHash)

    def group(id: Int, accessHash: Long) = OutPeer(2, id, accessHash)
  }

  final case class OutPeer(`type`: Int, id: Int, accessHash: Long) {
    final def isPrivate = `type` == 1

    final def isUser = isPrivate

    final def isGroup = `type` == 2
  }

  final case class UserOutPeer(id: Int, accessHash: Long) {
    val asOutPeer = OutPeer(1, id, accessHash)
  }

  final case class Peer(`type`: Int, id: Int)

  sealed trait RequestBody {
    type Response
  }

  sealed trait ResponseBody

  @key("Request")
  final case class BotRequest(id: Long, body: RequestBody) extends BotMessageIn

  @key("SendMessage")
  final case class SendTextMessage(peer: OutPeer, randomId: Long, text: String) extends RequestBody {
    override type Response = MessageSent
  }

  @key("Response")
  final case class BotResponse(id: Long, body: ResponseBody) extends BotMessageOut

  /*
  @key("SetValue")
  final case class SetValue(name: String, key: String, value: String) extends RequestBody

  @key("GetValue")
  final case class GetValue(name: String, key: String) extends RequestBody

  @key("DeleteValue")
  final case class DeleteValue(name: String, key: String) extends RequestBody

  @key("GetKeys")
  final case class GetKeys(name: String) extends RequestBody

  @key("GetKeysResponse")
  final case class GetKeysResponse(keys: Seq[String]) extends ResponseBody
*/

  @key("MessageSent")
  final case class MessageSent(date: Long) extends ResponseBody

  @key("SeqUpdate")
  final case class BotSeqUpdate(seq: Int, body: UpdateBody) extends BotMessageOut

  sealed trait UpdateBody

  @key("TextMessage")
  final case class TextMessage(peer: OutPeer, sender: UserOutPeer, date: Long, randomId: Long, text: String) extends UpdateBody

}
