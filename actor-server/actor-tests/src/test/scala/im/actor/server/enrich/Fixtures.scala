package im.actor.server.enrich

case class ImageData(url: String, contentLength: Int, fileName: Option[String], mimeType: String, w: Int, h: Int) {
  def getThumbWH(minSide: Int): (Int, Int) = {
    val scaleFactor = minSide.toDouble / math.min(w, h)
    ((w * scaleFactor).toInt, (h * scaleFactor).toInt)
  }
}

object Images {
  val noNameHttp = ImageData("http://www.google.com/images/srpr/logo11w.png", 14022, None, "image/png", 538, 190)
  val withNameHttp = ImageData(
    url = "http://3.bp.blogspot.com/-y_-bQwAC-po/Tb3z6szJMRI/AAAAAAAAAZY/ZLkBrx6zXXQ/s1600/Black+cat+and+spaniel%252C+HMS+%2527Barham%2527.jpg",
    contentLength = 215482,
    fileName = Some("Black cat and spaniel, HMS 'Barham'.jpg"),
    mimeType = "image/jpeg",
    w = 796, h = 1280
  )
  val noNameHttps = ImageData("https://www.google.com/images/srpr/logo11w.png", 14022, None, "image/png", 538, 190)
  val withRedirect = ImageData(
    url = "http://hsto.org/getpro/habr/avatars/f7a/28d/abb/f7a28dabb94ba9f0b4ec073446a6f722.png",
    contentLength = 19263,
    fileName = None,
    mimeType = "image/png",
    w = 96,
    h = 96
  )
}

object NonImages {
  val mixedText = s"hello, here is cure picture ${Images.noNameHttp.url}"
  val plainText = "well, it isn not cute actually"
  val nonImageUrl = "https://ajax.googleapis.com/ajax/libs/webfont/1.5.18/webfont.js"
}
