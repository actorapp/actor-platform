package im.actor.server.dialog.privat

import akka.actor._
import akka.cluster.sharding.ShardRegion
import akka.persistence.RecoveryCompleted
import akka.util.Timeout
import com.github.benmanes.caffeine.cache.Cache
import im.actor.api.rpc.misc.ApiExtension
import im.actor.api.rpc.peers.ApiPeerType
import im.actor.server.db.DbExtension
import im.actor.server.dialog._
import im.actor.server.dialog.privat.PrivateDialogEvents.PrivateDialogEvent
import im.actor.server.office.ProcessorState
import im.actor.server.sequence.SeqStateDate
import im.actor.server.social.SocialExtension
import im.actor.server.user.UserExtension
import im.actor.util.cache.CacheHelpers._
import slick.driver.PostgresDriver.api.Database

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

case class DialogState(
  peerId:          Int,
  lastMessageDate: Option[Long],
  lastReceiveDate: Option[Long],
  lastReadDate:    Option[Long]
)

object PrivateDialogEvents {

  private[dialog] case class Extensions(leftExt: Seq[ApiExtension], rightExt: Seq[ApiExtension])

  private[dialog] sealed trait PrivateDialogEvent

  private[dialog] case class Created(state: PrivateDialogState) extends PrivateDialogEvent

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

private[privat] final class PrivateDialog extends DialogProcessor[PrivateDialogState, PrivateDialogEvent] with PrivateDialogHandlers {

  import DialogCommands._
  import PrivateDialog._
  import PrivateDialogEvents._

  val (left, right) = {
    val lr = self.path.name.toString split "_" map (_.toInt)
    (lr(1), lr(2))
  }

  protected implicit val ec: ExecutionContext = context.dispatcher
  protected implicit val system: ActorSystem = context.system

  protected val db: Database = DbExtension(system).db
  protected val userExt = UserExtension(system)
  protected val dialogExt = DialogExtension(system)
  protected implicit val socialRegion = SocialExtension(system).region
  protected implicit val timeout = Timeout(5.seconds)

  private[this] var leftDeliveryExt: DeliveryExtension = _
  private[this] var rightDeliveryExt: DeliveryExtension = _

  protected def deliveryExt(userId: Int): DeliveryExtension = userId match {
    case `left`  ⇒ leftDeliveryExt
    case `right` ⇒ rightDeliveryExt
    case _ ⇒
      val ex = new Exception(s"Unable to get delivery extension for user: ${userId} in dialog: ${self.path.name}")
      log.error(ex, "Wrong user in private dialog")
      throw ex
  }

  protected implicit val sendResponseCache: Cache[AuthIdRandomId, Future[SeqStateDate]] =
    createCache[AuthIdRandomId, Future[SeqStateDate]](MaxCacheSize)

  context.setReceiveTimeout(1.hours)

  override protected def updatedState(evt: PrivateDialogEvent, state: PrivateDialogState): PrivateDialogState = evt match {
    case Created(s)                    ⇒ s
    case LastMessageDate(date, userId) ⇒ state.updated(userId, state(userId).copy(lastMessageDate = Some(date)))
    case LastReceiveDate(date, userId) ⇒ state.updated(userId, state(userId).copy(lastReceiveDate = Some(date)))
    case LastReadDate(date, userId)    ⇒ state.updated(userId, state(userId).copy(lastReadDate = Some(date)))
  }

  override protected def handleQuery(state: PrivateDialogState): Receive = Actor.emptyBehavior

  override protected def handleInitCommand: Receive = {
    case msg ⇒
      log.debug("Stashing while initializing, message: {}", msg)
      stash()
      context become (waitForExtensions orElse stashingBehavior)

      val u1 = userExt.getUser(left)
      val u2 = userExt.getUser(right)
      val stateFuture = for (l ← u1; r ← u2) yield Extensions(l.internalExtensions, r.internalExtensions)

      stateFuture onComplete {
        case Success(exts) ⇒ self ! exts
        case Failure(e)    ⇒ throw new Exception("Failed to initialize dialog")
      }
  }

  private def waitForExtensions: Receive = {
    case Extensions(leftExt, rightExt) ⇒
      leftDeliveryExt = dialogExt.getDeliveryExtension(leftExt)
      rightDeliveryExt = dialogExt.getDeliveryExtension(rightExt)
      val state = PrivateDialogState(Map(
        left → DialogState(right, None, None, None),
        right → DialogState(left, None, None, None)
      ))
      unstashAndWork(Created(state), state)
  }

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

  private[this] var recoveryState: Option[PrivateDialogState] = None

  override def receiveRecover = {
    case created: Created      ⇒ recoveryState = Some(created.state)
    case e: PrivateDialogEvent ⇒ recoveryState = recoveryState map (updatedState(e, _))
    case RecoveryCompleted ⇒
      recoveryState match {
        case Some(dialogState) ⇒ context become working(dialogState)
        case None              ⇒ context become initializing
      }
    case unmatched ⇒
      log.error("Unmatched recovery event {}", unmatched)
  }

  override def persistenceId: String = s"dialog_${ApiPeerType.Group.id}"

}
