package im.actor.server.dialog.privat

import akka.actor._
import akka.contrib.pattern.ShardRegion
import akka.persistence.{ RecoveryCompleted, RecoveryFailure }
import akka.util.Timeout
import com.github.benmanes.caffeine.cache.Cache
import im.actor.server.db.DbExtension
import im.actor.server.dialog._
import im.actor.server.dialog.privat.PrivateDialogEvents.PrivateDialogEvent
import im.actor.server.office.ProcessorState
import im.actor.server.sequence.{ SeqStateDate, SeqUpdatesExtension }
import im.actor.server.social.SocialExtension
import im.actor.server.user.{ UserExtension, UserViewRegion }
import im.actor.util.cache.CacheHelpers._
import slick.driver.PostgresDriver.api.Database

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

case class DialogState(
  peerId:          Int,
  lastMessageDate: Option[Long],
  lastReceiveDate: Option[Long],
  lastReadDate:    Option[Long]
)

object PrivateDialogEvents {
  private[dialog] sealed trait PrivateDialogEvent {
    def userId: Int
  }
  private[dialog] case class LastMessageDate(date: Long, userId: Int) extends PrivateDialogEvent
  private[dialog] case class LastReceiveDate(date: Long, userId: Int) extends PrivateDialogEvent
  private[dialog] case class LastReadDate(date: Long, userId: Int) extends PrivateDialogEvent
}

case class PrivateDialogState(private val state: Map[Int, DialogState]) extends ProcessorState {
  def apply(userId: Int): DialogState = state(userId)
  def updated(userId: Int, dialogState: DialogState): PrivateDialogState = PrivateDialogState(state.updated(userId, dialogState))
}

object PrivateDialog {
  val MaxCacheSize = 100L

  def props = Props(classOf[PrivateDialog])
}

class PrivateDialog extends DialogProcessor[PrivateDialogState, PrivateDialogEvent] with PrivateDialogHandlers {

  import DialogCommands._
  import PrivateDialog._
  import PrivateDialogEvents._

  val (left, right) = {
    val lr = self.path.name.toString split "_" map (_.toInt)
    (lr(1), lr(2))
  }

  private def initState: PrivateDialogState = PrivateDialogState(Map(
    left → DialogState(right, None, None, None),
    right → DialogState(left, None, None, None)
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
    case LastMessageDate(date, userId) ⇒ state.updated(userId, state(userId).copy(lastMessageDate = Some(date)))
    case LastReceiveDate(date, userId) ⇒ state.updated(userId, state(userId).copy(lastReceiveDate = Some(date)))
    case LastReadDate(date, userId)    ⇒ state.updated(userId, state(userId).copy(lastReadDate = Some(date)))
  }

  override protected def handleQuery(state: PrivateDialogState): Receive = PartialFunction.empty[Any, Unit]

  override protected def handleInitCommand: Receive = working(initState)

  override protected def handleCommand(state: PrivateDialogState): Receive = {
    case SendMessage(_, senderUserId, senderAuthId, randomId, message, isFat) ⇒
      sendMessage(state, senderUserId, senderAuthId, randomId, message, isFat)
    case MessageReceived(_, receiverUserId, date) ⇒
      messageReceived(state, receiverUserId, date)
    case MessageRead(_, readerUserUd, readerAuthId, date) ⇒
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
