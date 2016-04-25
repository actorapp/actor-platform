package im.actor.server.file.local.http

import java.io.File

import akka.http.scaladsl.model.headers.EntityTag
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.{ ContentType, DateTime, HttpEntity }
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.BasicDirectives.{ extractSettings ⇒ _, pass ⇒ _, _ }
import akka.http.scaladsl.server.directives.CacheConditionDirectives.{ conditional ⇒ _, _ }
import akka.http.scaladsl.server.directives.{ BasicDirectives, CodingDirectives, ContentTypeResolver, RangeDirectives }
import akka.stream.ActorAttributes
import akka.stream.scaladsl.FileIO

//TODO: remove as soon, as https://github.com/akka/akka/issues/20338 get fixed
trait GetFileFix {

  private val withRangeSupportAndPrecompressedMediaTypeSupportAndExtractSettings =
    RangeDirectives.withRangeSupport &
      CodingDirectives.withPrecompressedMediaTypeSupport &
      BasicDirectives.extractSettings

  def getFromFileFix(file: File)(implicit resolver: ContentTypeResolver): Route =
    getFromFile(file, resolver(file.getName))

  private def getFromFile(file: File, contentType: ContentType): Route =
    get {
      if (file.isFile && file.canRead)
        conditionalFor(file.length, file.lastModified) {
          if (file.length > 0) {
            withRangeSupportAndPrecompressedMediaTypeSupportAndExtractSettings { settings ⇒
              complete {
                HttpEntity.Chunked.fromData(
                  contentType,
                  FileIO.fromFile(file).withAttributes(ActorAttributes.dispatcher(settings.fileIODispatcher))
                )
              }
            }
          } else complete(HttpEntity.Empty)
        }
      else reject
    }

  private def conditionalFor(length: Long, lastModified: Long): Directive0 =
    extractSettings.flatMap(settings ⇒
      if (settings.fileGetConditional) {
        val tag = java.lang.Long.toHexString(lastModified ^ java.lang.Long.reverse(length))
        val lastModifiedDateTime = DateTime(math.min(lastModified, System.currentTimeMillis))
        conditional(EntityTag(tag), lastModifiedDateTime)
      } else pass)

}
