package im.actor.bots

import derive.key

sealed trait BotMessage
sealed trait BotMessageIn extends BotMessage
sealed trait BotMessageOut extends BotMessage

object BotMessages {

  final case class FileLocation(
    fileId:     Long,
    accessHash: Long
  )

  final case class AvatarImage(
    fileLocation: FileLocation,
    width:        Int,
    height:       Int,
    fileSize:     Int
  )

  final case class Avatar(
    smallImage: Option[AvatarImage],
    largeImage: Option[AvatarImage],
    fullImage:  Option[AvatarImage]
  )

  final case class User(
    id:         Int,
    accessHash: Long,
    name:       String,
    sex:        Option[Int],
    about:      Option[String],
    avatar:     Option[Avatar],
    username:   Option[String],
    isBot:      Option[Boolean]
  ) {
    def isMale = sex.contains(1)
    def isFemale = sex.contains(2)
    def isABot = isBot.contains(true)
  }

  final case class GroupMember(
    userId:        Int,
    inviterUserId: Int,
    memberSince:   Long,
    isAdmin:       Option[Boolean]
  )

  final case class Group(
    id:            Int,
    accessHash:    Long,
    title:         String,
    about:         Option[String],
    avatar:        Option[Avatar],
    isMember:      Boolean,
    creatorUserId: Int,
    members:       Seq[GroupMember]
  )

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

  sealed trait BotUpdate extends BotMessageOut {
    val seq: Int
    val body: UpdateBody
  }

  @key("SeqUpdate")
  final case class BotSeqUpdate(seq: Int, body: UpdateBody) extends BotUpdate

  @key("FatSeqUpdate")
  final case class BotFatSeqUpdate(seq: Int, body: UpdateBody, users: Map[Int, User], groups: Map[Int, Group]) extends BotUpdate

  sealed trait UpdateBody

  @key("TextMessage")
  final case class TextMessage(peer: OutPeer, sender: UserOutPeer, date: Long, randomId: Long, text: String) extends UpdateBody

}
