package im.actor.server.dialog

import java.time.Instant

import akka.actor._
import akka.http.scaladsl.util.FastFuture
import akka.persistence.SnapshotMetadata
import akka.util.Timeout
import com.github.benmanes.caffeine.cache.Cache
import im.actor.api.rpc.misc.ApiExtension
import im.actor.concurrent.{ ActorFutures, AlertingActor, StashingActor }
import im.actor.serialization.ActorSerializer
import im.actor.server.cqrs._
import im.actor.server.db.DbExtension
import im.actor.server.group.GroupExtension
import im.actor.server.model.Peer
import im.actor.server.sequence.{ SeqStateDate, SeqUpdatesExtension }
import im.actor.server.social.SocialExtension
import im.actor.server.user.UserExtension
import im.actor.util.cache.CacheHelpers._
import slick.driver.PostgresDriver.api.Database

import scala.collection.SortedSet
import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

private[dialog] trait DialogEvent extends TaggedEvent {
  override def tags: Set[String] = Set("dialog")
}

trait DialogQuery {
  val dest: Option[Peer]

  def getDest: Peer
}

private object UnreadMessage {
  val OrderingAsc = new Ordering[UnreadMessage] {
    override def compare(x: UnreadMessage, y: UnreadMessage): Int =
      if (x.randomId == y.randomId) 0
      else if (x.date.isBefore(y.date)) -1
      else if (x.date.isAfter(y.date)) 1
      else 0
  }
}

private case class UnreadMessage(date: Instant, randomId: Long)

private[dialog] object DialogState {
  def initial(userId: Int) = DialogState(
    userId = userId,
    lastMessageDate = Instant.ofEpochMilli(0),
    lastReceiveDate = Instant.ofEpochMilli(0),
    lastReadDate = Instant.ofEpochMilli(0),
    counter = 0,
    unreadMessages = SortedSet.empty(UnreadMessage.OrderingAsc),
    unreadMessagesMap = Map.empty
  )
}

private[dialog] final case class DialogState(
  userId:            Int,
  lastMessageDate:   Instant, //we don't use it now anywhere. should we remove it?
  lastReceiveDate:   Instant,
  lastReadDate:      Instant,
  counter:           Int,
  unreadMessages:    SortedSet[UnreadMessage],
  unreadMessagesMap: Map[Long, Long]
) extends ProcessorState[DialogState] {
  import DialogEvents._

  override def updated(e: Event): DialogState = e match {
    case NewMessage(randomId, date, senderUserId, messageHeader) ⇒
      if (senderUserId != userId) {
        this.copy(
          counter = counter + 1,
          unreadMessages = unreadMessages + UnreadMessage(date, randomId),
          unreadMessagesMap = unreadMessagesMap + (randomId → date.toEpochMilli),
          lastMessageDate = date
        )
      } else this.copy(lastMessageDate = date)
    case MessagesRead(date, readerUserId) if readerUserId == userId ⇒
      val readMessages = unreadMessages.takeWhile(um ⇒ um.date.isBefore(date) || um.date == date)
      val newUnreadMessages = unreadMessages -- readMessages
      val newUnreadMessagesMap = unreadMessagesMap -- readMessages.map(_.randomId)

      this.copy(
        counter = newUnreadMessages.size,
        unreadMessages = newUnreadMessages,
        unreadMessagesMap = newUnreadMessagesMap
      )
    case MessagesRead(date, readerUserId) if readerUserId != userId && date.isAfter(lastReadDate) ⇒
      this.copy(lastReadDate = date)
    case MessagesReceived(date) if date.isAfter(lastReceiveDate) ⇒ this.copy(lastReceiveDate = date)
    case CounterReset() ⇒
      this.copy(counter = 0, unreadMessages = SortedSet.empty(UnreadMessage.OrderingAsc), unreadMessagesMap = Map.empty)
  }

  override def withSnapshot(metadata: SnapshotMetadata, snapshot: Any): DialogState = snapshot match {
    case s: DialogStateSnapshot ⇒
      copy(
        userId = s.userId,
        lastMessageDate = s.lastMessageDate,
        lastReceiveDate = s.lastReceiveDate,
        lastReadDate = s.lastReadDate,
        counter = s.counter,
        unreadMessages = SortedSet(
          (s.unreadMessages.toSeq map {
            case (randomId, ts) ⇒ UnreadMessage(Instant.ofEpochMilli(ts), randomId)
          }): _*
        )(UnreadMessage.OrderingAsc),
        unreadMessagesMap = s.unreadMessages
      )
  }

  override lazy val snapshot = DialogStateSnapshot(
    userId = userId,
    lastMessageDate = lastMessageDate,
    lastReceiveDate = lastReceiveDate,
    lastReadDate = lastReadDate,
    counter = counter,
    unreadMessages = unreadMessagesMap
  )
}

object DialogProcessor {

  def register(): Unit = {
    ActorSerializer.register(
      40010 → classOf[DialogEvents.MessagesRead],
      40011 → classOf[DialogEvents.MessagesReceived],
      40012 → classOf[DialogEvents.NewMessage],
      40013 → classOf[DialogEvents.CounterReset],
      40014 → classOf[DialogStateSnapshot]
    )
  }

  val MaxCacheSize = 100L

  def props(userId: Int, peer: Peer, extensions: Seq[ApiExtension]): Props =
    Props(classOf[DialogProcessor], userId, peer, extensions)

  private[dialog] def persistenceId(userId: Int, peer: Peer) = s"Dialog_${userId}_${peer.typ.index}_${peer.id}"
}

private[dialog] final class DialogProcessor(val userId: Int, val peer: Peer, extensions: Seq[ApiExtension])
  extends Processor[DialogState]
  with IncrementalSnapshots[DialogState]
  with AlertingActor
  with DialogCommandHandlers
  with ActorFutures
  with StashingActor {
  import DialogCommands._
  import DialogQueries._
  import DialogProcessor._

  protected implicit val ec: ExecutionContext = context.dispatcher
  protected implicit val system: ActorSystem = context.system

  protected implicit val timeout = Timeout(5.seconds)

  protected val db: Database = DbExtension(system).db
  protected val userExt = UserExtension(system)
  protected val groupExt = GroupExtension(system)
  protected implicit val socialRegion = SocialExtension(system).region
  protected val dialogExt = DialogExtension(system)
  protected val deliveryExt = dialogExt.getDeliveryExtension(extensions)
  protected val seqUpdExt = SeqUpdatesExtension(context.system)

  protected val selfPeer: Peer = Peer.privat(userId)

  protected implicit val sendResponseCache: Cache[AuthSidRandomId, Future[SeqStateDate]] =
    createCache[AuthSidRandomId, Future[SeqStateDate]](MaxCacheSize)

  override def persistenceId: String = DialogProcessor.persistenceId(userId, peer)

  override protected def getInitialState: DialogState = DialogState.initial(userId)

  override protected def saveSnapshotIfNeeded(): Unit = {
    super.saveSnapshotIfNeeded()

  }

  override protected def handleQuery: PartialFunction[Any, Future[Any]] = {
    case GetCounter(_) ⇒ getCounter() map (GetCounterResponse(_))
    case GetInfo(_) ⇒ getCounter() map { counter ⇒
      GetInfoResponse(Some(DialogInfo(
        peer = Some(peer),
        counter = counter,
        date = state.lastMessageDate,
        lastMessageDate = state.lastMessageDate,
        lastReceivedDate = state.lastReceiveDate,
        lastReadDate = state.lastReadDate
      )))
    }
  }

  override protected def handleCommand: Receive = actions(state) orElse reactions(state)

  // when receiving this messages, dialog reacts on other dialog's action
  def reactions(state: DialogState): Receive = {
    case sm: SendMessage if accepts(sm)       ⇒ ackSendMessage(state, sm) // User's message been sent
    case mrv: MessageReceived if accepts(mrv) ⇒ ackMessageReceived(mrv) // User's messages been received
    case mrd: MessageRead if accepts(mrd)     ⇒ ackMessageRead(mrd) // User's messages been read
    case sr: SetReaction if accepts(sr)       ⇒ ackSetReaction(sr)
    case rr: RemoveReaction if accepts(rr)    ⇒ ackRemoveReaction(rr)
  }

  // when receiving this messages, dialog is required to take an action
  def actions(state: DialogState): Receive = {
    case sm: SendMessage if invokes(sm)                             ⇒ sendMessage(state, sm) // User sends message
    case mrv: MessageReceived if invokes(mrv)                       ⇒ messageReceived(state, mrv) // User received messages
    case mrd: MessageRead if invokes(mrd)                           ⇒ messageRead(state, mrd) // User reads messages
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
  private def invokes(dc: DirectDialogCommand): Boolean = (dc.getDest == peer) && (dc.getOrigin == selfPeer)

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
  private def accepts(dc: DirectDialogCommand) = (dc.getDest == selfPeer) || ((dc.getDest == peer) && (dc.getOrigin != selfPeer))

  private def getCounter(): Future[Int] = {
    if (peer.typ.isGroup)
      groupExt.isMember(peer.id, userId) map {
        case true  ⇒ state.counter
        case false ⇒ 0
      }
    else FastFuture.successful(state.counter)
  }
}
