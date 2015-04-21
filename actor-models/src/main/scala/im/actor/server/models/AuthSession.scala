package im.actor.server.models

import scodec.bits._
import org.joda.time.DateTime

@SerialVersionUID(1L)
case class AuthSession(
  userId: Int, id: Int, authId: Long, appId: Int, appTitle: String, deviceTitle: String, deviceHash: Array[Byte],
  authTime: DateTime, authLocation: String, latitude: Option[Double], longitude: Option[Double]
)

object AuthSession {
  def appTitleOf(appId: Int) = appId match {
    case 0 => "Android Official"
    case 1 => "Android Official"
    case 2 => "iOS Official"
    case 3 => "Web Official"
    case 42 => "Tests"
    case _ => "Unknown"
  }
}
//  def build(
//    id: Int, appId: Int, deviceTitle: String, authTime: DateTime,
//    authLocation: String, latitude: Option[Double], longitude: Option[Double],
//    authId: Long, publicKeyHash: Long, deviceHash: BitVector
//  ): AuthSession = {
//    val appTitle = appTitleOf(appId)
//    AuthSession(
//      id = id, appId = appId, appTitle = appTitle, deviceTitle = deviceTitle, authTime = authTime,
//      authLocation = authLocation, latitude = latitude, longitude = longitude,
//      authId = authId, publicKeyHash = publicKeyHash, deviceHash = deviceHash
//    )
//  }
//}
