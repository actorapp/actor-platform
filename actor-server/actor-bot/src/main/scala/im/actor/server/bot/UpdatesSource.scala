package im.actor.server.bot

import akka.actor.{ Stash, ActorLogging, Props }
import akka.pattern.pipe
import akka.stream.actor.ActorPublisher
import akka.stream.scaladsl.Source
import im.actor.api.rpc.codecs._
import im.actor.api.rpc.messaging.{ ApiTextMessage, UpdateMessage }
import im.actor.api.rpc.sequence.SeqUpdate
import im.actor.bot.BotMessages
import im.actor.server.acl.ACLUtils
import im.actor.server.db.DbExtension
import im.actor.server.mtproto.protocol.UpdateBox
import im.actor.server.presences.{ GroupPresenceManager, PresenceManager }
import im.actor.server.sequence.{ UpdatesConsumer, WeakUpdatesManager }
import im.actor.server.user.UserExtension
import im.actor.server.persist

import scala.annotation.tailrec

private[bot] object UpdatesSource {
  import BotMessages._

  private case class Initialized(userId: Int)
  private case object AuthIdNotAuthorized

  def source(authId: Long) = Source.actorPublisher[BotUpdate](props(authId))

  def props(authId: Long) = Props(classOf[UpdatesSource], authId)

  private final case class Enqueue(upd: BotUpdate)
}

private class UpdatesSource(authId: Long) extends ActorPublisher[BotMessages.BotUpdate] with ActorLogging with Stash {

  import UpdatesSource._
  import BotMessages._
  import akka.stream.actor.ActorPublisherMessage._
  import context._
  import im.actor.server.sequence.NewUpdate

  private implicit val weakUpdatesManagerRegion = WeakUpdatesManager.startRegionProxy()
  private implicit val presenceManagerRegion = PresenceManager.startRegionProxy()
  private implicit val groupPresenceManagerRegion = GroupPresenceManager.startRegionProxy()
  private val userExt = UserExtension(system)
  private val db = DbExtension(system).db

  context.actorOf(UpdatesConsumer.props(authId, self), "updatesConsumer")

  private var buf = Vector.empty[BotMessages.BotUpdate]

  db.run(persist.AuthId.findUserId(authId)).map {
    case Some(userId) ⇒ Initialized(userId)
    case None         ⇒ AuthIdNotAuthorized
  }.pipeTo(self)

  def receive = {
    case Initialized(userId) ⇒
      context become working(userId)
    case AuthIdNotAuthorized ⇒
      val msg = "AuthId not authorized"
      log.error(msg)
      throw new RuntimeException(msg)
    case msg ⇒ stash()
  }

  def working(userId: Int): Receive = {
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
                      if (upd.senderUserId != userId) {
                        log.debug("Received message {}", message)
                        (for {
                          apiOutPeer ← ACLUtils.getOutPeer(upd.peer, authId)
                          senderAccessHash ← userExt.getAccessHash(upd.senderUserId, authId)
                        } yield Enqueue(TextMessage(
                          peer = OutPeer(apiOutPeer.`type`.id, apiOutPeer.id, apiOutPeer.accessHash),
                          sender = UserOutPeer(upd.senderUserId, senderAccessHash),
                          date = upd.date,
                          randomId = upd.randomId,
                          text = message
                        ))) pipeTo self
                      } else {
                        log.debug("Message from self, ignoring")
                      }
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