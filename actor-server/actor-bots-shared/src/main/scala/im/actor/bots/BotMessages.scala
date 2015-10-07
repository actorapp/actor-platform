package im.actor.bots

import derive.key

import scala.beans.BeanProperty

sealed trait BotMessage
sealed trait BotMessageIn extends BotMessage
sealed trait BotMessageOut extends BotMessage

object BotMessages {

  final case class FileLocation(
    @BeanProperty fileId:     Long,
    @BeanProperty accessHash: Long
  )

  final case class AvatarImage(
    @BeanProperty fileLocation: FileLocation,
    @BeanProperty width:        Int,
    @BeanProperty height:       Int,
    @BeanProperty fileSize:     Int
  )

  final case class Avatar(
    @BeanProperty smallImage: Option[AvatarImage],
    @BeanProperty largeImage: Option[AvatarImage],
    @BeanProperty fullImage:  Option[AvatarImage]
  )

  final case class User(
    @BeanProperty id:         Int,
    @BeanProperty accessHash: Long,
    @BeanProperty name:       String,
    @BeanProperty sex:        Option[Int],
    @BeanProperty about:      Option[String],
    @BeanProperty avatar:     Option[Avatar],
    @BeanProperty username:   Option[String],
    @BeanProperty isBot:      Option[Boolean]
  ) {
    def isMale = sex.contains(1)
    def isFemale = sex.contains(2)
    def isABot = isBot.contains(true)
  }

  final case class GroupMember(
    @BeanProperty userId:        Int,
    @BeanProperty inviterUserId: Int,
    @BeanProperty memberSince:   Long,
    @BeanProperty isAdmin:       Option[Boolean]
  )

  final case class Group(
    @BeanProperty id:            Int,
    @BeanProperty accessHash:    Long,
    @BeanProperty title:         String,
    @BeanProperty about:         Option[String],
    @BeanProperty avatar:        Option[Avatar],
    @BeanProperty isMember:      Boolean,
    @BeanProperty creatorUserId: Int,
    @BeanProperty members:       Seq[GroupMember]
  )

  final object OutPeer {
    def privat(id: Int, accessHash: Long) = OutPeer(1, id, accessHash)

    def user(id: Int, accessHash: Long) = privat(id, accessHash)

    def group(id: Int, accessHash: Long) = OutPeer(2, id, accessHash)
  }

  final case class OutPeer(
    @BeanProperty `type`:     Int,
    @BeanProperty id:         Int,
    @BeanProperty accessHash: Long
  ) {
    final def isPrivate = `type` == 1

    final def isUser = isPrivate

    final def isGroup = `type` == 2
  }

  final case class UserOutPeer(
    @BeanProperty id:         Int,
    @BeanProperty accessHash: Long
  ) {
    val asOutPeer = OutPeer(1, id, accessHash)
  }

  final case class Peer(
    @BeanProperty `type`: Int,
    @BeanProperty id:     Int
  )

  sealed trait RequestBody {
    type Response
  }

  sealed trait ResponseBody

  @key("Request")
  final case class BotRequest(
    @BeanProperty id:   Long,
    @BeanProperty body: RequestBody
  ) extends BotMessageIn

  @key("SendMessage")
  final case class SendTextMessage(
    @BeanProperty peer:     OutPeer,
    @BeanProperty randomId: Long,
    @BeanProperty text:     String
  ) extends RequestBody {
    override type Response = MessageSent
  }

  @key("Response")
  final case class BotResponse(
    @BeanProperty id:   Long,
    @BeanProperty body: ResponseBody
  ) extends BotMessageOut

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
  final case class MessageSent(@BeanProperty date: Long) extends ResponseBody

  sealed trait BotUpdate extends BotMessageOut {
    val seq: Int
    val body: UpdateBody
  }

  @key("SeqUpdate")
  final case class BotSeqUpdate(
    @BeanProperty seq:  Int,
    @BeanProperty body: UpdateBody
  ) extends BotUpdate

  @key("FatSeqUpdate")
  final case class BotFatSeqUpdate(
    @BeanProperty seq:    Int,
    @BeanProperty body:   UpdateBody,
    @BeanProperty users:  Map[Int, User],
    @BeanProperty groups: Map[Int, Group]
  ) extends BotUpdate

  sealed trait UpdateBody

  @key("TextMessage")
  final case class TextMessage(
    @BeanProperty peer:     OutPeer,
    @BeanProperty sender:   UserOutPeer,
    @BeanProperty date:     Long,
    @BeanProperty randomId: Long,
    @BeanProperty text:     String
  ) extends UpdateBody

}
