package im.actor.server.dialog

import im.actor.api.rpc.messaging.ApiMessage
import im.actor.api.rpc.peers.ApiPeer
import im.actor.server.sequence.SeqState

import scala.concurrent.Future

trait DeliveryExtension {

  def receiverDelivery(
    receiverUserId: Int,
    senderUserId:   Int,
    peer:           ApiPeer,
    randomId:       Long,
    timestamp:      Long,
    message:        ApiMessage,
    isFat:          Boolean
  ): Future[Unit]

  def senderDelivery(
    senderUserId: Int,
    senderAuthId: Long,
    peer:         ApiPeer,
    randomId:     Long,
    timestamp:    Long,
    message:      ApiMessage,
    isFat:        Boolean
  ): Future[SeqState]

  def authorRead(readerUserId: Int, authorUserId: Int, date: Long, now: Long): Future[Unit]

  def readerRead(readerUserId: Int, readerAuthId: Long, authorUserId: Int, date: Long): Future[Unit]

}