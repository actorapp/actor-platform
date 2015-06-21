package im.actor.server.push

import scala.concurrent.ExecutionContext

import slick.dbio.Effect.Read
import slick.dbio.{ NoStream, DBIOAction }

import im.actor.api.rpc.peers.{ PeerType, Peer }
import im.actor.server.persist

private[push] trait VendorPush {
  protected def getShowText(userId: Int, paramBase: String)(implicit ec: ExecutionContext): DBIOAction[Boolean, NoStream, Read] = {
    persist.configs.Parameter.findValue(userId, s"${paramBase}.show_text") map {
      case Some("true")  ⇒ true
      case Some("false") ⇒ false
      case _             ⇒ true
    }
  }

  protected def getChatNotificationEnabled(userId: Int, paramBase: String, originPeer: Peer)(implicit ec: ExecutionContext): DBIOAction[Boolean, NoStream, Read] = {
    val peerStr = originPeer.`type` match {
      case PeerType.Private ⇒ s"PRIVATE_${originPeer.id}"
      case PeerType.Group   ⇒ s"GROUP_${originPeer.id}"
    }

    persist.configs.Parameter.findValue(userId, s"${paramBase}.chat.${peerStr}.enabled") map {
      case Some("true")  ⇒ true
      case Some("false") ⇒ false
      case _             ⇒ true
    }
  }
}