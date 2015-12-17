package im.actor.server.stickers

import im.actor.serialization.ActorSerializer
import im.actor.server.sticker.{ Sticker, StickerImage }

object StickerMessages {
  def register(): Unit =
    ActorSerializer.register(
      100001 → classOf[Sticker],
      100002 → classOf[StickerImage]
    )
}
