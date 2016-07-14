package im.actor.server.dialog

import akka.actor.ActorSystem
import im.actor.api.rpc.PeersImplicits
import im.actor.api.rpc.counters.{ ApiAppCounters, UpdateCountersChanged }
import im.actor.api.rpc.messaging._
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

  private implicit val ec: ExecutionContext = system.dispatcher
  private val seqUpdExt: SeqUpdatesExtension = SeqUpdatesExtension(system)

  override def receiverDelivery(
    receiverUserId: Int,
    senderUserId:   Int,
    peer:           Peer,
    randomId:       Long,
    timestamp:      Long,
    message:        ApiMessage,
    isFat:          Boolean,
    deliveryTag:    Option[String]
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
      senderName ← UserExtension(system).getName(senderUserId, receiverUserId)
      (pushText, censoredPushText) ← getPushText(peer, receiverUserId, senderName, message)
      _ ← seqUpdExt.deliverUserUpdate(
        receiverUserId,
        receiverUpdate,
        PushRules(isFat = isFat).withData(
          PushData()
            .withText(pushText)
            .withCensoredText(censoredPushText)
            .withPeer(peer)
        ),
        deliveryId = seqUpdExt.msgDeliveryId(peer, randomId),
        deliveryTag = deliveryTag
      )
    } yield ()
  }

  override def sendCountersUpdate(userId: Int): Future[Unit] =
    for {
      counter ← DialogExtension(system).getUnreadTotal(userId)
      _ ← sendCountersUpdate(userId, counter)
    } yield ()

  override def sendCountersUpdate(userId: Int, counter: Int): Future[Unit] = {
    val counterUpdate = UpdateCountersChanged(ApiAppCounters(Some(counter)))
    seqUpdExt.deliverUserUpdate(userId, counterUpdate, reduceKey = Some("counters_changed")) map (_ ⇒ ())
  }

  override def senderDelivery(
    senderUserId: Int,
    senderAuthId: Option[Long],
    peer:         Peer,
    randomId:     Long,
    timestamp:    Long,
    message:      ApiMessage,
    isFat:        Boolean,
    deliveryTag:  Option[String]
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

    seqUpdExt.deliverCustomUpdate(
      userId = senderUserId,
      authId = senderAuthId getOrElse 0L,
      default = Some(senderUpdate),
      custom = senderAuthId map (authId ⇒ Map(authId → senderClientUpdate)) getOrElse Map.empty,
      pushRules = PushRules(isFat = isFat, excludeAuthIds = senderAuthId.toSeq),
      deliveryId = seqUpdExt.msgDeliveryId(peer, randomId),
      deliveryTag = deliveryTag
    )
  }

  override def notifyReceive(userId: Int, peer: Peer, date: Long, now: Long): Future[Unit] = {
    val update = UpdateMessageReceived(peer.asStruct, date, now)
    seqUpdExt.deliverUserUpdate(
      userId,
      update,
      pushRules = seqUpdExt.pushRules(isFat = false, None),
      reduceKey = Some(reduceKey("receive", peer))
    ) map (_ ⇒ ())
  }

  override def notifyRead(userId: Int, peer: Peer, date: Long, now: Long): Future[Unit] = {
    val update = UpdateMessageRead(peer.asStruct, date, now)
    seqUpdExt.deliverUserUpdate(
      userId = userId,
      update = update,
      reduceKey = Some(reduceKey("read", peer))
    ) map (_ ⇒ ())
  }

  override def read(readerUserId: Int, readerAuthId: Long, peer: Peer, date: Long, unreadCount: Int): Future[Unit] =
    for {
      _ ← seqUpdExt.deliverClientUpdate(
        userId = readerUserId,
        authId = readerAuthId,
        update = UpdateMessageReadByMe(peer.asStruct, date, Some(unreadCount)),
        reduceKey = Some(reduceKey("read_by_me", peer))
      )
    } yield ()

  private def reduceKey(prefix: String, peer: Peer): String =
    s"${prefix}_${peer.`type`.value}_${peer.id}"

}
