package im.actor.server.model

import org.joda.time.DateTime

object DeviceType {

  val Desktop = "desktop"
  val Mobile = "mobile"
  val Tablet = "tablet"
  val Generic = "generic"

  def apply(appId: Int) = AuthSession.appCategory(appId) match {
    case "desktop" ⇒ Desktop
    case "mobile"  ⇒ Mobile
    case "tablet"  ⇒ Tablet
    case _         ⇒ Generic
  }
}

object AppId {
  val Mobile = 1
}

@SerialVersionUID(1L)
final case class AuthSession(
  userId:       Int,
  id:           Int,
  authId:       Long,
  appId:        Int,
  appTitle:     String,
  deviceTitle:  String,
  deviceHash:   Array[Byte],
  authTime:     DateTime,
  authLocation: String,
  latitude:     Option[Double],
  longitude:    Option[Double]
)

object AuthSession {
  def appTitleOf(appId: Int) = appId match {
    case 0  ⇒ "Android Official"
    case 1  ⇒ "Android Official"
    case 2  ⇒ "iOS Official"
    case 3  ⇒ "Web Official"
    case 42 ⇒ "Tests"
    case _  ⇒ "Unknown"
  }

  def appCategory(appId: Int) = appId match {
    case 0 | 1 | 2 ⇒ "mobile"
    case 3         ⇒ "desktop"
    case _         ⇒ "generic"
  }
}
