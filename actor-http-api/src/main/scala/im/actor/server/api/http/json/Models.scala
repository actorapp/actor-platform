package im.actor.server.api.http.json

sealed trait Content
case class Text(text: String) extends Content
case class Image(imageUrl: String) extends Content
case class Document(documentUrl: String) extends Content

case class GroupInviteInfo(
  groupTitle:     String,
  groupAvatars:   Option[AvatarUrls],
  inviterName:    String,
  inviterAvatars: Option[AvatarUrls]
)
case class AvatarUrls(small: Option[String], large: Option[String], full: Option[String])
case class Errors(message: String)