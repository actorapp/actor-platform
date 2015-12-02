package im.actor.bots

import derive.key
import upickle.Js
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
    preferredLanguages:     Seq[String]
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

  final case object Void extends Void

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
  final case object GetHooks extends GetHooks

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

  @key("Text")
  final case class TextMessage(@beanGetter text: String, ext: Option[TextMessageEx]) extends MessageBody {
    def getExt = ext.asJava
  }

  @key("Json")
  final case class JsonMessage(@beanGetter rawJson: String) extends MessageBody

  sealed trait TextMessageEx

  @key("TextModernMessage")
  final case class TextModernMessage(
    text:                Option[String],
    senderNameOverride:  Option[String],
    senderPhotoOverride: Option[Avatar],
    style:               Option[ParagraphStyle]
  ) extends TextMessageEx {
    def getText = text.asJava
    def getSenderNameOverride = senderNameOverride.asJava
    def getSenderPhotoOverride = senderPhotoOverride.asJava
    def getStyle = style.asJava
  }

  final case class TextModernAttach(
    title:     Option[String],
    titleUrl:  Option[String],
    titleIcon: Option[ImageLocation],
    text:      Option[String],
    style:     Option[ParagraphStyle],
    fields:    IndexedSeq[TextModernField]
  ) {
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
