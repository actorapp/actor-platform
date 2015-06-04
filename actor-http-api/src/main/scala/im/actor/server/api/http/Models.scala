package im.actor.server.api.http

import play.api.libs.functional.syntax._
import play.api.libs.json._

sealed trait Content
case class Text(text: String) extends Content
case class Image(imageUrl: String) extends Content
case class Document(documentUrl: String) extends Content

case class GroupInviteInfo(
  groupTitle:       String,
  groupAvatarUrl:   Option[String],
  inviterName:      String,
  inviterAvatarUrl: Option[String]
)
case class Errors(message: String)

object JsonImplicits {

  implicit val textReads: Reads[Content] =
    (JsPath \ "text").read[String].map[Content] { Text } |
      (JsPath \ "document_url").read[String].map[Content] { Document } |
      (JsPath \ "image_url").read[String].map[Content] { Image }

  implicit val groupInviteInfoFormat: Format[GroupInviteInfo] = Json.format[GroupInviteInfo]
  implicit val errorsFormat: Format[Errors] = Json.format[Errors]

}