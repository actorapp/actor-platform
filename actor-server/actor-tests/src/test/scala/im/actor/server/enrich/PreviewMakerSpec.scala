package im.actor.server.enrich

import scala.concurrent.duration._

import akka.actor.ActorRef
import akka.testkit.TestProbe
import com.sksamuel.scrimage.Image
import org.joda.time.DateTime

import im.actor.api.rpc.messaging.ApiMessage

class PreviewMakerSpec extends BaseRichMessageSpec {
  behavior of "Preview maker actor"

  it should "reply with failure to malformed url" in t.malformedUrl()

  it should "reply with failure to url with non image content" in t.nonImageUrl()

  it should "reply with success to url with image content(no file name)" in t.imageUrlWithoutName()

  it should "reply with success to url with image content(with file name)" in t.imageUrlWithName()

  it should "reply with failure when image content is too big(configured)" in t.imageTooLarge()

  it should "work with https just fine" in t.worksWithHttps()

  it should "work with redirects" in t.worksWithRedirects()

  object t {

    import PreviewMaker.Failures

    implicit val probe = TestProbe()

    val config = RichMessageConfig(5 * 1024 * 1024)
    val previewMaker = PreviewMaker(config, "previewMaker" + new DateTime)

    import PreviewMaker._

    def malformedUrl() = {

      sendGetPreview(previewMaker, NonImages.plainText)
      probe watch previewMaker
      probe.expectMsgPF(10.seconds) {
        case PreviewFailure(Failures.Messages.Failed, _) ⇒
      }
    }

    def nonImageUrl() = {

      sendGetPreview(previewMaker, NonImages.nonImageUrl)
      probe watch previewMaker
      probe.expectMsgPF(10.seconds) {
        case PreviewFailure(Failures.Messages.NotAnImage, _) ⇒
      }
    }

    def imageUrlWithoutName() = {
      val image = Images.noNameHttp

      sendGetPreview(previewMaker, image.url)
      probe watch previewMaker
      probe.expectMsgPF(10.seconds) {
        case PreviewSuccess(content, fileName, mimeType, _) ⇒
          content should not be empty
          content.length shouldEqual image.contentLength
          fileName shouldEqual image.fileName
          mimeType shouldEqual image.mimeType
      }
    }

    def imageUrlWithName() = {
      val image = Images.withNameHttp

      sendGetPreview(previewMaker, image.url)
      probe watch previewMaker
      probe.expectMsgPF(10.seconds) {
        case PreviewSuccess(content, fileName, mimeType, _) ⇒
          content should not be empty
          content.length shouldEqual image.contentLength
          fileName shouldEqual image.fileName
          mimeType shouldEqual image.mimeType
      }
    }

    def imageTooLarge() = {
      val image = Images.withNameHttp
      val config = RichMessageConfig(image.contentLength - 1000L)
      val previewMaker = PreviewMaker(config, "previewMaker" + new DateTime)

      sendGetPreview(previewMaker, image.url)
      probe watch previewMaker
      probe.expectMsgPF(10.seconds) {
        case PreviewFailure(Failures.Messages.ContentTooLong, _) ⇒
      }
    }

    def worksWithHttps() = {
      val image = Images.noNameHttps

      sendGetPreview(previewMaker, image.url)
      probe watch previewMaker
      probe.expectMsgPF(10.seconds) {
        case PreviewSuccess(content, fileName, mimeType, _) ⇒
          content should not be empty
          fileName shouldEqual image.fileName
          mimeType shouldEqual image.mimeType
      }
    }

    def worksWithRedirects() = {
      val image = Images.withRedirect

      sendGetPreview(previewMaker, image.url)
      probe watch previewMaker
      probe.expectMsgPF(10.seconds) {
        case PreviewSuccess(content, fileName, mimeType, _) ⇒
          content should not be empty
          fileName shouldEqual image.fileName
          mimeType shouldEqual image.mimeType
          val img = Image(content.toArray)
          img.width shouldEqual image.w
          img.height shouldEqual image.h
      }
    }

    class DummyUpdateHandler extends UpdateHandler(0L) {
      def handleDbUpdate(message: ApiMessage) = ???
      def handleUpdate(message: ApiMessage) = ???
    }

    protected def sendGetPreview(previewMaker: ActorRef, url: String)(implicit probe: TestProbe) = {
      previewMaker.tell(
        GetPreview(url, new DummyUpdateHandler()),
        probe.ref
      )
    }

  }

}
