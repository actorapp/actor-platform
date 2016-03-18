package im.actor.server.bot

import im.actor.api.rpc.files._
import im.actor.api.rpc.messaging._
import im.actor.api.rpc.peers.{ ApiPeer, ApiPeerType, ApiOutPeer }
import scodec.bits.BitVector

import scala.language.implicitConversions

trait BotToApiConversions {
  import im.actor.bots.BotMessages._

  implicit def toThumb(ft: FastThumb): ApiFastThumb =
    ApiFastThumb(ft.width, ft.height, BitVector.fromValidBase64(ft.thumb).toByteArray)

  implicit def toThumb(ft: Option[FastThumb]): Option[ApiFastThumb] = ft map toThumb

  implicit def toDocumentExt(ex: DocumentEx): ApiDocumentEx =
    ex match {
      case DocumentExPhoto(width, height)           ⇒ ApiDocumentExPhoto(width, height)
      case DocumentExVideo(width, height, duration) ⇒ ApiDocumentExVideo(width, height, duration)
      case DocumentExVoice(duration)                ⇒ ApiDocumentExVoice(duration)
    }

  implicit def toDocumentExt(ex: Option[DocumentEx]): Option[ApiDocumentEx] = ex map toDocumentExt

  implicit def toTextMessageEx(ex: TextMessageEx): ApiTextMessageEx = ex match {
    case TextModernMessage(text, senderNameOverride, senderPhotoOverride, style, attaches) ⇒ ApiTextModernMessage(text, senderNameOverride, senderPhotoOverride, style, attaches)
    case TextCommand(command, args) ⇒ ApiTextCommand(command, args)
  }

  implicit def toModernAttach(a: TextModernAttach): ApiTextModernAttach =
    ApiTextModernAttach(a.title, a.titleUrl, a.titleIcon, a.text, a.style, a.fields)

  implicit def toModernAttach(as: IndexedSeq[TextModernAttach]): IndexedSeq[ApiTextModernAttach] =
    as map toModernAttach

  implicit def toTextMessageEx(ex: Option[TextMessageEx]): Option[ApiTextMessageEx] =
    ex map toTextMessageEx

  implicit def toParagraphStyle(ps: ParagraphStyle): ApiParagraphStyle =
    ApiParagraphStyle(ps.showParagraph, ps.paragraphColor, ps.bgColor)

  implicit def toParagraphStyle(ps: Option[ParagraphStyle]): Option[ApiParagraphStyle] =
    ps map toParagraphStyle

  implicit def toApiAvatarImage(ai: AvatarImage): ApiAvatarImage =
    ApiAvatarImage(ai.fileLocation, ai.width, ai.height, ai.fileSize)

  implicit def toApiAvatarImage(ai: Option[AvatarImage]): Option[ApiAvatarImage] =
    ai map toApiAvatarImage

  implicit def toApiAvatar(avatar: Avatar): ApiAvatar =
    ApiAvatar(avatar.smallImage, avatar.largeImage, avatar.fullImage)

  implicit def toApiAvatar(avatar: Option[Avatar]): Option[ApiAvatar] =
    avatar map toApiAvatar

  implicit def toApiFileLocation(fl: FileLocation): ApiFileLocation =
    ApiFileLocation(fl.fileId, fl.accessHash)

  implicit def toTextModernField(mf: TextModernField): ApiTextModernField =
    ApiTextModernField(mf.title, mf.value, mf.isShort)

  implicit def toTextModernField(mfs: IndexedSeq[TextModernField]): IndexedSeq[ApiTextModernField] =
    mfs map toTextModernField

  implicit def toColors(color: Colors): ApiColors.ApiColors =
    color match {
      case Red    ⇒ ApiColors.red
      case Yellow ⇒ ApiColors.yellow
      case Green  ⇒ ApiColors.green
    }

  implicit def toColor(color: Color): ApiColor =
    color match {
      case RgbColor(rgb)      ⇒ ApiRgbColor(rgb)
      case PredefinedColor(c) ⇒ ApiPredefinedColor(c)
    }

  implicit def toColor(color: Option[Color]): Option[ApiColor] =
    color map toColor

  implicit def toImageLocation(il: ImageLocation): ApiImageLocation =
    ApiImageLocation(il.fileLocation, il.width, il.height, il.fileSize)

  implicit def toImageLocation(il: Option[ImageLocation]): Option[ApiImageLocation] =
    il map toImageLocation

  implicit def toMessage(message: MessageBody): ApiMessage =
    message match {
      case TextMessage(text, ext) ⇒ ApiTextMessage(text, Vector.empty, ext)
      case JsonMessage(rawJson)   ⇒ ApiJsonMessage(rawJson)
      case DocumentMessage(
        fileId,
        accessHash,
        fileSize,
        name,
        mimeType,
        thumb,
        ext) ⇒ ApiDocumentMessage(fileId, accessHash, fileSize.toInt, name, mimeType, thumb, ext)
      case StickerMessage(stickerId, fastPreview, image512, image256, stickerCollectionId, stickerCollectionAccessHash) ⇒
        ApiStickerMessage(stickerId, fastPreview, image512, image256, stickerCollectionId, stickerCollectionAccessHash)
      case ServiceMessage(_)  ⇒ throw new RuntimeException("Service messages are not supported")
      case UnsupportedMessage ⇒ ApiUnsupportedMessage
    }

  implicit def toOutPeer(outPeer: OutPeer): ApiOutPeer =
    outPeer match {
      case UserOutPeer(id, accessHash)  ⇒ ApiOutPeer(ApiPeerType.Private, id, accessHash)
      case GroupOutPeer(id, accessHash) ⇒ ApiOutPeer(ApiPeerType.Group, id, accessHash)
    }

  implicit def toPeer(outPeer: OutPeer): ApiPeer =
    outPeer match {
      case UserOutPeer(id, _)  ⇒ ApiPeer(ApiPeerType.Private, id)
      case GroupOutPeer(id, _) ⇒ ApiPeer(ApiPeerType.Group, id)
    }
}