package im.actor.server.dialog

import akka.actor.ActorSystem
import akka.http.scaladsl.util.FastFuture
import im.actor.api.rpc.PeersImplicits
import im.actor.api.rpc.counters.{ ApiAppCounters, UpdateCountersChanged }
import im.actor.api.rpc.messaging._
import im.actor.server.db.DbExtension
import im.actor.server.messaging.PushText
import im.actor.server.model.Peer
import im.actor.server.sequence.{ PushData, PushRules, SeqState, SeqUpdatesExtension }
import im.actor.server.user.UserExtension

import scala.concurrent.{ ExecutionContext, Future }

//default extension
final class ActorDelivery()(implicit val system: ActorSystem)
  extends DeliveryExtension
  with PushText
  with PeersImplicits {

  implicit val ec: ExecutionContext = system.dispatcher
  implicit val seqUpdatesExt: SeqUpdatesExtension = SeqUpdatesExtension(system)
  private val userExt = UserExtension(system)
  private val dialogExt = DialogExtension(system)

  override def receiverDelivery(
    receiverUserId: Int,
    senderUserId:   Int,
    peer:           Peer,
    randomId:       Long,
    timestamp:      Long,
    message:        ApiMessage,
    isFat:          Boolean
  ): Future[Unit] = {
    val receiverUpdate = UpdateMessage(
      peer = peer.asStruct,
      senderUserId = senderUserId,
      date = timestamp,
      randomId = randomId,
      message = message,
      attributes = None,
      quotedMessage = None
    )

    for {
      senderName ← userExt.getName(senderUserId, receiverUserId)
      (pushText, censoredPushText) ← getPushText(peer, receiverUserId, senderName, message)
      _ ← seqUpdatesExt.deliverSingleUpdate(
        receiverUserId,
        receiverUpdate,
        PushRules(isFat = isFat).withData(
          PushData()
            .withText(pushText)
            .withCensoredText(censoredPushText)
            .withPeer(peer)
        ),
        deliveryId = s"msg_${peer.toString}_$randomId"
      )
    } yield ()
  }

  override def sendCountersUpdate(userId: Int): Future[Unit] =
    for {
      counter ← dialogExt.getUnreadTotal(userId)
      _ ← sendCountersUpdate(userId, counter)
    } yield ()

  override def sendCountersUpdate(userId: Int, counter: Int): Future[Unit] = {
    val counterUpdate = UpdateCountersChanged(ApiAppCounters(Some(counter)))
    seqUpdatesExt.deliverSingleUpdate(userId, counterUpdate, reduceKey = Some("counters_changed")) map (_ ⇒ ())
  }

  override def senderDelivery(
    senderUserId:  Int,
    senderAuthSid: Int,
    peer:          Peer,
    randomId:      Long,
    timestamp:     Long,
    message:       ApiMessage,
    isFat:         Boolean
  ): Future[SeqState] = {
    val apiPeer = peer.asStruct
    val senderUpdate = UpdateMessage(
      peer = apiPeer,
      senderUserId = senderUserId,
      date = timestamp,
      randomId = randomId,
      message = message,
      attributes = None,
      quotedMessage = None
    )

    val senderClientUpdate = UpdateMessageSent(apiPeer, randomId, timestamp)

    seqUpdatesExt.deliverMappedUpdate(
      userId = senderUserId,
      default = Some(senderUpdate),
      custom = Map(senderAuthSid → senderClientUpdate),
      pushRules = PushRules(isFat = isFat, excludeAuthSids = Seq(senderAuthSid)),
      deliveryId = s"msg_${peer.toString}_$randomId"
    )
  }

  override def notifyReceive(userId: Int, peer: Peer, date: Long, now: Long): Future[Unit] = {
    val update = UpdateMessageReceived(peer.asStruct, date, now)
    userExt.broadcastUserUpdate(
      userId,
      update,
      None,
      isFat = false,
      reduceKey = Some(s"receive_${peer.toString}"),
      deliveryId = None
    ) map (_ ⇒ ())
  }

  override def notifyRead(userId: Int, peer: Peer, date: Long, now: Long): Future[Unit] = {
    val update = UpdateMessageRead(peer.asStruct, date, now)
    seqUpdatesExt.deliverSingleUpdate(
      userId = userId,
      update = update,
      reduceKey = Some(s"read_${peer.toString}")
    ) map (_ ⇒ ())
  }

  override def read(readerUserId: Int, readerAuthSid: Int, peer: Peer, date: Long, unreadCount: Int): Future[Unit] =
    for {
      _ ← seqUpdatesExt.deliverSingleUpdate(
        userId = readerUserId,
        update = UpdateMessageReadByMe(peer.asStruct, date, Some(unreadCount)),
        pushRules = PushRules(),
        reduceKey = Some(s"read_by_me_${peer.toString}")
      )
    } yield ()

}
