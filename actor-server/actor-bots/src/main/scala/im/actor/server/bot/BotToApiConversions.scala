package im.actor.server.bot

import im.actor.api.rpc.files.ApiFastThumb
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

  implicit def toMessage(message: MessageBody): ApiMessage =
    message match {
      case TextMessage(text)    ⇒ ApiTextMessage(text, Vector.empty, None)
      case JsonMessage(rawJson) ⇒ ApiJsonMessage(rawJson)
      case DocumentMessage(
        fileId,
        accessHash,
        fileSize,
        name,
        mimeType,
        thumb,
        ext) ⇒ ApiDocumentMessage(fileId, accessHash, fileSize.toInt, name, mimeType, thumb, ext)
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