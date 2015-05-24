package im.actor.server.api.rpc.service.llectro

import play.api.libs.json.Json

object BannerMessageFormats {
  implicit val bannerMessageFormat = Json.format[BannerMessage]
}

case class BannerMessage(fileId: Long, fileAccessHash: Long, advertUrl: String)
