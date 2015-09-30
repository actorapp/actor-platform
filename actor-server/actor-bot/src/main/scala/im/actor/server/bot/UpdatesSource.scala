package im.actor.server.bot

import akka.actor.{ ActorLogging, Props }
import akka.pattern.pipe
import akka.stream.actor.ActorPublisher
import akka.stream.scaladsl.Source
import im.actor.api.rpc.codecs._
import im.actor.api.rpc.messaging.{ ApiTextMessage, UpdateMessage }
import im.actor.api.rpc.sequence.SeqUpdate
import im.actor.bot.BotMessages
import im.actor.bot.BotMessages.{ OutPeer, BotUpdate }
import im.actor.server.acl.ACLUtils
import im.actor.server.mtproto.protocol.UpdateBox
import im.actor.server.presences.{ GroupPresenceManager, PresenceManager }
import im.actor.server.sequence.{ UpdatesConsumer, WeakUpdatesManager }

import scala.annotation.tailrec

private[bot] object UpdatesSource {
  def source(authId: Long) = Source.actorPublisher[BotUpdate](props(authId))

  def props(authId: Long) = Props(classOf[UpdatesSource], authId)

  private final case class Enqueue(upd: BotUpdate)
}

private class UpdatesSource(authId: Long) extends ActorPublisher[BotUpdate] with ActorLogging {

  import UpdatesSource._
  import BotMessages.TextMessage
  import akka.stream.actor.ActorPublisherMessage._
  import context._
  import im.actor.server.sequence.NewUpdate

  private implicit val weakUpdatesManagerRegion = WeakUpdatesManager.startRegionProxy()
  private implicit val presenceManagerRegion = PresenceManager.startRegionProxy()
  private implicit val groupPresenceManagerRegion = GroupPresenceManager.startRegionProxy()

  context.actorOf(UpdatesConsumer.props(authId, self), "updatesConsumer")

  private var buf = Vector.empty[BotMessages.BotUpdate]

  def receive = {
    case Enqueue(upd) ⇒ enqueue(upd)
    case NewUpdate(UpdateBox(bodyBytes), _) ⇒
      UpdateBoxCodec.decode(bodyBytes).require.value match {
        case SeqUpdate(_, _, header, body) ⇒
          header match {
            case UpdateMessage.header ⇒
              UpdateMessage.parseFrom(body) match {
                case Right(upd) ⇒
                  upd.message match {
                    case ApiTextMessage(message, _, _) ⇒
                      log.debug("Received ApiTextMessage")

                      (for {
                        apiOutPeer ← ACLUtils.getOutPeer(upd.peer, authId)
                      } yield Enqueue(TextMessage(
                        peer = OutPeer(apiOutPeer.`type`.id, apiOutPeer.id, apiOutPeer.accessHash),
                        senderUserId = upd.senderUserId,
                        date = upd.date,
                        randomId = upd.randomId,
                        text = message
                      ))) pipeTo self

                    case _ ⇒
                      log.debug("Received non-text message, ignoring")
                  }
                case Left(e) ⇒
                  log.error(e, "Failed to parse UpdateMessage")
              }
            case _ ⇒
              log.debug("Received SeqUpdate with header: {}, ignoring", header)
          }
        case _ ⇒
      }
    case Request(_) ⇒
      deliverBuf()
    case Cancel ⇒
      log.warning("Cancelling")
      context.stop(self)
  }

  private def enqueue(upd: BotMessages.BotUpdate): Unit = {
    log.debug("Enqueuing {}", upd)

    if (buf.isEmpty && totalDemand > 0) {
      onNext(upd)
    } else {
      buf :+= upd
      deliverBuf()
    }
  }

  @tailrec final def deliverBuf(): Unit =
    if (totalDemand > 0) {
      if (totalDemand <= Int.MaxValue) {
        val (use, keep) = buf.splitAt(totalDemand.toInt)
        buf = keep
        use foreach onNext
      } else {
        val (use, keep) = buf.splitAt(Int.MaxValue)
        buf = keep
        use foreach onNext
        deliverBuf()
      }
    }
}