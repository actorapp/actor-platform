package im.actor.server.bot

import im.actor.api.rpc.files._
import im.actor.api.rpc.groups.{ ApiGroup, ApiMember }
import im.actor.api.rpc.messaging._
import im.actor.api.rpc.peers.{ ApiOutPeer, ApiPeerType }
import im.actor.api.rpc.users.{ ApiBotCommand, ApiContactRecord, ApiContactType, ApiUser }
import scodec.bits.BitVector

import scala.language.{ implicitConversions, postfixOps }

trait ApiToBotConversions {

  import im.actor.bots.BotMessages._

  implicit def toOutPeer(outPeer: ApiOutPeer): OutPeer =
    outPeer.`type` match {
      case ApiPeerType.Private ⇒ UserOutPeer(outPeer.id, outPeer.accessHash)
      case ApiPeerType.Group   ⇒ GroupOutPeer(outPeer.id, outPeer.accessHash)
    }

  implicit def toFastThumb(ft: ApiFastThumb): FastThumb =
    FastThumb(ft.w, ft.h, BitVector(ft.thumb).toBase64)

  implicit def toFastThumb(ft: Option[ApiFastThumb]): Option[FastThumb] =
    ft map toFastThumb

  implicit def toDocumentEx(ex: ApiDocumentEx): DocumentEx =
    ex match {
      case ApiDocumentExPhoto(w, h)           ⇒ DocumentExPhoto(w, h)
      case ApiDocumentExVideo(w, h, duration) ⇒ DocumentExVideo(w, h, duration)
      case ApiDocumentExVoice(duration)       ⇒ DocumentExVoice(duration)
    }

  implicit def toDocumentEx(ex: Option[ApiDocumentEx]): Option[DocumentEx] =
    ex map toDocumentEx

  implicit def toTextMessageEx(ex: ApiTextMessageEx): TextMessageEx =
    ex match {
      case ApiTextModernMessage(text, senderNameOverride, senderPhotoOverride, style, attaches) ⇒ TextModernMessage(text, senderNameOverride, toAvatar(senderPhotoOverride), style, attaches)
      case ApiTextExMarkdown(text) ⇒ TextModernMessage(Some(text), None, None, None, Vector.empty)
      case ApiTextCommand(command, args) ⇒ TextCommand(command, args)
    }

  implicit def toModernAttach(a: ApiTextModernAttach): TextModernAttach =
    TextModernAttach(a.title, a.titleUrl, a.titleIcon, a.text, a.style, a.fields)

  implicit def toModernAttach(as: IndexedSeq[ApiTextModernAttach]): IndexedSeq[TextModernAttach] =
    as map toModernAttach

  implicit def toTextMessageEx(ex: Option[ApiTextMessageEx]): Option[TextMessageEx] =
    ex map toTextMessageEx

  implicit def toMessage(message: ApiMessage): MessageBody =
    message match {
      case ApiTextMessage(text, _, ext) ⇒ TextMessage(text, ext)
      case ApiJsonMessage(rawJson)      ⇒ JsonMessage(rawJson)
      case ApiDocumentMessage(
        fileId,
        accessHash,
        fileSize,
        name,
        mimeType,
        thumb,
        ext) ⇒ DocumentMessage(fileId, accessHash, fileSize.toLong, name, mimeType, thumb, ext)
      case ApiServiceMessage(text, _) ⇒ ServiceMessage(text)
      case ApiStickerMessage(stickerId, fastPreview, image512, image256, stickerCollectionId, stickerCollectionAccessHash) ⇒
        StickerMessage(stickerId, fastPreview, image512, image256, stickerCollectionId, stickerCollectionAccessHash)
      case _: ApiUnsupportedMessage ⇒ UnsupportedMessage
      case _: ApiEncryptedMessage   ⇒ UnsupportedMessage
      case _: ApiBinaryMessage      ⇒ UnsupportedMessage
      case _: ApiEmptyMessage       ⇒ UnsupportedMessage
    }

  implicit def toTextModernAttach(ma: ApiTextModernAttach): TextModernAttach =
    TextModernAttach(ma.title, ma.titleUrl, ma.titleIcon, ma.text, ma.style, ma.fields)

  implicit def toTextModernField(mf: ApiTextModernField): TextModernField =
    TextModernField(mf.title, mf.value, mf.isShort)

  implicit def toTextModernField(mfs: IndexedSeq[ApiTextModernField]): IndexedSeq[TextModernField] =
    mfs map toTextModernField

  implicit def toParagraphStyle(ps: ApiParagraphStyle): ParagraphStyle =
    ParagraphStyle(ps.showParagraph, ps.paragraphColor, ps.bgColor)

  implicit def toParagraphStyle(ps: Option[ApiParagraphStyle]): Option[ParagraphStyle] =
    ps map toParagraphStyle

  implicit def toFileLocation(fl: ApiFileLocation): FileLocation =
    FileLocation(fl.fileId, fl.accessHash)

  implicit def toImageLocation(il: ApiImageLocation): ImageLocation =
    ImageLocation(il.fileLocation, il.width, il.height, il.fileSize)

  implicit def toImageLocation(il: Option[ApiImageLocation]): Option[ImageLocation] =
    il map toImageLocation

  implicit def toColors(color: ApiColors.ApiColors): Colors =
    color match {
      case c if c == ApiColors.red    ⇒ Red
      case c if c == ApiColors.yellow ⇒ Yellow
      case c if c == ApiColors.green  ⇒ Green
    }

  implicit def toColor(color: ApiColor): Color =
    color match {
      case ApiRgbColor(rgb)      ⇒ RgbColor(rgb)
      case ApiPredefinedColor(c) ⇒ PredefinedColor(c)
    }

  implicit def toColor(color: Option[ApiColor]): Option[Color] =
    color map toColor

  implicit def toAvatarImage(ai: ApiAvatarImage): AvatarImage =
    AvatarImage(ai.fileLocation, ai.width, ai.height, ai.fileSize)

  implicit def toAvatarImage(ai: Option[ApiAvatarImage]): Option[AvatarImage] =
    ai map toAvatarImage

  implicit def toAvatar(avatar: ApiAvatar): Avatar =
    Avatar(avatar.smallImage, avatar.largeImage, avatar.fullImage)

  implicit def toAvatar(avatar: Option[ApiAvatar]): Option[Avatar] =
    avatar map toAvatar

  implicit def toMember(apiMember: ApiMember): GroupMember =
    GroupMember(
      userId = apiMember.userId,
      inviterUserId = apiMember.inviterUserId,
      memberSince = apiMember.date,
      isAdmin = apiMember.isAdmin
    )

  implicit def toMembers(apiMembers: Seq[ApiMember]): Seq[GroupMember] =
    apiMembers map toMember

  def toContactRecord(apiContactRecord: ApiContactRecord): Option[ContactRecord] =
    apiContactRecord.`type` match {
      case ApiContactType.Email ⇒
        apiContactRecord.stringValue match {
          case Some(email) ⇒ Some(EmailContactRecord(email))
          case None        ⇒ throw new RuntimeException(s"ApiContactRecord with Email type does not contain stringValue: $apiContactRecord")
        }
      case ApiContactType.Phone ⇒
        apiContactRecord.longValue match {
          case Some(phone) ⇒ Some(PhoneContactRecord(phone))
          case None        ⇒ throw new RuntimeException(s"ApiContactRecord with Phone type does not contain longValue: $apiContactRecord")
        }
      case _ ⇒ None
    }

  implicit def toContactRecords(apiContactRecords: Seq[ApiContactRecord]): Seq[ContactRecord] =
    apiContactRecords flatMap toContactRecord

  implicit def toUser(apiUser: ApiUser): User =
    User(
      id = apiUser.id,
      accessHash = apiUser.accessHash,
      name = apiUser.name,
      sex = apiUser.sex.map(_.id),
      about = apiUser.about,
      avatar = apiUser.avatar,
      username = apiUser.nick,
      isBot = apiUser.isBot,
      contactRecords = apiUser.contactInfo,
      timeZone = apiUser.timeZone,
      preferredLanguages = apiUser.preferredLanguages,
      botCommands = apiUser.botCommands
    )

  implicit def toUsers(apiUsers: Seq[ApiUser]): Seq[User] = apiUsers map toUser

  implicit def toBotCommand(command: ApiBotCommand): BotCommand = BotCommand(command.slashCommand, command.description, command.locKey)

  implicit def toBotCommans(commands: Seq[ApiBotCommand]): Seq[BotCommand] = commands map toBotCommand

  def buildGroups(apiGroups: Seq[ApiGroup]): Map[Int, Group] = {
    apiGroups map { apiGroup ⇒
      apiGroup.id → Group(
        id = apiGroup.id,
        accessHash = apiGroup.accessHash,
        title = apiGroup.title,
        about = apiGroup.about,
        avatar = apiGroup.avatar,
        isMember = apiGroup.isMember getOrElse false,
        creatorUserId = apiGroup.creatorUserId,
        members = apiGroup.members
      )
    } toMap
  }

  def buildUsers(apiUsers: Seq[ApiUser]): Map[Int, User] = {
    apiUsers map { apiUser ⇒
      apiUser.id → toUser(apiUser)
    } toMap
  }
}