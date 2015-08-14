package im.actor.server.dialog.privat

import akka.actor._
import akka.contrib.pattern.ShardRegion
import akka.persistence.{ RecoveryCompleted, RecoveryFailure }
import akka.util.Timeout
import com.github.benmanes.caffeine.cache.Cache
import im.actor.server.commons.serialization.ActorSerializer
import im.actor.server.db.DbExtension
import im.actor.server.dialog.PrivateDialogCommands.Origin
import im.actor.server.dialog.PrivateDialogCommands.Origin.{ LEFT, RIGHT }
import im.actor.server.dialog.privat.PrivateDialogEvents.PrivateDialogEvent
import im.actor.server.dialog.{ StopDialog, AuthIdRandomId, PrivateDialogCommands }
import im.actor.server.office.{ ProcessorState, Processor }
import im.actor.server.push.SeqUpdatesExtension
import im.actor.server.sequence.SeqStateDate
import im.actor.server.social.SocialExtension
import im.actor.server.user.{ UserViewRegion, UserExtension }
import slick.driver.PostgresDriver.api.Database
import im.actor.utils.cache.CacheHelpers._
import scala.concurrent.duration._

import scala.concurrent.{ Future, ExecutionContext }

trait PrivateDialogCommand {
  require(right > left, "Left should be less than right")
  def left: Int
  def right: Int
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
  //def apply to allow syntax like val userState = state(origin)
  def get(origin: Origin): DialogState = state(origin)
  def updated(origin: Origin, dialogState: DialogState): PrivateDialogState = PrivateDialogState(state.updated(origin, dialogState))
}

object PrivateDialog {
  def register(): Unit = {
    ActorSerializer.register(13000, classOf[PrivateDialogCommands])
    ActorSerializer.register(13001, classOf[PrivateDialogCommands.SendMessage])
    ActorSerializer.register(13002, classOf[PrivateDialogCommands.MessageReceived])
    ActorSerializer.register(13003, classOf[PrivateDialogCommands.MessageReceivedAck])
    ActorSerializer.register(13004, classOf[PrivateDialogCommands.MessageRead])
    ActorSerializer.register(13005, classOf[PrivateDialogCommands.MessageReadAck])
  }

  val MaxCacheSize = 100L

  def props = Props(classOf[PrivateDialog])

  def persistenceIdFor(left: Int, right: Int): String = s"PrivateDialog-${left}_${right}"
}

class PrivateDialog extends Processor[PrivateDialogState, PrivateDialogEvent] with PrivateDialogHandlers {

  import PrivateDialog._
  import PrivateDialogCommands._
  import PrivateDialogEvents._

  val (left, right) = {
    val lr = self.path.name.toString split "_" map (_.toInt)
    (lr(0), lr(1))
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
  protected implicit val socilaRegion = SocialExtension(system).region
  protected implicit val timeout = Timeout(5.seconds)

  protected implicit val sendResponseCache: Cache[AuthIdRandomId, Future[SeqStateDate]] =
    createCache[AuthIdRandomId, Future[SeqStateDate]](MaxCacheSize)

  context.setReceiveTimeout(1.hours)

  override protected def updatedState(evt: PrivateDialogEvent, state: PrivateDialogState): PrivateDialogState = evt match {
    case LastMessageDate(date, origin) ⇒ state.updated(origin, state.get(origin).copy(lastMessageDate = Some(date)))
    case LastReceiveDate(date, origin) ⇒ state.updated(origin, state.get(origin).copy(lastReceiveDate = Some(date)))
    case LastReadDate(date, origin)    ⇒ state.updated(origin, state.get(origin).copy(lastReadDate = Some(date)))
  }

  override protected def handleQuery(state: PrivateDialogState): Receive = PartialFunction.empty[Any, Unit]

  override protected def handleInitCommand: Receive = working(initState)

  override protected def handleCommand(state: PrivateDialogState): Receive = {
    case SendMessage(_, _, origin, senderAuthId, randomId, message, isFat) ⇒
      sendMessage(state, origin, senderAuthId, randomId, message, isFat)
    case MessageReceived(_, _, origin, date) ⇒
      messageReceived(state, origin, date)
    case MessageRead(_, _, origin, readerAuthId, date) ⇒
      messageRead(state, origin, readerAuthId, date)
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

  override def persistenceId: String = PrivateDialog.persistenceIdFor(left, right)
}
