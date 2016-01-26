package im.actor.server.bot

import akka.actor.{ ActorLogging, Props, Stash }
import akka.stream.actor.ActorPublisher
import akka.stream.scaladsl.Source
import im.actor.api.rpc.Update
import im.actor.api.rpc.messaging.UpdateMessage
import im.actor.api.rpc.sequence.{ UpdateRawUpdate, FatSeqUpdate, SeqUpdate }
import im.actor.server.sequence.{ UpdatesConsumerMessage, UpdatesConsumer }

import scala.annotation.tailrec

private[bot] object UpdatesSource {
  private case class Initialized(userId: Int)

  def source(userId: Int, authId: Long, authSid: Int) = Source.actorPublisher[(Int, Update)](props(userId, authId, authSid))

  def props(userId: Int, authId: Long, authSid: Int) = Props(classOf[UpdatesSource], userId, authId, authSid)
}

private class UpdatesSource(userId: Int, authId: Long, authSid: Int) extends ActorPublisher[(Int, Update)] with ActorLogging with Stash {

  import akka.stream.actor.ActorPublisherMessage._
  import context._
  import im.actor.server.sequence.NewUpdate

  val consumer = context.actorOf(UpdatesConsumer.props(userId, authId, authSid, self), "updates-consumer")
  consumer ! UpdatesConsumerMessage.SubscribeToSeq

  private var buf = Vector.empty[(Int, Update)]

  def receive: Receive = {
    case NewUpdate(ub, _) ⇒
      (ub match {
        case SeqUpdate(seq, _, header, body)          ⇒ Some((seq, header, body))
        case FatSeqUpdate(seq, _, header, body, _, _) ⇒ Some((seq, header, body))
        case _                                        ⇒ None
      }) foreach {
        case (seq, header, body) ⇒
          header match {
            case UpdateMessage.header ⇒
              UpdateMessage.parseFrom(body) match {
                case Right(upd) ⇒
                  enqueue(seq, upd)
                case Left(e) ⇒
                  log.error(e, "Failed to parse UpdateMessage")
              }
            case UpdateRawUpdate.header ⇒
              UpdateRawUpdate.parseFrom(body) match {
                case Right(upd) ⇒
                  enqueue(seq, upd)
                case Left(e) ⇒
                  log.error(e, "Failed to parse UpdateRawUpdate")
              }
            case _ ⇒
              log.debug("Received SeqUpdate with header: {}, ignoring", header)
          }
      }
    case Request(_) ⇒
      deliverBuf()
    case Cancel ⇒
      log.warning("Cancelling")
      context.stop(self)
  }

  private def enqueue(seq: Int, upd: Update): Unit = {
    if (buf.isEmpty && totalDemand > 0) {
      onNext((seq, upd))
    } else {
      buf :+= seq → upd
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