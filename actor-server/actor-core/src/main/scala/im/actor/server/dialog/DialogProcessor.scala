package im.actor.server.dialog

import akka.actor._
import akka.pattern.pipe
import akka.util.Timeout
import com.github.benmanes.caffeine.cache.Cache
import im.actor.api.rpc.misc.ApiExtension
import im.actor.concurrent.{ ActorFutures, AlertingActor }
import im.actor.serialization.ActorSerializer
import im.actor.server.cqrs.ProcessorState
import im.actor.server.db.DbExtension
import im.actor.server.group.GroupExtension
import im.actor.server.model.{ Dialog ⇒ DialogModel, PeerType, Peer }
import im.actor.server.persist.dialog.DialogRepo
import im.actor.server.persist.{ GroupRepo, UserRepo }
import im.actor.server.sequence.{ SeqUpdatesExtension, SeqStateDate }
import im.actor.server.social.SocialExtension
import im.actor.server.user.UserExtension
import im.actor.util.actors.StashingActorDebug
import im.actor.util.cache.CacheHelpers._
import org.joda.time.DateTime
import slick.dbio.DBIO
import slick.driver.PostgresDriver.api.Database

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

private[dialog] sealed trait DialogEvent

object DialogEvents {

  private[dialog] final case class LastMessageDate(date: Long) extends DialogEvent

  private[dialog] final case class LastReceiveDate(date: Long) extends DialogEvent

  private[dialog] final case class LastReadDate(date: Long) extends DialogEvent

  private[dialog] case object Shown extends DialogEvent

  private[dialog] case object Archived extends DialogEvent

  private[dialog] case object Favourited extends DialogEvent
  private[dialog] case object Unfavourited extends DialogEvent

}

private[dialog] object DialogState {
  def init(
    lastMessageDate: Long,
    lastReceiveDate: Long,
    lastReadDate:    Long,
    isFavourite:     Boolean,
    isCreated:       Boolean,
    isArchived:      Boolean
  ) = DialogState(lastMessageDate, lastReceiveDate, lastReadDate, isFavourite, isCreated, isArchived)

  def fromModel(model: DialogModel, isCreated: Boolean): DialogState =
    DialogState(
      model.lastMessageDate.getMillis,
      model.ownerLastReceivedAt.getMillis,
      model.ownerLastReadAt.getMillis,
      model.isFavourite,
      isCreated = isCreated,
      isArchived = model.archivedAt.isDefined
    )
}

private[dialog] final case class DialogState(
  lastMessageDate: Long,
  lastReceiveDate: Long,
  lastReadDate:    Long,
  isFavourite:     Boolean,
  isCreated:       Boolean,
  isArchived:      Boolean
) extends ProcessorState[DialogState, DialogEvent] {
  import DialogEvents._
  override def updated(e: DialogEvent): DialogState = e match {
    case LastMessageDate(date) if date > this.lastMessageDate ⇒ this.copy(lastMessageDate = date)
    case LastReceiveDate(date) if date > this.lastReceiveDate ⇒ this.copy(lastReceiveDate = date)
    case LastReadDate(date) if date > this.lastReadDate ⇒ this.copy(lastReadDate = date)
    case Shown ⇒ this.copy(isArchived = false)
    case Archived ⇒ this.copy(isArchived = true)
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
  extends AlertingActor
  with DialogCommandHandlers
  with ActorFutures
  with StashingActorDebug {
  import DialogCommands._
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

  override def preStart() = init()

  def receive = initializing

  def initializing: Receive = receiveStashing(
    replyTo ⇒ {
      case state: DialogState ⇒
        context become initialized(state)
        unstashAll()
      case Status.Failure(cause) ⇒
        log.error(cause, "Failed to init dialog")
        self ! Kill
    },
    debugMessage = debugMessage("initializing dialog")
  )

  def initialized(state: DialogState): Receive = actions(state) orElse reactions(state)

  // when receiving this messages, dialog reacts on other dialog's action
  def reactions(state: DialogState): Receive = {
    case sm: SendMessage if accepts(sm)       ⇒ ackSendMessage(state, sm) //User's message been sent
    case mrv: MessageReceived if accepts(mrv) ⇒ ackMessageReceived(mrv) //User's messages been received
    case mrd: MessageRead if accepts(mrd)     ⇒ ackMessageRead(mrd) //User's messages been read
    case sr: SetReaction if accepts(sr)       ⇒ ackSetReaction(sr)
    case rr: RemoveReaction if accepts(rr)    ⇒ ackRemoveReaction(rr)
    case uc: UpdateCounters                   ⇒ updateCountersChanged()
  }

  // when receiving this messages, dialog required to take action
  def actions(state: DialogState): Receive = {
    case sm: SendMessage if invokes(sm) ⇒ sendMessage(state, sm) //User sends message
    case mrv: MessageReceived if invokes(mrv) ⇒ messageReceived(state, mrv) //User received messages
    case mrd: MessageRead if invokes(mrd) ⇒ messageRead(state, mrd) //User reads messages
    case sr: SetReaction if invokes(sr) ⇒ setReaction(state, sr)
    case rr: RemoveReaction if invokes(rr) ⇒ removeReaction(state, rr)
    case Show(_) ⇒ show(state)
    case Archive(_) ⇒ archive(state)
    case Favourite(_) ⇒ favourite(state)
    case Unfavourite(_) ⇒ unfavourite(state)
    case Delete(_) ⇒ delete(state)
    case WriteMessage(_, _, date, randomId, message) ⇒ writeMessage(state, date, randomId, message)
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

  private def init(): Unit = {
    (for {
      optDialog ← db.run(DialogRepo.findDialog(userId, peer))
      initState ← optDialog match {
        case Some(dialog) ⇒ Future.successful(DialogState.fromModel(dialog, isCreated = true))
        case None         ⇒ Future.successful(DialogState.fromModel(DialogModel(userId, peer), isCreated = false))
      }
    } yield initState) pipeTo self
  }

  protected def withCreated(state: DialogState)(f: DialogState ⇒ Unit): Unit = {
    if (state.isCreated) f(state)
    else {
      log.debug("Creating dialog for userId: {}, peer: {}", userId, peer)
      val dialog = DialogModel.withLastMessageDate(userId, peer, new DateTime)
      val replyTo = sender()

      db.run(for {
        exists ← peer.`type` match {
          case PeerType.Private ⇒ UserRepo.find(peer.id) map (_.isDefined)
          case PeerType.Group   ⇒ GroupRepo.find(peer.id) map (_.isDefined)
          case unknown          ⇒ DBIO.failed(new RuntimeException(s"Unknown peer type $unknown"))
        }
        _ ← if (exists) DialogRepo.create(dialog) else DBIO.failed(new RuntimeException(s"Entity $peer does not exist"))
        _ ← DBIO.from(userExt.notifyDialogsChanged(userId))
      } yield DialogState.fromModel(dialog, isCreated = true)).to(self, replyTo)

      becomeStashing(
        replyTo ⇒ {
          case state: DialogState ⇒
            context become initialized(state)
            unstashAll()
            f(state)
          case Status.Failure(e) ⇒
            log.error(e, "Failed to create dialog")
            self ! Kill
        },
        debugMessage = debugMessage("creating dialog"),
        discardOld = true
      )
    }
  }

}
