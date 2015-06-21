package im.actor.server.api.http.json

import play.api.libs.json.{ Json, Format, JsPath, Reads }
import play.api.libs.functional.syntax._

object JsonImplicits {

  implicit val textReads: Reads[Content] =
    (JsPath \ "text").read[String].map[Content] { Text } |
      (JsPath \ "document_url").read[String].map[Content] { Document } |
      (JsPath \ "image_url").read[String].map[Content] { Image }

  implicit val avatarUrlsFormat: Format[AvatarUrls] = Json.format[AvatarUrls]
  implicit val userFormat: Format[User] = Json.format[User]
  implicit val groupFormat: Format[Group] = Json.format[Group]
  implicit val groupInviteInfoFormat: Format[GroupInviteInfo] = Json.format[GroupInviteInfo]
  implicit val errorsFormat: Format[Errors] = Json.format[Errors]

}