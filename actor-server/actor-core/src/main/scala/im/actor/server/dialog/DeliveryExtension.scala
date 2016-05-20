package im.actor.server.dialog

import im.actor.api.rpc.messaging.ApiMessage
import im.actor.server.sequence.SeqState
import im.actor.server.model.Peer

import scala.concurrent.Future

trait DeliveryExtension {

  def receiverDelivery(
    receiverUserId: Int,
    senderUserId:   Int,
    peer:           Peer,
    randomId:       Long,
    timestamp:      Long,
    message:        ApiMessage,
    isFat:          Boolean
  ): Future[Unit]

  def senderDelivery(
    senderUserId:  Int,
    senderAuthSid: Int,
    peer:          Peer,
    randomId:      Long,
    timestamp:     Long,
    message:       ApiMessage,
    isFat:         Boolean
  ): Future[SeqState]

  def sendCountersUpdate(userId: Int): Future[Unit]

  def sendCountersUpdate(userId: Int, counter: Int): Future[Unit]

  def notifyReceive(authorUserId: Int, peer: Peer, date: Long, now: Long): Future[Unit]

  def notifyRead(userId: Int, peer: Peer, date: Long, now: Long): Future[Unit]

  def read(readerUserId: Int, readerAuthSid: Int, peer: Peer, date: Long, unreadCount: Int): Future[Unit]

}