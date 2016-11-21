package im.actor.server.dialog

import akka.actor.ActorSystem
import im.actor.api.rpc.messaging.ApiMessage
import im.actor.server.sequence.SeqState
import im.actor.server.model.Peer

import scala.concurrent.Future

abstract class DeliveryExtension(system: ActorSystem, extData: Array[Byte]) {

  def receiverDelivery(
    receiverUserId: Int,
    senderUserId:   Int,
    peer:           Peer,
    randomId:       Long,
    timestamp:      Long,
    message:        ApiMessage,
    isFat:          Boolean,
    deliveryTag:    Option[String]
  ): Future[Unit]

  def senderDelivery(
    senderUserId: Int,
    senderAuthId: Option[Long],
    peer:         Peer,
    randomId:     Long,
    timestamp:    Long,
    message:      ApiMessage,
    isFat:        Boolean,
    deliveryTag:  Option[String]
  ): Future[SeqState]

  def sendCountersUpdate(userId: Int): Future[Unit]

  def sendCountersUpdate(userId: Int, counter: Int): Future[Unit]

  def notifyReceive(authorUserId: Int, peer: Peer, date: Long, now: Long): Future[Unit]

  def notifyRead(userId: Int, peer: Peer, date: Long, now: Long): Future[Unit]

  def read(readerUserId: Int, readerAuthId: Long, peer: Peer, date: Long, unreadCount: Int): Future[Unit]

}
