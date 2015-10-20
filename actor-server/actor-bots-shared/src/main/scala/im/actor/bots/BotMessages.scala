package im.actor.bots

import derive.key
import upickle.Js
import upickle.default._

object BotMessages {

  sealed trait BotMessage

  sealed trait BotMessageIn extends BotMessage

  sealed trait BotMessageOut extends BotMessage

  object Services {
    val KeyValue = "keyvalue"
    val Messaging = "messaging"
    val Bots = "bots"
    val WebHooks = "webhooks"
    val Users = "users"
  }

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
    small: Option[AvatarImage],
    large: Option[AvatarImage],
    full:  Option[AvatarImage]
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

  final case class OutPeer(
    `type`:     Int,
    id:         Int,
    accessHash: Long
  ) {
    final def isPrivate = `type` == 1

    final def isUser = isPrivate

    final def isGroup = `type` == 2
  }

  final case class UserOutPeer(
    id:         Int,
    accessHash: Long
  ) {
    val asOutPeer = OutPeer(1, id, accessHash)
  }

  final case class Peer(
    `type`: Int,
    id:     Int
  )

  sealed trait RequestBody {
    type Response <: ResponseBody
    val service: String

    def readResponse(obj: Js.Obj): Response
  }

  trait ResponseBody

  @key("Request")
  final case class BotRequest(
    id:      Long,
    service: String,
    body:    RequestBody
  ) extends BotMessageIn

  @key("Response")
  final case class BotResponse(
    id:   Long,
    body: BotResponseBody
  ) extends BotMessageOut

  sealed trait BotResponseBody

  sealed trait BotUpdate extends BotMessageOut {
    val seq: Int
    val body: UpdateBody
  }

  sealed trait UpdateBody

  @key("SeqUpdate")
  final case class BotSeqUpdate(
    seq:  Int,
    body: UpdateBody
  ) extends BotUpdate

  @key("FatSeqUpdate")
  final case class BotFatSeqUpdate(
    seq:    Int,
    body:   UpdateBody,
    users:  Map[Int, User],
    groups: Map[Int, Group]
  ) extends BotUpdate

  @key("Error")
  case class BotError(code: Int, tag: String, data: Js.Obj, retryIn: Option[Int]) extends RuntimeException(
    s"code: $code, tag: $tag, data: ${write(data)}, retryIn: $retryIn"
  ) with BotResponseBody

  object BotError {
    def apply(code: Int, tag: String): BotError = BotError(code, tag, Js.Obj(), None)
    def apply(code: Int, tag: String, retryIn: Option[Int]): BotError = BotError(code, tag, Js.Obj(), retryIn)
  }

  @key("Success")
  case class BotSuccess(obj: Js.Obj) extends BotResponseBody

  implicit val objWriter = Writer[Js.Obj] {
    case obj ⇒ obj
  }

  implicit val objReader = Reader[Js.Obj] {
    case obj: Js.Obj ⇒ obj
  }

  implicit val botSuccessWriter = upickle.default.Writer[BotSuccess] {
    case BotSuccess(obj) ⇒ obj
  }

  implicit val botSuccessReader = upickle.default.Reader[BotSuccess] {
    case obj: Js.Obj ⇒ BotSuccess(obj)
  }

  implicit val botErrorWriter = upickle.default.Writer[BotError] {
    case BotError(code, tag, data, retryInOpt) ⇒
      Js.Obj(
        "code" → Js.Num(code.toDouble),
        "tag" → Js.Str(tag),
        "data" → data,
        "retryIn" → retryInOpt.map(n ⇒ Js.Num(n.toDouble)).getOrElse(Js.Null)
      )
  }

  final case class Container[T](value: T) extends ResponseBody

  trait Void extends ResponseBody

  final case object Void extends Void

  implicit val voidReader = upickle.default.Reader[Void] {
    case Js.Obj() ⇒ Void
  }

  implicit val voidWriter = upickle.default.Writer[Void] {
    case _ ⇒ Js.Obj()
  }

  @key("SendMessage")
  final case class SendMessage(
    peer:     OutPeer,
    randomId: Long,
    message:  MessageBody
  ) extends RequestBody {
    override type Response = MessageSent
    override val service = Services.Messaging

    override def readResponse(obj: Js.Obj) = readJs[MessageSent](obj)
  }

  @key("SetValue")
  final case class SetValue(
    keyspace: String,
    key:      String,
    value:    String
  ) extends RequestBody {
    override type Response = Void
    override val service = Services.KeyValue

    override def readResponse(obj: Js.Obj) = readJs[Void](obj)
  }

  @key("GetValue")
  final case class GetValue(
    keyspace: String,
    key:      String
  ) extends RequestBody {
    override type Response = Container[Option[String]]
    override val service = Services.KeyValue

    override def readResponse(obj: Js.Obj) = readJs[Container[Option[String]]](obj)
  }

  @key("DeleteValue")
  final case class DeleteValue(
    keyspace: String,
    key:      String
  ) extends RequestBody {
    override type Response = Void
    override val service = Services.KeyValue

    override def readResponse(obj: Js.Obj) = readJs[Void](obj)
  }

  @key("GetKeys")
  final case class GetKeys(keyspace: String) extends RequestBody {
    override type Response = Container[Seq[String]]
    override val service = Services.KeyValue

    override def readResponse(obj: Js.Obj) = readJs[Container[Seq[String]]](obj)
  }

  @key("CreateBot")
  final case class CreateBot(username: String, name: String) extends RequestBody {
    override type Response = Container[String]
    override val service = Services.Bots

    override def readResponse(obj: Js.Obj) = readJs[Response](obj)
  }

  @key("RegisterHook")
  final case class RegisterHook(name: String) extends RequestBody {
    override type Response = Container[String]
    override val service = Services.WebHooks

    override def readResponse(obj: Js.Obj) = readJs[Response](obj)
  }

  @key("GetHooks")
  sealed trait GetHooks extends RequestBody {
    override type Response = Container[Seq[String]]
    override val service = Services.WebHooks

    override def readResponse(obj: Js.Obj) = readJs[Response](obj)
  }

  @key("GetHooks")
  final case object GetHooks extends GetHooks

  @key("UpdateAvatar")
  final case class UpdateAvatar(userId: Int, avatar: Avatar) extends RequestBody {
    override type Response = Void
    override val service = Services.Users

    override def readResponse(obj: Js.Obj) = readJs[Response](obj)
  }

  final case class MessageSent(date: Long) extends ResponseBody

  @key("Message")
  final case class Message(
    peer:     OutPeer,
    sender:   UserOutPeer,
    date:     Long,
    randomId: Long,
    message:  MessageBody
  ) extends UpdateBody

  sealed trait MessageBody

  @key("Text")
  final case class TextMessage(text: String) extends MessageBody

  @key("Json")
  final case class JsonMessage(rawJson: String) extends MessageBody

  @key("Document")
  final case class DocumentMessage(
    fileId:     Long,
    accessHash: Long,
    fileSize:   Long,
    name:       String,
    mimeType:   String,
    thumb:      Option[FastThumb],
    ext:        Option[DocumentEx]
  ) extends MessageBody

  @key("Service")
  final case class ServiceMessage(text: String) extends MessageBody

  @key("Unsupported")
  sealed trait UnsupportedMessage extends MessageBody

  @key("Unsupported")
  final case object UnsupportedMessage extends UnsupportedMessage

  @key("FastThumb")
  final case class FastThumb(
    width:  Int,
    height: Int,
    thumb:  String
  )

  sealed trait DocumentEx

  @key("Photo")
  final case class DocumentExPhoto(
    width:  Int,
    height: Int
  ) extends DocumentEx

  @key("Video")
  final case class DocumentExVideo(
    width:    Int,
    height:   Int,
    duration: Int
  ) extends DocumentEx

  @key("Voice")
  final case class DocumentExVoice(duration: Int) extends DocumentEx

}
