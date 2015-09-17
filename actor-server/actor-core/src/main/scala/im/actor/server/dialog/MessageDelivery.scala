package im.actor.server.dialog

import akka.actor.ActorSystem
import im.actor.api.rpc.messaging.{ UpdateMessageSent, UpdateMessage, ApiMessage }
import im.actor.api.rpc.peers.ApiPeer
import im.actor.server.db.DbExtension
import im.actor.server.messaging.PushText
import im.actor.server.misc.UpdateCounters
import im.actor.server.sequence.{ SeqUpdatesExtension, SeqState, SeqUpdatesManager }
import im.actor.server.user.UserExtension

import slick.driver.PostgresDriver.api.Database
import scala.concurrent.{ ExecutionContext, Future }

class MessageDelivery()(implicit val system: ActorSystem) extends UpdateCounters with PushText {

  implicit val ec: ExecutionContext = system.dispatcher
  implicit val seqUpdatesExt: SeqUpdatesExtension = SeqUpdatesExtension(system)
  private val db: Database = DbExtension(system).db
  private val userExt = UserExtension(system)

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
      receiverAuthIds ← userExt.getAuthIds(receiverUserId)
      _ ← receiverAuthIds match {
        case Seq() ⇒ Future.successful(())
        case receiverAuthId +: _ ⇒
          for {
            senderUser ← userExt.getApiStruct(senderUserId, receiverUserId, receiverAuthId)
            senderName = senderUser.localName.getOrElse(senderUser.name)
            pushText ← getPushText(peer, receiverUserId, senderName, message)
            _ ← SeqUpdatesManager.persistAndPushUpdates(receiverAuthIds.toSet, receiverUpdate, Some(pushText), isFat, deliveryId = Some(s"msg_${peer.toString}_${randomId}"))
            counterUpdate ← db.run(getUpdateCountersChanged(receiverUserId))
            _ ← userExt.broadcastUserUpdate(receiverUserId, counterUpdate, None, isFat = false, deliveryId = Some(s"counter_${randomId}"))
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
      senderAuthIds ← userExt.getAuthIds(senderUserId) map (_.toSet)
      _ ← SeqUpdatesManager.persistAndPushUpdates(senderAuthIds filterNot (_ == senderAuthId), senderUpdate, None, isFat, deliveryId = Some(s"msg_${peer.toString}_${randomId}"))
    } yield ()

    val senderAuthIdUpdate = UpdateMessageSent(peer, randomId, timestamp)
    SeqUpdatesManager.persistAndPushUpdate(senderAuthId, senderAuthIdUpdate, None, isFat, deliveryId = Some(s"msgsent_${peer.toString}_${randomId}"))
  }

}
