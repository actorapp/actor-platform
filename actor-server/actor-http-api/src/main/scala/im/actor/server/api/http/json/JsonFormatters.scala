package im.actor.server.api.http.json

import play.api.libs.json._
import play.api.libs.functional.syntax._

object JsonFormatters {

  implicit val textReads: Reads[Content] =
    (JsPath \ "text").read[String].map[Content] { Text } |
      (JsPath \ "document_url").read[String].map[Content] { Document } |
      (JsPath \ "image_url").read[String].map[Content] { Image }

  implicit val avatarUrlsFormat: Format[AvatarUrls] = Json.format[AvatarUrls]
  implicit val userFormat: Format[InviterInfo] = Json.format[InviterInfo]
  implicit val groupFormat: Format[GroupInfo] = Json.format[GroupInfo]
  implicit val groupInviteInfoFormat: Format[GroupInviteInfo] = Json.format[GroupInviteInfo]
  implicit val errorsFormat: Format[Errors] = Json.format[Errors]

  implicit val reverseHookReads: Reads[ReverseHook] =
    (JsPath \ "target_url").read[String].map { ReverseHook } |
      (JsPath \ "url").read[String].map { ReverseHook }

  implicit val reverseHookResponseFormat: Format[ReverseHookResponse] = Json.format[ReverseHookResponse]
  implicit val statusFormat: Format[Status] = Json.format[Status]
}
