package im.actor.server.api.http.json

import io.circe._
import io.circe.generic.semiauto._

trait JsonEncoders {
  implicit val endpointsFormat = deriveFor[Endpoints].encoder
  implicit val serverInfoFormat = deriveFor[ServerInfo].encoder
}
