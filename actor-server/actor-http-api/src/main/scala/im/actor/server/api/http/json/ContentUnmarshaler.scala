package im.actor.server.api.http.json

import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.unmarshalling.Unmarshaller
import akka.stream.FlowMaterializer
import akka.stream.scaladsl.Sink
import play.api.libs.json.Json

import im.actor.server.api.http.json.JsonImplicits.textReads

trait ContentUnmarshaler {

  implicit val flowMaterializer: FlowMaterializer

  implicit val toContent = Unmarshaller.apply[HttpRequest, Content] { implicit ec ⇒ req ⇒
    req.entity.dataBytes
      .map { data ⇒ Json.parse(data.decodeString("utf-8")).as[Content] }
      .runWith(Sink.head)
  }

}
