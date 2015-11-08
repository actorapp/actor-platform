package im.actor.server.dialog

import akka.actor.ActorSystem
import im.actor.api.rpc.Implicits._
import im.actor.api.rpc.messaging._
import im.actor.api.rpc.peers.{ ApiPeerType, ApiPeer }
import im.actor.server.db.DbExtension
import im.actor.server.messaging.PushText
import im.actor.server.misc.UpdateCounters
import im.actor.server.sequence.{ PushData, PushRules, SeqUpdatesExtension, SeqState }
import im.actor.server.user.UserExtension

import slick.driver.PostgresDriver.api.Database
import scala.concurrent.{ ExecutionContext, Future }

// default extension
private[dialog] final class ActorDelivery()(implicit val system: ActorSystem) extends DeliveryExtension with UpdateCounters with PushText {

  implicit val ec: ExecutionContext = system.dispatcher
  implicit val seqUpdatesExt: SeqUpdatesExtension = SeqUpdatesExtension(system)
  private val db: Database = DbExtension(system).db
  private val userExt = UserExtension(system)

  override def receiverDelivery(
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
      senderName ← userExt.getName(receiverUserId, senderUserId)
      pushText ← getPushText(peer, receiverUserId, senderName, message)
      _ ← seqUpdatesExt.deliverSingleUpdate(
        receiverUserId,
        receiverUpdate,
        PushRules(isFat = isFat).withData(PushData().withText(pushText).withPeer(peer.asModel)),
        deliveryId = s"msg_${peer.toString}_$randomId"
      )
      counterUpdate ← db.run(getUpdateCountersChanged(receiverUserId))
      _ ← seqUpdatesExt.deliverSingleUpdate(receiverUserId, counterUpdate, deliveryId = s"counter_$randomId")
    } yield ()
  }

  override def senderDelivery(
    senderUserId:  Int,
    senderAuthSid: Int,
    peer:          ApiPeer,
    randomId:      Long,
    timestamp:     Long,
    message:       ApiMessage,
    isFat:         Boolean
  ): Future[SeqState] = {
    val senderUpdate = UpdateMessage(
      peer = peer,
      senderUserId = senderUserId,
      date = timestamp,
      randomId = randomId,
      message = message
    )

    val senderClientUpdate = UpdateMessageSent(peer, randomId, timestamp)

    seqUpdatesExt.deliverMappedUpdate(
      userId = senderUserId,
      default = Some(senderUpdate),
      custom = Map(senderAuthSid → senderClientUpdate),
      pushRules = PushRules(isFat = isFat),
      deliveryId = s"msg_${peer.toString}_$randomId"
    )
  }

  override def authorRead(readerUserId: Int, authorUserId: Int, date: Long, now: Long): Future[Unit] = {
    val update = UpdateMessageRead(ApiPeer(ApiPeerType.Private, readerUserId), date, now)
    seqUpdatesExt.deliverSingleUpdate(
      userId = authorUserId,
      update = update
    ) map (_ ⇒ ())
  }

  override def readerRead(readerUserId: Int, readerAuthSid: Int, authorUserId: Int, date: Long): Future[Unit] = {
    val update = UpdateMessageReadByMe(ApiPeer(ApiPeerType.Private, authorUserId), date)
    for {
      counterUpdate ← db.run(getUpdateCountersChanged(readerUserId))
      _ ← seqUpdatesExt.deliverSingleUpdate(
        userId = readerUserId,
        update = update,
        pushRules = PushRules(excludeAuthSids = Seq(readerAuthSid))
      )
      _ ← seqUpdatesExt.deliverSingleUpdate(
        userId = readerUserId,
        update = counterUpdate
      )
    } yield ()
  }

}
