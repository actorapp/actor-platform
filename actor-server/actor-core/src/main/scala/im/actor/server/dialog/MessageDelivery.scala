package im.actor.server.dialog

import akka.actor.ActorSystem
import akka.util.Timeout
import im.actor.api.rpc.messaging.{ UpdateMessageSent, UpdateMessage, ApiMessage }
import im.actor.api.rpc.peers.ApiPeer
import im.actor.server.db.DbExtension
import im.actor.server.misc.{ UpdateCounters, PushText }
import im.actor.server.sequence.{ SeqUpdatesExtension, SeqState, SeqUpdatesManager }
import im.actor.server.user.{ UserExtension, UserViewRegion, UserOffice }

import slick.driver.PostgresDriver.api.Database
import scala.concurrent.{ ExecutionContext, Future }

class MessageDelivery()(implicit system: ActorSystem, timeout: Timeout) extends UpdateCounters with PushText {

  implicit val ec: ExecutionContext = system.dispatcher
  implicit val seqUpdatesExt: SeqUpdatesExtension = SeqUpdatesExtension(system)
  implicit val userViewRegion: UserViewRegion = UserExtension(system).viewRegion
  protected val db: Database = DbExtension(system).db

  def receiverDelivery(
    receiverUserId: Int,
    senderUserId:   Int,
    peer:           ApiPeer,
    randomId:       Long,
    timestamp:      Long,
    message:        ApiMessage,
    isFat:          Boolean
  ): Future[Unit] = {
    val receiverUpdate = UpdateMessage(
      peer = peer,
      senderUserId = senderUserId,
      date = timestamp,
      randomId = randomId,
      message = message
    )
    for {
      receiverAuthIds ← UserOffice.getAuthIds(receiverUserId)
      _ ← receiverAuthIds match {
        case Seq() ⇒ Future.successful(())
        case receiverAuthId +: _ ⇒
          for {
            senderUser ← UserOffice.getApiStruct(senderUserId, receiverUserId, receiverAuthId)
            senderName = senderUser.localName.getOrElse(senderUser.name)
            pushText ← getPushText(peer, receiverUserId, senderName, message)
            _ ← SeqUpdatesManager.persistAndPushUpdatesF(receiverAuthIds.toSet, receiverUpdate, Some(pushText), isFat, deliveryId = Some(s"msg_${peer.toString}_${randomId}"))
            counterUpdate ← db.run(getUpdateCountersChanged(receiverUserId))
            _ ← UserOffice.broadcastUserUpdate(receiverUserId, counterUpdate, None, isFat = false, deliveryId = Some(s"counter_${randomId}"))
          } yield ()
      }
    } yield ()
  }

  def senderDelivery(
    senderUserId: Int,
    senderAuthId: Long,
    peer:         ApiPeer,
    randomId:     Long,
    timestamp:    Long,
    message:      ApiMessage,
    isFat:        Boolean
  ): Future[SeqState] = {
    val senderUpdate = UpdateMessage(
      peer = peer,
      senderUserId = senderUserId,
      date = timestamp,
      randomId = randomId,
      message = message
    )
    for {
      senderAuthIds ← UserOffice.getAuthIds(senderUserId) map (_.toSet)
      _ ← SeqUpdatesManager.persistAndPushUpdatesF(senderAuthIds filterNot (_ == senderAuthId), senderUpdate, None, isFat, deliveryId = Some(s"msg_${peer.toString}_${randomId}"))
    } yield ()

    val senderAuthIdUpdate = UpdateMessageSent(peer, randomId, timestamp)
    SeqUpdatesManager.persistAndPushUpdateF(senderAuthId, senderAuthIdUpdate, None, isFat, deliveryId = Some(s"msgsent_${peer.toString}_${randomId}"))
  }

}
