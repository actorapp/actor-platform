package im.actor.bots

import derive.key
import upickle.Js
import upickle.Js.Obj
import upickle.default._

import scala.annotation.meta.beanGetter
import scala.collection.JavaConversions._
import scala.compat.java8.OptionConverters._

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
    val Groups = "groups"
    val Stickers = "stickers"
    val Files = "files"
  }

  final case class FileLocation(
    @beanGetter fileId:     Long,
    @beanGetter accessHash: Long
  )

  final case class AvatarImage(
    @beanGetter fileLocation: FileLocation,
    @beanGetter width:        Int,
    @beanGetter height:       Int,
    @beanGetter fileSize:     Int
  )

  final case class ImageLocation(
    @beanGetter fileLocation: FileLocation,
    @beanGetter width:        Int,
    @beanGetter height:       Int,
    @beanGetter fileSize:     Int
  )

  final case class Avatar(
    @beanGetter smallImage: Option[AvatarImage],
    @beanGetter largeImage: Option[AvatarImage],
    @beanGetter fullImage:  Option[AvatarImage]
  )

  final case class BotCommand(
    @beanGetter slashCommand: String,
    @beanGetter description:  String,
    locKey:                   Option[String]
  ) { def getLocKey = locKey.asJava }

  final case class ContactInfo(
    phones: Seq[Long],
    emails: Seq[String]
  )

  sealed trait ContactRecord

  @key("Email")
  final case class EmailContactRecord(email: String) extends ContactRecord

  @key("Phone")
  final case class PhoneContactRecord(phone: Long) extends ContactRecord

  final case class User(
    @beanGetter id:         Int,
    @beanGetter accessHash: Long,
    @beanGetter name:       String,
    @beanGetter sex:        Option[Int],
    about:                  Option[String],
    avatar:                 Option[Avatar],
    username:               Option[String],
    isBot:                  Option[Boolean],
    contactRecords:         Seq[ContactRecord],
    timeZone:               Option[String],
    preferredLanguages:     Seq[String],
    botCommands:            Seq[BotCommand]
  ) {
    def isMale = sex.contains(1)

    def isFemale = sex.contains(2)

    def isABot = isBot.contains(true)

    def getSex = sex.asJava

    def getAbout = about.asJava

    def getAvatar = avatar.asJava

    def getUsername = username.asJava

    def getIsBot = isBot.asJava

    def getContactRecords = seqAsJavaList(contactRecords)

    def getEmailContactRecords = seqAsJavaList(contactRecords collect {
      case e: EmailContactRecord ⇒ e
    })

    def getPhoneContactRecords = seqAsJavaList(contactRecords collect {
      case p: PhoneContactRecord ⇒ p
    })

    def getTimeZone = timeZone.asJava

    def getPreferredLanguages = seqAsJavaList(preferredLanguages)

    def getBotCommands = seqAsJavaList(botCommands)
  }

  final case class GroupMember(
    @beanGetter userId:        Int,
    @beanGetter inviterUserId: Int,
    @beanGetter memberSince:   Long,
    isAdmin:                   Option[Boolean]
  ) {
    def getIsAdmin = isAdmin.asJava
  }

  final case class Group(
    @beanGetter id:            Int,
    @beanGetter accessHash:    Long,
    @beanGetter title:         String,
    about:                     Option[String],
    avatar:                    Option[Avatar],
    @beanGetter isMember:      Boolean,
    @beanGetter creatorUserId: Int,
    members:                   Seq[GroupMember]
  ) {
    def getAbout = about.asJava

    def getAvatar = about.asJava

    def getMembers = seqAsJavaList(members)
  }

  final object OutPeer {
    def privat(id: Int, accessHash: Long) = UserOutPeer(id, accessHash)

    def user(id: Int, accessHash: Long) = privat(id, accessHash)

    def group(id: Int, accessHash: Long) = GroupOutPeer(id, accessHash)
  }

  sealed trait Peer {
    val id: Int
  }

  @key("User")
  final case class UserPeer(@beanGetter id: Int) extends Peer

  @key("Group")
  final case class GroupPeer(@beanGetter id: Int) extends Peer

  sealed trait OutPeer extends Peer {
    val id: Int
    val accessHash: Long

    val isPrivate: Boolean
    val isGroup: Boolean
  }

  @key("Group")
  final case class GroupOutPeer(
    @beanGetter id:         Int,
    @beanGetter accessHash: Long
  ) extends OutPeer {
    override val isPrivate = false
    override val isGroup = true
  }

  @key("User")
  final case class UserOutPeer(
    @beanGetter id:         Int,
    @beanGetter accessHash: Long
  ) extends OutPeer {
    override val isPrivate = true
    override val isGroup = false
  }

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

  case class Container[T](@beanGetter value: T) extends ResponseBody
  final case class ContainerList[T](value: Seq[T]) extends ResponseBody {
    def getValue = seqAsJavaList(value)
  }

  trait Void extends ResponseBody

  case object Void extends Void

  implicit val voidReader = upickle.default.Reader[Void] {
    case Js.Obj() ⇒ Void
  }

  implicit val voidWriter = upickle.default.Writer[Void] {
    case _ ⇒ Js.Obj()
  }

  @key("SendMessage")
  final case class SendMessage(
    @beanGetter peer:     OutPeer,
    @beanGetter randomId: Long,
    @beanGetter message:  MessageBody
  ) extends RequestBody {
    override type Response = MessageSent
    override val service = Services.Messaging

    override def readResponse(obj: Js.Obj) = readJs[MessageSent](obj)
  }

  @key("UpdateMessageContent")
  final case class UpdateMessageContent(
    @beanGetter peer:           OutPeer,
    @beanGetter randomId:       Long,
    @beanGetter updatedMessage: MessageBody
  ) extends RequestBody {
    override type Response = MessageContentUpdated
    override val service = Services.Messaging

    override def readResponse(obj: Js.Obj) = readJs[MessageContentUpdated](obj)
  }

  @key("SetValue")
  final case class SetValue(
    @beanGetter keyspace: String,
    @beanGetter key:      String,
    @beanGetter value:    String
  ) extends RequestBody {
    override type Response = Void
    override val service = Services.KeyValue

    override def readResponse(obj: Js.Obj) = readJs[Void](obj)
  }

  @key("GetValue")
  final case class GetValue(
    @beanGetter keyspace: String,
    @beanGetter key:      String
  ) extends RequestBody {
    override type Response = Container[Option[String]]
    override val service = Services.KeyValue

    override def readResponse(obj: Js.Obj) = readJs[Container[Option[String]]](obj)
  }

  @key("DeleteValue")
  final case class DeleteValue(
    @beanGetter keyspace: String,
    @beanGetter key:      String
  ) extends RequestBody {
    override type Response = Void
    override val service = Services.KeyValue

    override def readResponse(obj: Js.Obj) = readJs[Void](obj)
  }

  @key("GetKeys")
  final case class GetKeys(@beanGetter keyspace: String) extends RequestBody {
    override type Response = ContainerList[String]
    override val service = Services.KeyValue

    override def readResponse(obj: Js.Obj) = readJs[ContainerList[String]](obj)
  }

  //username is nickname
  @key("CreateBot")
  final case class CreateBot(
    @beanGetter username: String,
    @beanGetter name:     String
  ) extends RequestBody {
    override type Response = BotCreated
    override val service = Services.Bots

    override def readResponse(obj: Js.Obj) = readJs[Response](obj)
  }

  @key("BotCreated")
  final case class BotCreated(
    @beanGetter token:  String,
    @beanGetter userId: Int
  ) extends ResponseBody

  @key("RegisterHook")
  final case class RegisterHook(@beanGetter name: String) extends RequestBody {
    override type Response = Container[String]
    override val service = Services.WebHooks

    override def readResponse(obj: Js.Obj) = readJs[Response](obj)
  }

  @key("GetHooks")
  sealed trait GetHooks extends RequestBody {
    override type Response = ContainerList[String]
    override val service = Services.WebHooks

    override def readResponse(obj: Js.Obj) = readJs[Response](obj)
  }

  @key("GetHooks")
  case object GetHooks extends GetHooks

  @key("ChangeUserAvatar")
  final case class ChangeUserAvatar(
    @beanGetter userId:       Int,
    @beanGetter fileLocation: FileLocation
  ) extends RequestBody {
    override type Response = Void
    override val service = Services.Users

    override def readResponse(obj: Js.Obj) = readJs[Response](obj)
  }

  @key("ChangeUserName")
  final case class ChangeUserName(
    @beanGetter userId: Int,
    @beanGetter name:   String
  ) extends RequestBody {
    override type Response = Void
    override val service = Services.Users

    override def readResponse(obj: Js.Obj) = readJs[Response](obj)
  }

  @key("ChangeUserNickname")
  final case class ChangeUserNickname(
    @beanGetter userId:   Int,
    @beanGetter nickname: Option[String]
  ) extends RequestBody {
    override type Response = Void
    override val service = Services.Users

    override def readResponse(obj: Js.Obj): Response = readJs[Response](obj)
  }

  @key("ChangeUserAbout")
  final case class ChangeUserAbout(
    @beanGetter userId: Int,
    about:              Option[String]
  ) extends RequestBody {
    override type Response = Void
    override val service = Services.Users

    override def readResponse(obj: Js.Obj) = readJs[Response](obj)

    def getAbout = about.asJava
  }

  @key("AddSlashCommand")
  final case class AddSlashCommand(
    @beanGetter userId:  Int,
    @beanGetter command: BotCommand
  ) extends RequestBody {
    override type Response = Void
    override val service = Services.Users

    override def readResponse(obj: Js.Obj) = readJs[Response](obj)
  }

  @key("RemoveSlashCommand")
  final case class RemoveSlashCommand(
    @beanGetter userId:       Int,
    @beanGetter slashCommand: String
  ) extends RequestBody {
    override type Response = Void
    override val service = Services.Users

    override def readResponse(obj: Js.Obj) = readJs[Response](obj)
  }

  @key("AddUserExtString")
  final case class AddUserExtString(
    @beanGetter userId: Int,
    @beanGetter key:    String,
    @beanGetter value:  String
  ) extends RequestBody {
    override type Response = Void

    override def readResponse(obj: Obj) = readJs[Response](obj)

    override val service: String = Services.Users
  }

  @key("AddUserExtBool")
  final case class AddUserExtBool(
    @beanGetter userId: Int,
    @beanGetter key:    String,
    @beanGetter value:  Boolean
  ) extends RequestBody {
    override type Response = Void

    override def readResponse(obj: Obj) = readJs[Response](obj)

    override val service: String = Services.Users
  }

  @key("RemoveUserExt")
  final case class RemoveUserExt(
    @beanGetter userId: Int,
    @beanGetter key:    String
  ) extends RequestBody {
    override type Response = Void

    override def readResponse(obj: Obj) = readJs[Response](obj)

    override val service: String = Services.Users
  }

  @key("IsAdmin")
  final case class IsAdmin(@beanGetter userId: Int) extends RequestBody {
    override type Response = ResponseIsAdmin
    override val service = Services.Users
    override def readResponse(obj: Js.Obj) = readJs[Response](obj)
  }

  final case class ResponseIsAdmin(isAdmin: Boolean) extends ResponseBody {
    def getIsAdmin: java.lang.Boolean = isAdmin.booleanValue()
  }

  @key("FindUser")
  final case class FindUser(
    @beanGetter query: String
  ) extends RequestBody {
    override type Response = FoundUsers
    override val service: String = Services.Users

    override def readResponse(obj: Js.Obj): Response = readJs[Response](obj)
  }

  final case class FoundUsers(users: Seq[User]) extends ResponseBody {
    def getUsers = seqAsJavaList(users)
  }

  final case class MessageSent(@beanGetter date: Long) extends ResponseBody

  @key("MessageContentUpdated")
  sealed trait MessageContentUpdated extends ResponseBody

  @key("MessageContentUpdated")
  case object MessageContentUpdated extends MessageContentUpdated

  implicit val messageContentUpdatedReader = upickle.default.Reader[MessageContentUpdated] {
    case Js.Obj() ⇒ MessageContentUpdated
  }

  implicit val messageContentUpdatedWriter = upickle.default.Writer[MessageContentUpdated] {
    case _ ⇒ Js.Obj()
  }

  @key("CreateGroup")
  final case class CreateGroup(
    title: String
  ) extends RequestBody {
    override type Response = ResponseCreateGroup
    override val service: String = Services.Groups

    override def readResponse(obj: Js.Obj): Response = readJs[Response](obj)
  }

  final case class ResponseCreateGroup(@beanGetter peer: GroupOutPeer) extends ResponseBody

  @key("InviteUser")
  final case class InviteUser(@beanGetter groupPeer: GroupOutPeer, @beanGetter userPeer: UserOutPeer) extends RequestBody {
    override type Response = Void
    override val service: String = Services.Groups

    override def readResponse(obj: Js.Obj): Response = readJs[Response](obj)
  }

  @key("CreateStickerPack")
  final case class CreateStickerPack(@beanGetter creatorUserId: Int) extends RequestBody {
    override type Response = Container[String]
    override def readResponse(obj: Js.Obj): Response = readJs[Response](obj)
    override val service: String = Services.Stickers
  }

  @key("ShowStickerPacks")
  final case class ShowStickerPacks(@beanGetter ownerUserId: Int) extends RequestBody {
    override type Response = StickerPackIds
    override def readResponse(obj: Js.Obj): Response = readJs[Response](obj)
    override val service: String = Services.Stickers
  }

  final case class StickerPackIds(ids: Seq[String]) extends ResponseBody {
    def getIds = seqAsJavaList(ids)
  }

  @key("ShowStickers")
  final case class ShowStickers(
    @beanGetter ownerUserId: Int,
    @beanGetter packId:      Int
  ) extends RequestBody {
    override type Response = StickerIds
    override def readResponse(obj: Js.Obj): Response = readJs[Response](obj)
    override val service: String = Services.Stickers
  }

  final case class StickerIds(ids: Seq[String]) extends ResponseBody {
    def getIds = seqAsJavaList(ids)
  }

  @key("DownloadFile")
  final case class DownloadFile(@beanGetter fileLocation: FileLocation) extends RequestBody {
    override type Response = ResponseDownloadFile
    override def readResponse(obj: Js.Obj): Response = readJs[Response](obj)
    override val service: String = Services.Files
  }

  final case class ResponseDownloadFile(fileBytes: Array[Byte]) extends ResponseBody

  @key("UploadFile")
  final case class UploadFile(@beanGetter bytes: Array[Byte]) extends RequestBody {
    override type Response = ResponseUploadFile
    override def readResponse(obj: Js.Obj): Response = readJs[Response](obj)
    override val service: String = Services.Files
  }

  final case class ResponseUploadFile(@beanGetter location: FileLocation) extends ResponseBody

  @key("AddSticker")
  final case class AddSticker(
    @beanGetter ownerUserId: Int,
    @beanGetter packId:      Int,
    emoji:                   Option[String],
    @beanGetter small:       Array[Byte],
    @beanGetter smallW:      Int,
    @beanGetter smallH:      Int,
    @beanGetter medium:      Array[Byte],
    @beanGetter mediumW:     Int,
    @beanGetter mediumH:     Int,
    @beanGetter large:       Array[Byte],
    @beanGetter largeW:      Int,
    @beanGetter largeH:      Int
  ) extends RequestBody {
    override type Response = Void
    override def readResponse(obj: Js.Obj): Response = readJs[Response](obj)
    override val service: String = Services.Stickers

    def getEmoji = emoji.asJava
  }

  @key("DeleteSticker")
  final case class DeleteSticker(
    @beanGetter ownerUserId: Int,
    @beanGetter packId:      Int,
    @beanGetter stickerId:   Int
  ) extends RequestBody {
    override type Response = Void
    override def readResponse(obj: Js.Obj): Response = readJs[Response](obj)
    override val service: String = Services.Stickers
  }

  @key("MakeStickerPackDefault")
  final case class MakeStickerPackDefault(
    @beanGetter userId: Int,
    @beanGetter packId: Int
  ) extends RequestBody {
    override type Response = Void
    override def readResponse(obj: Js.Obj): Response = readJs[Response](obj)
    override val service: String = Services.Stickers
  }

  @key("UnmakeStickerPackDefault")
  final case class UnmakeStickerPackDefault(
    @beanGetter userId: Int,
    @beanGetter packId: Int
  ) extends RequestBody {
    override type Response = Void
    override def readResponse(obj: Js.Obj): Response = readJs[Response](obj)
    override val service: String = Services.Stickers
  }

  @key("Message")
  final case class Message(
    @beanGetter peer:     OutPeer,
    @beanGetter sender:   UserOutPeer,
    @beanGetter date:     Long,
    @beanGetter randomId: Long,
    @beanGetter message:  MessageBody
  ) extends UpdateBody

  @key("RawUpdate")
  final case class RawUpdate(
    `type`:           Option[String],
    @beanGetter data: String
  ) extends UpdateBody {
    def getType = `type`.asJava
  }

  sealed trait MessageBody

  //ext has default value for backward compatibility with old bots
  @key("Text")
  final case class TextMessage(@beanGetter text: String, ext: Option[TextMessageEx] = None) extends MessageBody {
    def getExt = ext.asJava
  }

  @key("Json")
  final case class JsonMessage(@beanGetter rawJson: String) extends MessageBody

  @key("Sticker")
  final case class StickerMessage(
    stickerId:                   Option[Int],
    fastPreview:                 Option[Array[Byte]],
    image512:                    Option[ImageLocation],
    image256:                    Option[ImageLocation],
    stickerCollectionId:         Option[Int],
    stickerCollectionAccessHash: Option[Long]
  ) extends MessageBody {
    def getStickerId = stickerId.asPrimitive
    def getFastPreview = fastPreview.asJava
    def getImage512 = image512.asJava
    def getImage256 = image256.asJava
    def getStickerCollectionId = stickerCollectionId.asPrimitive
    def getStickerCollectionAccessHash = stickerCollectionAccessHash.asPrimitive
  }

  sealed trait TextMessageEx

  @key("TextCommand")
  final case class TextCommand(text: String, args: String) extends TextMessageEx

  @key("TextModernMessage")
  final case class TextModernMessage(
    text:                Option[String],
    senderNameOverride:  Option[String],
    senderPhotoOverride: Option[Avatar],
    style:               Option[ParagraphStyle],
    attaches:            IndexedSeq[TextModernAttach]
  ) extends TextMessageEx {
    def this(
      text:                String,
      senderNameOverride:  String,
      senderPhotoOverride: Avatar,
      style:               ParagraphStyle,
      attaches:            java.util.List[TextModernAttach]
    ) =
      this(Option(text), Option(senderNameOverride), Option(senderPhotoOverride), Option(style), attaches.toIndexedSeq)

    def getText = text.asJava
    def getSenderNameOverride = senderNameOverride.asJava
    def getSenderPhotoOverride = senderPhotoOverride.asJava
    def getStyle = style.asJava
    def getAttaches = seqAsJavaList(attaches)
  }

  final case class TextModernAttach(
    title:     Option[String],
    titleUrl:  Option[String],
    titleIcon: Option[ImageLocation],
    text:      Option[String],
    style:     Option[ParagraphStyle],
    fields:    IndexedSeq[TextModernField]
  ) {
    def this(
      title:     String,
      titleUrl:  String,
      titleIcon: ImageLocation,
      text:      String,
      style:     ParagraphStyle,
      fields:    java.util.List[TextModernField]
    ) = this(Option(title), Option(titleUrl), Option(titleIcon), Option(text), Option(style), fields.toIndexedSeq)

    def getTitle = title.asJava
    def getTitleUrl = titleUrl.asJava
    def getTitleIcon = titleIcon.asJava
    def getText = text.asJava
    def getStyle = style.asJava
    def getFields = seqAsJavaList(fields) //fields.toSeq.seqAsJavaList doesn't work for some reason
  }

  final case class TextModernField(@beanGetter title: String, @beanGetter value: String, isShort: Option[Boolean]) {
    def getIsShort = isShort.asJava
  }

  final case class ParagraphStyle(
    showParagraph:  Option[Boolean],
    paragraphColor: Option[Color],
    bgColor:        Option[Color]
  ) {
    def getShowParagraph = showParagraph.asJava
    def getParagraphColor = paragraphColor.asJava
    def getBgColor = bgColor.asJava
  }

  sealed trait Colors

  @key("Red") case object Red extends Colors

  @key("Yellow") case object Yellow extends Colors

  @key("Green") case object Green extends Colors

  sealed trait Color

  @key("PredefinedColor")
  final case class PredefinedColor(color: Colors) extends Color

  @key("RgbColor")
  final case class RgbColor(rgb: Int) extends Color

  @key("Document")
  final case class DocumentMessage(
    @beanGetter fileId:     Long,
    @beanGetter accessHash: Long,
    @beanGetter fileSize:   Long,
    @beanGetter name:       String,
    @beanGetter mimeType:   String,
    thumb:                  Option[FastThumb],
    ext:                    Option[DocumentEx]
  ) extends MessageBody {
    def getThumb = thumb.asJava

    def getExt = ext.asJava
  }

  @key("Service")
  final case class ServiceMessage(@beanGetter text: String) extends MessageBody

  @key("Unsupported")
  sealed trait UnsupportedMessage extends MessageBody

  @key("Unsupported")
  final case object UnsupportedMessage extends UnsupportedMessage

  @key("FastThumb")
  final case class FastThumb(
    @beanGetter width:  Int,
    @beanGetter height: Int,
    @beanGetter thumb:  String
  )

  sealed trait DocumentEx

  @key("Photo")
  final case class DocumentExPhoto(
    @beanGetter width:  Int,
    @beanGetter height: Int
  ) extends DocumentEx

  @key("Video")
  final case class DocumentExVideo(
    @beanGetter width:    Int,
    @beanGetter height:   Int,
    @beanGetter duration: Int
  ) extends DocumentEx

  @key("Voice")
  final case class DocumentExVoice(@beanGetter duration: Int) extends DocumentEx

}
