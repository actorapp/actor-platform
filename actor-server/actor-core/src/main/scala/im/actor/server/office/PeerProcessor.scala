package im.actor.server.office

import im.actor.api.rpc.counters.{ UpdateCountersChanged, AppCounters }
import im.actor.api.rpc.messaging._
import im.actor.api.rpc.peers.{ Peer, PeerType }
import im.actor.server.models
import im.actor.server.{ persist ⇒ p }
import im.actor.server.util.ContactsUtils
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext

object PeerProcessor {
  case object MessageSentComplete extends Serializable
}

trait PeerProcessor[State <: ProcessorState, Event <: AnyRef] extends Processor[State, Event] {

  import ContactsUtils._

  private implicit val ec: ExecutionContext = context.dispatcher

  protected var lastReadDate: Option[Long] = None
  protected var lastReceiveDate: Option[Long] = None

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

  type AuthIdRandomId = (Long, Long)

  protected def formatAuthored(authorName: String, message: String): String = s"${authorName}: ${message}"

  protected def privatePeerStruct(userId: Int): Peer = Peer(PeerType.Private, userId)

  protected def groupPeerStruct(groupId: Int): Peer = Peer(PeerType.Group, groupId)

  protected def getUpdateCountersChanged(userId: Int): DBIO[UpdateCountersChanged] = for {
    unreadTotal ← p.HistoryMessage.getUnreadTotal(userId)
    unreadOpt = if (unreadTotal == 0) None else Some(unreadTotal)
  } yield UpdateCountersChanged(AppCounters(unreadOpt))

}