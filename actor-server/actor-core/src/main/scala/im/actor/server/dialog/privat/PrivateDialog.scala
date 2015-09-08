package im.actor.server.dialog.privat

import akka.actor._
import akka.contrib.pattern.ShardRegion
import akka.persistence.{ RecoveryCompleted, RecoveryFailure }
import akka.util.Timeout
import com.github.benmanes.caffeine.cache.Cache
import im.actor.serialization.ActorSerializer
import im.actor.server.db.DbExtension
import im.actor.server.dialog._
import im.actor.server.dialog.Origin.{ LEFT, RIGHT }
import im.actor.server.dialog.privat.PrivateDialogEvents.PrivateDialogEvent
import im.actor.server.office.{ ProcessorState, Processor }
import im.actor.server.sequence.SeqUpdatesExtension
import im.actor.server.sequence.SeqStateDate
import im.actor.server.social.SocialExtension
import im.actor.server.user.{ UserViewRegion, UserExtension }
import slick.driver.PostgresDriver.api.Database
import im.actor.util.cache.CacheHelpers._
import scala.concurrent.duration._

import scala.concurrent.{ Future, ExecutionContext }

private[dialog] trait PrivateDialogCommand {
  val dialogId: PrivateDialogId
}

case class DialogState(
  userId:          Int,
  peerId:          Int,
  lastMessageDate: Option[Long],
  lastReceiveDate: Option[Long],
  lastReadDate:    Option[Long]
)

object PrivateDialogEvents {
  private[dialog] sealed trait PrivateDialogEvent {
    def origin: Origin
  }
  private[dialog] case class LastMessageDate(date: Long, origin: Origin) extends PrivateDialogEvent
  private[dialog] case class LastReceiveDate(date: Long, origin: Origin) extends PrivateDialogEvent
  private[dialog] case class LastReadDate(date: Long, origin: Origin) extends PrivateDialogEvent
}

case class PrivateDialogState(private val state: Map[Origin, DialogState]) extends ProcessorState {
  def apply(origin: Origin): DialogState = state(origin)
  def updated(origin: Origin, dialogState: DialogState): PrivateDialogState = PrivateDialogState(state.updated(origin, dialogState))
}

object PrivateDialog {
  def register(): Unit = {
    ActorSerializer.register(30000, classOf[PrivateDialogCommands.SendMessage])
    ActorSerializer.register(30001, classOf[PrivateDialogCommands.MessageReceived])
    ActorSerializer.register(30002, classOf[PrivateDialogCommands.MessageReceivedAck])
    ActorSerializer.register(30003, classOf[PrivateDialogCommands.MessageRead])
    ActorSerializer.register(30004, classOf[PrivateDialogCommands.MessageReadAck])
  }

  val MaxCacheSize = 100L

  def props = Props(classOf[PrivateDialog])
}

class PrivateDialog extends Processor[PrivateDialogState, PrivateDialogEvent] with PrivateDialogHandlers {

  import PrivateDialog._
  import PrivateDialogCommands._
  import PrivateDialogEvents._

  val (left, right) = {
    val lr = self.path.name.toString split "_" map (_.toInt)
    (lr(1), lr(2))
  }

  private val initState: PrivateDialogState = PrivateDialogState(Map(
    LEFT → DialogState(left, right, None, None, None),
    RIGHT → DialogState(right, left, None, None, None)
  ))

  protected implicit val ec: ExecutionContext = context.dispatcher
  protected implicit val system: ActorSystem = context.system

  protected val db: Database = DbExtension(system).db
  protected implicit val seqUpdatesExt: SeqUpdatesExtension = SeqUpdatesExtension(system)
  protected implicit val userRegion = UserExtension(system).processorRegion
  protected implicit val userViewRegion: UserViewRegion = UserExtension(system).viewRegion
  protected implicit val socialRegion = SocialExtension(system).region
  protected implicit val timeout = Timeout(5.seconds)

  protected implicit val sendResponseCache: Cache[AuthIdRandomId, Future[SeqStateDate]] =
    createCache[AuthIdRandomId, Future[SeqStateDate]](MaxCacheSize)

  context.setReceiveTimeout(1.hours)

  override protected def updatedState(evt: PrivateDialogEvent, state: PrivateDialogState): PrivateDialogState = evt match {
    case LastMessageDate(date, origin) ⇒ state.updated(origin, state(origin).copy(lastMessageDate = Some(date)))
    case LastReceiveDate(date, origin) ⇒ state.updated(origin, state(origin).copy(lastReceiveDate = Some(date)))
    case LastReadDate(date, origin)    ⇒ state.updated(origin, state(origin).copy(lastReadDate = Some(date)))
  }

  override protected def handleQuery(state: PrivateDialogState): Receive = PartialFunction.empty[Any, Unit]

  override protected def handleInitCommand: Receive = working(initState)

  override protected def handleCommand(state: PrivateDialogState): Receive = {
    case SendMessage(id, senderUserId, senderAuthId, randomId, message, isFat) ⇒
      sendMessage(state, senderUserId, senderAuthId, randomId, message, isFat)
    case MessageReceived(id, receiverUserId, date) ⇒
      messageReceived(state, receiverUserId, date)
    case MessageRead(id, readerUserUd, readerAuthId, date) ⇒
      messageRead(state, readerUserUd, readerAuthId, date)
    case StopDialog     ⇒ context stop self
    case ReceiveTimeout ⇒ context.parent ! ShardRegion.Passivate(stopMessage = StopDialog)
  }

  private[this] var tmpDialogState: PrivateDialogState = initState
  override def receiveRecover = {
    case e: PrivateDialogEvent ⇒ tmpDialogState = updatedState(e, tmpDialogState)
    case RecoveryFailure(e) ⇒
      log.error(e, "Failed to recover")
    case RecoveryCompleted ⇒
      context become working(tmpDialogState)
    case unmatched ⇒
      log.error("Unmatched recovery event {}", unmatched)
  }

  override def persistenceId: String = self.path.parent.name + "_" + self.path.name
}
