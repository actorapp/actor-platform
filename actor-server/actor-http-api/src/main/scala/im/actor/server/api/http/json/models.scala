package im.actor.server.api.http.json

sealed trait Content
case class Text(text: String) extends Content
case class Image(imageUrl: String) extends Content
case class Document(documentUrl: String) extends Content

case class Group(title: String, avatars: Option[AvatarUrls])
case class User(name: String, avatars: Option[AvatarUrls])
case class GroupInviteInfo(group: Group, inviter: User)
case class AvatarUrls(small: Option[String], large: Option[String], full: Option[String])

case class Errors(message: String)

case class ReverseHook(url: String)