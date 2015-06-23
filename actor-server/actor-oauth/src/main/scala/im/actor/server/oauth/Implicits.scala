package im.actor.server.oauth

import akka.http.scaladsl.unmarshalling.PredefinedFromEntityUnmarshallers._
import akka.http.scaladsl.unmarshalling._
import akka.stream.FlowMaterializer
import play.api.libs.functional.syntax._
import play.api.libs.json.{ JsPath, Json, Reads }

trait Implicits {

  implicit val flowMaterializer: FlowMaterializer

  implicit val tokenReads: Reads[Token] =
    ((JsPath \ "access_token").read[String] and
      (JsPath \ "token_type").read[String] and
      (JsPath \ "expires_in").read[Long] and
      (JsPath \ "refresh_token").read[String])(createToken _)

  private def createToken(accessToken: String, tokenType: String, expiresIn: Long, refreshToken: String) =
    Token(accessToken, tokenType, expiresIn, refreshToken)

  implicit val toOAuthToken: FromResponseUnmarshaller[Option[Token]] = Unmarshaller { implicit ec ⇒ resp ⇒
    Unmarshal(resp.entity).to[String].map { body ⇒
      Json.parse(body).validate[Token].fold(errors ⇒ None, token ⇒ Some(token))
    }
  }
}
