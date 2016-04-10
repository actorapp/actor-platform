package im.actor.server.dialog

import java.time.Instant

import akka.actor._
import akka.util.Timeout
import com.github.benmanes.caffeine.cache.Cache
import im.actor.api.rpc.misc.ApiExtension
import im.actor.concurrent.{ ActorFutures, AlertingActor, StashingActor }
import im.actor.serialization.ActorSerializer
import im.actor.server.cqrs._
import im.actor.server.db.DbExtension
import im.actor.server.group.GroupExtension
import im.actor.server.model.{ Peer, DialogObsolete ⇒ DialogModel }
import im.actor.server.sequence.{ SeqStateDate, SeqUpdatesExtension }
import im.actor.server.social.SocialExtension
import im.actor.server.user.UserExtension
import im.actor.util.cache.CacheHelpers._
import slick.driver.PostgresDriver.api.Database

import scala.collection.immutable
import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

private[dialog] trait DialogEvent extends TaggedEvent {
  override def tags: Set[String] = Set("dialog")
}

trait DialogQuery

object DialogEventsObsolete {

  private[dialog] final case class LastMessageDate(date: Long) extends DialogEvent

  private[dialog] final case class LastReceiveDate(date: Long) extends DialogEvent

  private[dialog] final case class LastReadDate(date: Long) extends DialogEvent
}

private[dialog] object DialogState {
  def init(
    lastMessageDate: Long,
    lastReceiveDate: Long,
    lastReadDate:    Long,
    counter:         Int
  ) = DialogState(lastMessageDate, lastReceiveDate, lastReadDate, counter, immutable.SortedSet.empty(UnreadMessage.ordering))

  def fromModel(model: DialogModel, isCreated: Boolean, counter: Int): DialogState =
    DialogState(
      model.lastMessageDate.getMillis,
      model.ownerLastReceivedAt.getMillis,
      model.ownerLastReadAt.getMillis,
      counter,
      immutable.SortedSet.empty(UnreadMessage.ordering)
    )
}

private object UnreadMessage {
  val ordering = new Ordering[UnreadMessage] {
    override def compare(x: UnreadMessage, y: UnreadMessage): Int =
      if (x.date.isBefore(y.date)) -1
      else if (x.date.isAfter(y.date)) 1
      else 0
  }
}

private case class UnreadMessage(date: Instant, randomId: Long) {
  override def hashCode(): Int = randomId.hashCode()

  override def equals(obj: scala.Any): Boolean = obj match {
    case um: UnreadMessage ⇒ randomId == um.randomId
    case _                 ⇒ false
  }
}

private[dialog] final case class DialogState(
  lastMessageDate: Long, //we don't use it now anywhere. should we remove it?
  lastReceiveDate: Long,
  lastReadDate:    Long,
  counter:         Int,
  unreadMessages:  immutable.SortedSet[UnreadMessage]
) extends ProcessorState[DialogState, DialogEvent] {
  import DialogEvents._
  import DialogEventsObsolete._
  override def updated(e: DialogEvent): DialogState = e match {
    case LastMessageDate(date) if date > this.lastMessageDate ⇒ this.copy(lastMessageDate = date)
    case LastReceiveDate(date) if date > this.lastReceiveDate ⇒ this.copy(lastReceiveDate = date)
    case LastReadDate(date) if date > this.lastReadDate       ⇒ this.copy(lastReadDate = date)
    case NewMessage(randomId, date, isIncoming) ⇒
      if (isIncoming) {
        this.copy(counter = counter + 1, unreadMessages = unreadMessages + UnreadMessage(date, randomId))
      } else this
    case MessagesRead(date) ⇒
      val newUnreadMessages = unreadMessages.dropWhile(um ⇒ um.date.isBefore(date) || um.date == date)
      this.copy(counter = newUnreadMessages.size, unreadMessages = newUnreadMessages)
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

  private[dialog] def persistenceId(peer: Peer) = s"Dialog_${peer.typ.index}_${peer.id}"
}

private[dialog] final class DialogProcessor(val userId: Int, val peer: Peer, extensions: Seq[ApiExtension])
  extends Processor[DialogState, DialogEvent]
  with AlertingActor
  with DialogCommandHandlers
  with ActorFutures
  with StashingActor {
  import DialogCommands._
  import DialogQueries._
  import DialogProcessor._

  protected implicit val ec: ExecutionContext = context.dispatcher
  protected implicit val system: ActorSystem = context.system

  protected val db: Database = DbExtension(system).db
  protected val userExt = UserExtension(system)
  protected val groupExt = GroupExtension(system)
  protected implicit val socialRegion = SocialExtension(system).region
  protected implicit val timeout = Timeout(5.seconds)

  protected val dialogExt = DialogExtension(system)
  protected val deliveryExt = dialogExt.getDeliveryExtension(extensions)
  protected val seqUpdExt = SeqUpdatesExtension(context.system)

  protected val selfPeer: Peer = Peer.privat(userId)

  protected implicit val sendResponseCache: Cache[AuthSidRandomId, Future[SeqStateDate]] =
    createCache[AuthSidRandomId, Future[SeqStateDate]](MaxCacheSize)

  override def persistenceId: String = DialogProcessor.persistenceId(peer)

  override protected def getInitialState: DialogState = DialogState(0, 0, 0, 0, immutable.SortedSet.empty(UnreadMessage.ordering))

  override protected def handleQuery: PartialFunction[Any, Future[Any]] = {
    case GetCounter() ⇒ Future.successful(GetCounterResponse(state.counter))
    case GetInfo()    ⇒ Future.successful(GetInfoResponse(DialogInfo(peer, state.counter, Instant.ofEpochMilli(state.lastMessageDate))))
  }

  override protected def handleCommand: Receive = actions(state) orElse reactions(state)

  // when receiving this messages, dialog reacts on other dialog's action
  def reactions(state: DialogState): Receive = {
    case sm: SendMessage if accepts(sm)       ⇒ ackSendMessage(state, sm) //User's message been sent
    case mrv: MessageReceived if accepts(mrv) ⇒ ackMessageReceived(mrv) //User's messages been received
    case mrd: MessageRead if accepts(mrd)     ⇒ ackMessageRead(mrd) //User's messages been read
    case sr: SetReaction if accepts(sr)       ⇒ ackSetReaction(sr)
    case rr: RemoveReaction if accepts(rr)    ⇒ ackRemoveReaction(rr)
    case uc: UpdateCounters                   ⇒ updateCountersChanged()
  }

  // when receiving this messages, dialog is required to take an action
  def actions(state: DialogState): Receive = {
    case sm: SendMessage if invokes(sm)                             ⇒ sendMessage(state, sm) //User sends message
    case mrv: MessageReceived if invokes(mrv)                       ⇒ messageReceived(state, mrv) //User received messages
    case mrd: MessageRead if invokes(mrd)                           ⇒ messageRead(state, mrd) //User reads messages
    case sr: SetReaction if invokes(sr)                             ⇒ setReaction(state, sr)
    case rr: RemoveReaction if invokes(rr)                          ⇒ removeReaction(state, rr)
    case WriteMessageSelf(_, senderUserId, date, randomId, message) ⇒ writeMessageSelf(state, senderUserId, date, randomId, message)
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
}
