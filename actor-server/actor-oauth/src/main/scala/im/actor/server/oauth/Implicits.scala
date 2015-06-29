package im.actor.server.oauth

import akka.http.scaladsl.unmarshalling.PredefinedFromEntityUnmarshallers._
import akka.http.scaladsl.unmarshalling._
import akka.stream.Materializer
import play.api.libs.functional.syntax._
import play.api.libs.json.{ JsPath, Json, Reads }

trait Implicits {

  implicit val materializer: Materializer

  implicit val tokenReads: Reads[Token] =
    ((JsPath \ "access_token").read[String] and
      (JsPath \ "token_type").read[String] and
      (JsPath \ "expires_in").read[Long] and
      (JsPath \ "refresh_token").readNullable[String])(createToken _)

  implicit val profileReads: Reads[Profile] =
    ((JsPath \ "email").read[String] and
      (JsPath \ "family_name").readNullable[String] and
      (JsPath \ "name").readNullable[String] and
      (JsPath \ "given_name").readNullable[String] and
      (JsPath \ "picture").readNullable[String] and
      (JsPath \ "gender").readNullable[String] and
      (JsPath \ "locale").readNullable[String])(Profile)

  private def createToken(accessToken: String, tokenType: String, expiresIn: Long, refreshToken: Option[String]) =
    Token(accessToken, tokenType, expiresIn, refreshToken)

  implicit val toOAuthToken: FromResponseUnmarshaller[Option[Token]] = Unmarshaller { implicit ec ⇒ resp ⇒
    Unmarshal(resp.entity).to[String].map { body ⇒
      Json.parse(body).validate[Token].fold(errors ⇒ None, token ⇒ Some(token))
    }
  }

  implicit val toProfile: FromResponseUnmarshaller[Option[Profile]] = Unmarshaller { implicit ec ⇒ resp ⇒
    Unmarshal(resp.entity).to[String].map { body ⇒
      Json.parse(body).validate[Profile].fold(errors ⇒ None, profile ⇒ Some(profile))
    }
  }
}
