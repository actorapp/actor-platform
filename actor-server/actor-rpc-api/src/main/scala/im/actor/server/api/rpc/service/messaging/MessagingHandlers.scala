package im.actor.server.api.rpc.service.messaging

import scala.concurrent._
import scala.concurrent.duration._

import akka.util.Timeout
import org.joda.time.DateTime
import slick.driver.PostgresDriver.api._

import im.actor.api.rpc._
import im.actor.api.rpc.messaging._
import im.actor.api.rpc.misc._
import im.actor.api.rpc.peers._
import im.actor.server.group.{ GroupErrors, GroupOffice }
import im.actor.server.user.UserOffice

private[messaging] trait MessagingHandlers {
  self: MessagingServiceImpl ⇒

  import im.actor.api.rpc.Implicits._

  override implicit val ec: ExecutionContext = actorSystem.dispatcher
  private implicit val timeout: Timeout = Timeout(5.seconds) // TODO: configurable

  override def jhandleSendMessage(outPeer: OutPeer, randomId: Long, message: Message, clientData: ClientData): Future[HandlerResult[ResponseSeqDate]] = {
    val authorizedAction = requireAuth(clientData).map { implicit client ⇒
      val dateTime = new DateTime
      val dateMillis = dateTime.getMillis

      val seqstateAction = outPeer.`type` match {
        case PeerType.Private ⇒
          DBIO.from(UserOffice.sendMessage(outPeer.id, client.userId, client.authId, outPeer.accessHash, randomId, message))
        case PeerType.Group ⇒
          DBIO.from(GroupOffice.sendMessage(outPeer.id, client.userId, client.authId, outPeer.accessHash, randomId, message))
      }

      for (seqstate ← seqstateAction) yield {
        val fromPeer = Peer(PeerType.Private, client.userId)
        val toPeer = outPeer.asPeer
        onMessage(Events.PeerMessage(fromPeer.asModel, toPeer.asModel, randomId, dateMillis, message))
        Ok(ResponseSeqDate(seqstate.seq, seqstate.state.toByteArray, dateMillis))
      }
    }

    db.run(toDBIOAction(authorizedAction)) recover {
      case GroupErrors.InvalidAccessHash ⇒ Error(CommonErrors.InvalidAccessHash)
      case GroupErrors.NotAMember        ⇒ Error(CommonErrors.forbidden("You are not a group member."))
    }
  }
}
