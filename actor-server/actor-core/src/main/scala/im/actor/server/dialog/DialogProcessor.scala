package im.actor.server.dialog

import java.time.Instant

import akka.actor._
import akka.pattern.pipe
import akka.util.Timeout
import com.github.benmanes.caffeine.cache.Cache
import im.actor.api.rpc.messaging.ApiTextMessage
import im.actor.api.rpc.misc.ApiExtension
import im.actor.concurrent.{ ActorFutures, ActorStashing }
import im.actor.serialization.ActorSerializer
import im.actor.server.cqrs.ProcessorState
import im.actor.server.db.DbExtension
import im.actor.server.model.{ Dialog ⇒ DialogModel, PeerType, Peer }
import im.actor.server.persist.dialog.DialogRepo
import im.actor.server.persist.{ GroupRepo, UserRepo, HistoryMessageRepo }
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

  sealed trait InitState

  private[dialog] object Initialized {
    def fromDialog(dialog: DialogModel): Initialized =
      Initialized(
        dialog.lastMessageDate.getMillis,
        dialog.ownerLastReceivedAt.getMillis,
        dialog.ownerLastReadAt.getMillis,
        dialog.shownAt.isEmpty,
        dialog.isFavourite
      )
  }

  private[dialog] final case class Initialized(
    lastMessageDate: Long,
    lastReceiveDate: Long,
    lastReadDate:    Long,
    isHidden:        Boolean,
    isFavourite:     Boolean
  ) extends DialogEvent with InitState

  private[dialog] case object Uninitialized extends DialogEvent with InitState

  private[dialog] final case class LastMessageDate(date: Long) extends DialogEvent

  private[dialog] final case class LastReceiveDate(date: Long) extends DialogEvent

  private[dialog] final case class LastReadDate(date: Long) extends DialogEvent

  private[dialog] case object Shown extends DialogEvent
  private[dialog] case object Hidden extends DialogEvent

  private[dialog] case object Favourited extends DialogEvent
  private[dialog] case object Unfavourited extends DialogEvent

}

private[dialog] object DialogState {
  def init(
    lastMessageDate: Long,
    lastReceiveDate: Long,
    lastReadDate:    Long,
    isHidden:        Boolean,
    isFavourite:     Boolean
  ) = DialogState(lastMessageDate, lastReceiveDate, lastReadDate, isHidden, isFavourite)
}

private[dialog] final case class DialogState(
  lastMessageDate: Long, //we don't use it now anywhere. should we remove it?
  lastReceiveDate: Long,
  lastReadDate:    Long,
  isHidden:        Boolean,
  isFavourite:     Boolean
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
    case _ ⇒ this
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
  import DialogCommands._
  import DialogEvents._
  import DialogProcessor._

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

  override def receive: Receive = initializing

  def initializing: Receive = {
    case msg ⇒
      stash()
      becomeStashing(replyTo ⇒ ({
        case Initialized(lastMessageDate, lastReceiveDate, lastReadDate, isHidden, isFavourite) ⇒
          context become initialized(DialogState.init(lastMessageDate, lastReceiveDate, lastReadDate, isHidden, isFavourite))
          unstashAll()
        case Status.Failure(e) ⇒
          log.error(e, "Failed to init dialog")
          self ! Kill
      }: Receive) orElse reactions(isHidden = false) orElse dummyActions)

      (for {
        state ← initialState
        resultMessage ← state match {
          case init: Initialized ⇒ Future.successful(init)
          case Uninitialized ⇒
            if (isWriteOperation(msg)) {
              log.debug("Creating dialog for userId: {}, peer: {}", userId, peer)
              val dialog = DialogModel.withLastMessageDate(userId, peer, new DateTime)
              db.run(for {
                exists ← peer.`type` match {
                  case PeerType.Private ⇒ UserRepo.find(peer.id) map (_.isDefined)
                  case PeerType.Group   ⇒ GroupRepo.find(peer.id) map (_.isDefined)
                  case unknown          ⇒ DBIO.failed(new RuntimeException(s"Unknown peer type $unknown"))
                }
                _ ← if (exists) DialogRepo.create(dialog) else DBIO.failed(new RuntimeException(s"Entity ${peer} does not exist"))
                _ ← DBIO.from(userExt.notifyDialogsChanged(userId))
              } yield Initialized.fromDialog(dialog))
            } else {
              Future.successful(msg)
            }
        }
      } yield resultMessage).to(self, sender())
  }

  def initialized(state: DialogState): Receive = actions(state) orElse reactions(isHidden = state.isHidden)

  // when receiving this messages, dialog reacts on other dialog's action
  def reactions(isHidden: Boolean): Receive = {
    case sm: SendMessage if accepts(sm)       ⇒ ackSendMessage(isHidden, sm) //User's message been sent
    case mrv: MessageReceived if accepts(mrv) ⇒ ackMessageReceived(mrv) //User's messages been received
    case mrd: MessageRead if accepts(mrd)     ⇒ ackMessageRead(mrd) //User's messages been read
    case sr: SetReaction if accepts(sr)       ⇒ ackSetReaction(sr)
    case rr: RemoveReaction if accepts(rr)    ⇒ ackRemoveReaction(rr)
    case uc: UpdateCounters                   ⇒ updateCountersChanged()
  }

  def dummyActions: Receive = {
    case mrv: MessageReceived if invokes(mrv) ⇒ Future.successful(MessageReceivedAck()) pipeTo sender()
    case mrd: MessageRead if invokes(mrd)     ⇒ Future.successful(MessageReadAck()) pipeTo sender()
  }

  // when receiving this messages, dialog required to take action
  def actions(state: DialogState): Receive = {
    case sm: SendMessage if invokes(sm) ⇒ sendMessage(state, sm) //User sends message
    case mrv: MessageReceived if invokes(mrv) ⇒ messageReceived(state, mrv) //User received messages
    case mrd: MessageRead if invokes(mrd) ⇒ messageRead(state, mrd) //User reads messages
    case sr: SetReaction if invokes(sr) ⇒ setReaction(state, sr)
    case rr: RemoveReaction if invokes(rr) ⇒ removeReaction(state, rr)
    case Show(_) ⇒ show(state)
    case Hide(_) ⇒ hide(state)
    case Favourite(_) ⇒ favourite(state)
    case Unfavourite(_) ⇒ unfavourite(state)
    case Delete(_) ⇒ delete(state)
    case WriteMessage(_, _, date, randomId, message) ⇒ writeMessage(date, randomId, message)
    case WriteMessageSelf(_, senderUserId, date, randomId, message) ⇒ writeMessageSelf(senderUserId, date, randomId, message)
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
  private def accepts(dc: DirectDialogCommand) = (dc.dest == selfPeer) || ((dc.dest == peer) && (dc.origin != selfPeer))

  private def isWriteOperation: PartialFunction[Any, Boolean] = {
    case _: SendMessage | _: WriteMessage | _: WriteMessageSelf ⇒ true
    case _ ⇒ false
  }

  private def initialState: Future[InitState] =
    for {
      optDialog ← db.run(DialogRepo.findDialog(userId, peer))
      initState ← optDialog match {
        case Some(dialog) ⇒ Future.successful(Initialized.fromDialog(dialog))
        case None         ⇒ Future.successful(Uninitialized)
      }
    } yield initState

}
