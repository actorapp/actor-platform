package im.actor.server.api.http.json

import akka.http.scaladsl.unmarshalling.PredefinedFromEntityUnmarshallers._
import akka.http.scaladsl.unmarshalling._
import akka.stream.Materializer
import play.api.libs.json.Json

trait ContentUnmarshaller {
  import im.actor.server.api.http.json.JsonFormatters.textReads

  implicit val materializer: Materializer

  implicit val toContent: FromRequestUnmarshaller[Content] = Unmarshaller { implicit ec ⇒ req ⇒
    Unmarshal(req.entity).to[String].map { body ⇒
      Json.parse(body).as[Content]
    }
  }

}

trait ReverseHookUnmarshaler {
  import im.actor.server.api.http.json.JsonFormatters.reverseHookFormat

  implicit val materializer: Materializer

  implicit val toReverseHook: FromRequestUnmarshaller[ReverseHook] = Unmarshaller { implicit ec ⇒ req ⇒
    Unmarshal(req.entity).to[String].map { body ⇒
      Json.parse(body).as[ReverseHook]
    }
  }
}
