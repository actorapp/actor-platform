package im.actor.server.push

import akka.actor._

import im.actor.api.rpc.codecs.UpdateBoxCodec
import im.actor.api.rpc.sequence.SeqUpdate
import im.actor.server.models
import im.actor.server.mtproto.protocol.UpdateBox

object SeqUpdatesPusher {
  def props(seqUpdatesManagerRegion: ActorRef, authId: Long, session: ActorRef) =
    Props(classOf[SeqUpdatesPusher], seqUpdatesManagerRegion, authId, session)
}

private[push] class SeqUpdatesPusher(seqUpdatesManagerRegion: ActorRef, authId: Long, session: ActorRef) extends Actor with ActorLogging {

  import SeqUpdatesManager._
  import im.actor.server.session.SessionMessage._

  override def preStart(): Unit = {
    seqUpdatesManagerRegion ! Envelope(authId, Subscribe(self))
  }

  def receive = {
    case SubscribeAck(ref) if ref == self =>
      log.debug("Subscribed to updates of authId: {}", authId)
    case update: models.sequence.SeqUpdate =>
      val protoUpdate = SeqUpdate(update.seq, timestampToBytes(update.timestamp), update.header, update.serializedData)
      val ub = UpdateBox(UpdateBoxCodec.encode(protoUpdate).require)
      session ! SendProtoMessage(ub)
  }
}