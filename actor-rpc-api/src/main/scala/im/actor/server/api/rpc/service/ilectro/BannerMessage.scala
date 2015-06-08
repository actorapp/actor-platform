package im.actor.server.api.rpc.service.ilectro

import play.api.libs.json._

object MessageFormats {
  implicit val imageFormat: Format[Image] = Json.format[Image]
  implicit val bannerDataFormat: Format[BannerData] = Json.format[BannerData]
  implicit val dataWrites: Writes[Data] = Writes[Data] { data ⇒
    data match {
      case d: BannerData ⇒ Json.writes[BannerData].writes(d)
    }
  }
  implicit val messageWrites: Writes[Message] = Json.writes[Message]
}

object Message {
  def banner(advertUrl: String, fileId: Long, fileAccessHash: Long, fileSize: Long, width: Int, height: Int) = {
    Message(BannerData.dataType, BannerData(advertUrl, Image(fileId, fileAccessHash, fileSize, width, height)))
  }
}

case class Message(dataType: String, data: Data)

sealed trait Data

object BannerData {
  val dataType = "banner"
}
case class BannerData(advertUrl: String, image: Image) extends Data
case class Image(fileId: Long, fileAccessHash: Long, fileSize: Long, width: Int, height: Int)
