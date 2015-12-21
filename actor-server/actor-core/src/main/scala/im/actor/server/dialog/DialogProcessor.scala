package im.actor.server.dialog

import java.time.Instant

import akka.actor._
import akka.pattern.pipe
import akka.util.Timeout
import com.github.benmanes.caffeine.cache.Cache
import im.actor.api.rpc.misc.ApiExtension
import im.actor.concurrent.{ ActorFutures, ActorStashing }
import im.actor.serialization.ActorSerializer
import im.actor.server.cqrs.ProcessorState
import im.actor.server.db.DbExtension
import im.actor.server.model.{ Dialog ⇒ DialogModel, PeerType, Peer }
import im.actor.server.persist.{ HistoryMessageRepo, DialogRepo }
import im.actor.server.sequence.{ SeqUpdatesExtension, SeqStateDate }
import im.actor.server.social.SocialExtension
import im.actor.server.user.UserExtension
import im.actor.util.cache.CacheHelpers._
import org.joda.time.DateTime
import slick.dbio.DBIO

import slick.driver.PostgresDriver.api.Database

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

object DialogEvents {

  private[dialog] sealed trait DialogEvent

  private[dialog] final case class Initialized(
    lastMessageDate: Long,
    lastReceiveDate: Long,
    lastReadDate:    Long,
    isHidden:        Boolean,
    isFavourite:     Boolean,
    isOpen:          Boolean
  ) extends DialogEvent

  private[dialog] final case class LastMessageDate(date: Long) extends DialogEvent

  private[dialog] final case class LastReceiveDate(date: Long) extends DialogEvent

  private[dialog] final case class LastReadDate(date: Long) extends DialogEvent

  private[dialog] case object Shown extends DialogEvent
  private[dialog] case object Hidden extends DialogEvent

  private[dialog] case object Favourited extends DialogEvent
  private[dialog] case object Unfavourited extends DialogEvent

  // Event  which means there was a message sent to all dialog participant
  // Closed dialog means dialog with only ServiceMessage like ContactRegistered
  private[dialog] case object Open extends DialogEvent

}

private[dialog] object DialogState {
  def init(
    lastMessageDate: Long,
    lastReceiveDate: Long,
    lastReadDate:    Long,
    isHidden:        Boolean,
    isFavourite:     Boolean,
    isOpen:          Boolean
  ) = DialogState(lastMessageDate, lastReceiveDate, lastReadDate, isHidden, isFavourite, isOpen)
}

private[dialog] final case class DialogState(
  lastMessageDate: Long,
  lastReceiveDate: Long,
  lastReadDate:    Long,
  isHidden:        Boolean,
  isFavourite:     Boolean,
  isOpen:          Boolean
) extends ProcessorState[DialogState] {
  import DialogEvents._
  override def updated(e: AnyRef, ts: Instant): DialogState = e match {
    case LastMessageDate(date) if date > this.lastMessageDate ⇒ this.copy(lastMessageDate = date)
    case LastReceiveDate(date) if date > this.lastReceiveDate ⇒ this.copy(lastReceiveDate = date)
    case LastReadDate(date) if date > this.lastReadDate ⇒ this.copy(lastReadDate = date)
    case Shown ⇒ this.copy(isHidden = false)
    case Hidden ⇒ this.copy(isHidden = true)
    case Favourited ⇒ this.copy(isFavourite = true)
    case Unfavourited ⇒ this.copy(isFavourite = false)
    case Open ⇒ this.copy(isOpen = true)
    case unm ⇒ this
  }
}

object DialogProcessor {

  def register(): Unit = {
    ActorSerializer.register(
      40000 → classOf[DialogCommands.SendMessage],
      40001 → classOf[DialogCommands.MessageReceived],
      40002 → classOf[DialogCommands.MessageReceivedAck],
      40003 → classOf[DialogCommands.MessageRead],
      40004 → classOf[DialogCommands.MessageReadAck],
      40005 → classOf[DialogCommands.WriteMessage],
      40006 → classOf[DialogCommands.WriteMessageAck],
      40009 → classOf[DialogCommands.Envelope]
    )
  }

  val MaxCacheSize = 100L

  def props(userId: Int, peer: Peer, extensions: Seq[ApiExtension]): Props =
    Props(classOf[DialogProcessor], userId, peer, extensions)

}

private[dialog] final class DialogProcessor(val userId: Int, val peer: Peer, extensions: Seq[ApiExtension])
  extends Actor
  with ActorLogging
  with DialogCommandHandlers
  with ActorFutures
  with ActorStashing {
  import DialogProcessor._
  import DialogCommands._
  import DialogEvents._

  protected implicit val ec: ExecutionContext = context.dispatcher
  protected implicit val system: ActorSystem = context.system

  protected val db: Database = DbExtension(system).db
  protected val userExt = UserExtension(system)
  protected implicit val socialRegion = SocialExtension(system).region
  protected implicit val timeout = Timeout(5.seconds)

  protected val dialogExt = DialogExtension(system)
  protected val deliveryExt = dialogExt.getDeliveryExtension(extensions)
  protected val seqUpdExt = SeqUpdatesExtension(context.system)

  protected val selfPeer: Peer = Peer.privat(userId)

  protected implicit val sendResponseCache: Cache[AuthSidRandomId, Future[SeqStateDate]] =
    createCache[AuthSidRandomId, Future[SeqStateDate]](MaxCacheSize)

  init()

  override def receive: Receive = initializing

  def initializing: Receive = receiveStashing(replyTo ⇒ {
    case Initialized(lastMessageDate, lastReceiveDate, lastReadDate, isHidden, isFavourite, isOpen) ⇒
      context become initialized(DialogState.init(lastMessageDate, lastReceiveDate, lastReadDate, isHidden, isFavourite, isOpen))
      unstashAll()
    case Status.Failure(e) ⇒
      log.error(e, "Failed to init dialog")
      self ! Kill
  })

  def initialized(state: DialogState): Receive = {
    case sm: SendMessage if invokes(sm) ⇒ sendMessage(state, sm) //User sends message
    case sm: SendMessage if accepts(sm) ⇒ ackSendMessage(state, sm) //User's message been sent
    case mrv: MessageReceived if invokes(mrv) ⇒ messageReceived(state, mrv) //User received messages
    case mrv: MessageReceived if accepts(mrv) ⇒ ackMessageReceived(state, mrv) //User's messages been received
    case mrd: MessageRead if invokes(mrd) ⇒ messageRead(state, mrd) //User reads messages
    case mrd: MessageRead if accepts(mrd) ⇒ ackMessageRead(state, mrd) //User's messages been read
    case sr: SetReaction if invokes(sr) ⇒ setReaction(state, sr)
    case sr: SetReaction if accepts(sr) ⇒ ackSetReaction(state, sr)
    case rr: RemoveReaction if invokes(rr) ⇒ removeReaction(state, rr)
    case rr: RemoveReaction if accepts(rr) ⇒ ackRemoveReaction(state, rr)
    case WriteMessage(_, _, date, randomId, message) ⇒ writeMessage(date, randomId, message)
    case WriteMessageSelf(_, senderUserId, date, randomId, message) ⇒ writeMessageSelf(senderUserId, date, randomId, message)
    case Show(_) ⇒ show(state)
    case Hide(_) ⇒ hide(state)
    case Favourite(_) ⇒ favourite(state)
    case Unfavourite(_) ⇒ unfavourite(state)
    case Delete(_) ⇒ delete(state)
  }

  /**
   * dialog owner invokes `dc`
   * destination should be `peer` and origin should be `selfPeer`
   * private example: SendMessage(u1, u2) in Dialog(selfPeer = u1, peer = u2)
   * destination is u2(peer) and origin is u1(self)
   * group example: SendMessage(u1, g1) in Dialog(selfPeer = u1, peer = g1)
   * destination is g1(peer) and origin is u1(self)
   *
   * @param dc command
   * @return does dialog owner invokes this command
   */
  private def invokes(dc: DirectDialogCommand): Boolean = (dc.dest == peer) && (dc.origin == selfPeer)

  /**
   * dialog owner accepts `dc`
   * destination should be `selfPeer`(private case) or destination should be `peer` and origin in not `selfPeer`(group case)
   * private example: SendMessage(u1, u2) in Dialog(selfPeer = u2, peer = u1)
   * destination is u2(selfPeer)
   * group example: SendMessage(u1, g1) in Dialog(selfPeer = u2, peer = g1), where g1 is Group(members = [u1, u2])
   * destination is not u2(selfPeer), but  destination is g1(peer) and origin is not u2(selfPeer)
   *
   * @param dc command
   * @return does dialog owner accepts this command
   */
  def accepts(dc: DirectDialogCommand) = (dc.dest == selfPeer) || ((dc.dest == peer) && (dc.origin != selfPeer))

  private def init(): Unit =
    db.run(for {
      optDialog ← DialogRepo.find(userId, peer)
      dialog ← optDialog match {
        case Some(dialog) ⇒ DBIO.successful(dialog)
        case None ⇒
          log.debug("Creating dialog for userId: {}, peer: {}", userId, peer)
          val dialog = DialogModel.withLastMessageDate(userId, peer, new DateTime)
          for {
            _ ← DialogRepo.create(dialog)
            _ ← DBIO.from(userExt.notifyDialogsChanged(userId))
          } yield dialog
      }
      isOpen ← restoreIsOpen()
    } yield Initialized(
      dialog.lastMessageDate.getMillis,
      dialog.ownerLastReceivedAt.getMillis,
      dialog.ownerLastReadAt.getMillis,
      dialog.shownAt.isEmpty,
      dialog.isFavourite,
      isOpen
    )) pipeTo self

  private def restoreIsOpen(): DBIO[Boolean] =
    peer.typ match {
      case PeerType.Private ⇒
        HistoryMessageRepo.findNewest(peer.id, Peer.privat(userId)) map (_.isDefined)
      case _ ⇒ DBIO.successful(true)
    }
}
