package im.actor.util.http

import java.math.BigInteger
import java.nio.file.Files
import java.security.MessageDigest

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{ Span, Seconds }
import org.scalatest.{ FlatSpec, Matchers }

class DownloadManagerSpec extends FlatSpec with ScalaFutures with Matchers {
  it should "Download https files" in e1

  override implicit def patienceConfig: PatienceConfig =
    new PatienceConfig(timeout = Span(10, Seconds))

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val downloadManager = new DownloadManager()

  def e1() = {
    whenReady(downloadManager.download("https://ajax.googleapis.com/ajax/libs/webfont/1.5.18/webfont.js")) {
      case (path, size) ⇒

        val fileBytes = Files.readAllBytes(path)
        fileBytes.length shouldEqual size

        val md = MessageDigest.getInstance("MD5")
        val hexDigest = new BigInteger(1, md.digest(fileBytes)) toString (16)

        hexDigest shouldEqual "593e60ad549e46f8ca9a60755336c7df"
    }
  }
}
