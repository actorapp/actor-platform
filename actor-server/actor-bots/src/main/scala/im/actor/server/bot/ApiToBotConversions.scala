package im.actor.server.bot

import im.actor.api.rpc.files.ApiFastThumb
import im.actor.api.rpc.messaging._
import scodec.bits.BitVector

import scala.language.implicitConversions

trait ApiToBotConversions {

  import im.actor.bots.BotMessages._

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

  implicit def toMessage(message: ApiMessage): MessageBody =
    message match {
      case ApiTextMessage(text, _, _) ⇒ TextMessage(text)
      case ApiJsonMessage(rawJson)    ⇒ JsonMessage(rawJson)
      case ApiDocumentMessage(
        fileId,
        accessHash,
        fileSize,
        name,
        mimeType,
        thumb,
        ext) ⇒ DocumentMessage(fileId, accessHash, fileSize.toLong, name, mimeType, thumb, ext)
      case ApiServiceMessage(text, _) ⇒ ServiceMessage(text)
      case _: ApiUnsupportedMessage   ⇒ UnsupportedMessage
    }

}