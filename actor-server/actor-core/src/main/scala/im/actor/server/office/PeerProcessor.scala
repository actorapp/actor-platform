package im.actor.server.office

import im.actor.api.rpc.counters.{ AppCounters, UpdateCountersChanged }
import im.actor.api.rpc.messaging._
import im.actor.api.rpc.peers.{ Peer, PeerType }
import im.actor.server.{ persist ⇒ p }
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext

object PeerProcessor {
  case object MessageSentComplete extends Serializable
}

trait PeerProcessor[State <: ProcessorState, Event <: AnyRef] extends Processor[State, Event] {

  private implicit val ec: ExecutionContext = context.dispatcher

  protected var lastReadDate: Option[Long] = None
  protected var lastReceiveDate: Option[Long] = None

  protected def getPushText(message: Message, clientName: String, outUser: Int): String = {
    message match {
      case TextMessage(text, _, _) ⇒
        formatAuthored(clientName, text)
      case dm: DocumentMessage ⇒
        dm.ext match {
          case Some(_: DocumentExPhoto) ⇒
            formatAuthored(clientName, "Photo")
          case Some(_: DocumentExVideo) ⇒
            formatAuthored(clientName, "Video")
          case _ ⇒
            formatAuthored(clientName, dm.name)
        }
      case unsupported ⇒ ""
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