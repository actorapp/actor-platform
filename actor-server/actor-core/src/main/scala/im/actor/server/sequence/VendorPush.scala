package im.actor.server.sequence

import scala.concurrent.ExecutionContext

import slick.dbio.Effect.Read
import slick.dbio.{ DBIO, DBIOAction, NoStream }

import im.actor.api.rpc.peers.{ ApiPeer, ApiPeerType }
import im.actor.server.{ models, persist }

private[sequence] trait VendorPush {
  protected def setPushCredentials(creds: models.push.ApplePushCredentials): DBIO[Int] =
    persist.push.ApplePushCredentials.createOrUpdate(creds)

  protected def setPushCredentials(creds: models.push.GooglePushCredentials): DBIO[Int] =
    persist.push.GooglePushCredentials.createOrUpdate(creds)

  protected def deletePushCredentials(authId: Long)(implicit ec: ExecutionContext): DBIO[Int] =
    for {
      a ← persist.push.ApplePushCredentials.delete(authId)
      g ← persist.push.GooglePushCredentials.delete(authId)
    } yield a + g

  protected def getShowText(userId: Int, paramBase: String)(implicit ec: ExecutionContext): DBIOAction[Boolean, NoStream, Read] = {
    persist.configs.Parameter.findValue(userId, s"${paramBase}.show_text") map {
      case Some("true")  ⇒ true
      case Some("false") ⇒ false
      case _             ⇒ true
    }
  }

  protected def getChatNotificationEnabled(userId: Int, paramBase: String, originPeer: ApiPeer)(implicit ec: ExecutionContext): DBIOAction[Boolean, NoStream, Read] = {
    val peerStr = originPeer.`type` match {
      case ApiPeerType.Private ⇒ s"PRIVATE_${originPeer.id}"
      case ApiPeerType.Group   ⇒ s"GROUP_${originPeer.id}"
    }

    persist.configs.Parameter.findValue(userId, s"${paramBase}.chat.${peerStr}.enabled") map {
      case Some("true")  ⇒ true
      case Some("false") ⇒ false
      case _             ⇒ true
    }
  }
}