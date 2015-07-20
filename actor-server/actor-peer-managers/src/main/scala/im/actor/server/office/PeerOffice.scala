package im.actor.server.office

import akka.actor.ActorLogging
import akka.persistence.PersistentActor
import im.actor.api.rpc.messaging._
import im.actor.api.rpc.peers.{ PeerType, Peer }
import im.actor.server.models
import im.actor.server.util.ContactsUtils
import im.actor.server.util.ContactsUtils._
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext

trait PeerOffice extends Office {

  import ContactsUtils._

  implicit private val ec: ExecutionContext = context.dispatcher

  protected def getPushText(message: Message, clientUser: models.User, outUser: Int) = {
    message match {
      case TextMessage(text, _, _) ⇒
        for (localName ← getLocalNameOrDefault(outUser, clientUser))
          yield formatAuthored(localName, text)
      case dm: DocumentMessage ⇒
        getLocalNameOrDefault(outUser, clientUser) map { localName ⇒
          dm.ext match {
            case Some(_: DocumentExPhoto) ⇒
              formatAuthored(localName, "Photo")
            case Some(_: DocumentExVideo) ⇒
              formatAuthored(localName, "Video")
            case _ ⇒
              formatAuthored(localName, dm.name)
          }
        }
      case unsupported ⇒
        DBIO.successful("")
    }
  }

  protected def formatAuthored(authorName: String, message: String): String = s"${authorName}: ${message}"

  protected def privatePeerStruct(userId: Int): Peer = Peer(PeerType.Private, userId)

  protected def groupPeerStruct(groupId: Int): Peer = Peer(PeerType.Group, groupId)
}