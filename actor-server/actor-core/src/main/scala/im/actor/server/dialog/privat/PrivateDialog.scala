package im.actor.server.dialog.privat

import akka.actor._
import akka.cluster.sharding.ShardRegion
import akka.pattern.pipe
import akka.persistence.RecoveryCompleted
import akka.util.Timeout
import com.github.benmanes.caffeine.cache.Cache
import im.actor.api.rpc.misc.ApiExtension
import im.actor.api.rpc.peers.ApiPeerType
import im.actor.server.db.DbExtension
import im.actor.server.dialog._
import im.actor.server.event.TSEvent
import im.actor.server.model.{ Dialog, Peer, PeerType }
import im.actor.server.office.ProcessorState
import im.actor.server.persist.DialogRepo
import im.actor.server.sequence.SeqStateDate
import im.actor.server.social.SocialExtension
import im.actor.server.user.UserExtension
import im.actor.util.cache.CacheHelpers._
import slick.dbio.DBIO
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

private[privat] final class PrivateDialog extends DialogProcessor[PrivateDialogState, TSEvent] with PrivateDialogHandlers {

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

  protected implicit val sendResponseCache: Cache[AuthSidRandomId, Future[SeqStateDate]] =
    createCache[AuthSidRandomId, Future[SeqStateDate]](MaxCacheSize)

  context.setReceiveTimeout(1.hours)

  override protected def updatedState(evt: TSEvent, state: PrivateDialogState): PrivateDialogState = evt match {
    case TSEvent(_, Created(s))                    ⇒ s
    case TSEvent(_, LastMessageDate(date, userId)) ⇒ state.updated(userId, state(userId).copy(lastMessageDate = Some(date)))
    case TSEvent(_, LastReceiveDate(date, userId)) ⇒ state.updated(userId, state(userId).copy(lastReceiveDate = Some(date)))
    case TSEvent(_, LastReadDate(date, userId))    ⇒ state.updated(userId, state(userId).copy(lastReadDate = Some(date)))
  }

  override protected def handleQuery(state: PrivateDialogState): Receive = Actor.emptyBehavior

  override protected def handleInitCommand: Receive = {
    case Extensions(leftExt, rightExt) ⇒
      leftDeliveryExt = dialogExt.getDeliveryExtension(leftExt)
      rightDeliveryExt = dialogExt.getDeliveryExtension(rightExt)
      val state = PrivateDialogState(Map(
        left → DialogState(right, None, None, None),
        right → DialogState(left, None, None, None)
      ))
      unstashAll()
      context become working(updatedState(TSEvent(now(), Created(state)), state))
    case msg ⇒
      log.debug("Stashing while initializing: {}", msg)
      stash()
  }

  override protected def handleCommand(state: PrivateDialogState): Receive = {
    case SendMessage(_, senderUserId, senderAuthSid, randomId, message, isFat) ⇒
      sendMessage(state, senderUserId, senderAuthSid, randomId, message, isFat)
    case WriteMessage(_, senderUserId, date, randomId, message) ⇒
      writeMessage(state, senderUserId, date, randomId, message)
    case MessageReceived(_, receiverUserId, date) ⇒
      messageReceived(state, receiverUserId, date)
    case MessageRead(_, readerUserUd, readerAuthSid, date) ⇒
      messageRead(state, readerUserUd, readerAuthSid, date)
    case StopDialog     ⇒ context stop self
    case ReceiveTimeout ⇒ context.parent ! ShardRegion.Passivate(stopMessage = StopDialog)
  }

  private def init(): Unit = {
    if (left == right) {
      val error = new RuntimeException(s"Attempt to create dialog with yourself: $left")
      log.error(error, "Failed to init dialog")
      throw error
    }

    val rightPeer = Peer(PeerType.Private, right)
    val leftPeer = Peer(PeerType.Private, left)

    val createModelFuture = db.run(for {
      leftDialogOpt ← DialogRepo.find(left, rightPeer)
      rightDialogOpt ← DialogRepo.find(right, leftPeer)
      _ ← leftDialogOpt match {
        case Some(_) ⇒ DBIO.successful(())
        case None ⇒
          for {
            _ ← DialogRepo.create(Dialog(left, rightPeer))
            _ ← DBIO.from(userExt.notifyDialogsChanged(left))
          } yield ()
      }
      _ ← rightDialogOpt match {
        case Some(_) ⇒ DBIO.successful(())
        case None ⇒
          for {
            _ ← DialogRepo.create(Dialog(right, leftPeer))
            _ ← DBIO.from(userExt.notifyDialogsChanged(right))
          } yield ()
      }
    } yield ())

    (for {
      _ ← createModelFuture
      l ← userExt.getUser(left)
      r ← userExt.getUser(right)
    } yield Extensions(l.internalExtensions, r.internalExtensions)) pipeTo self onFailure {
      case e ⇒
        log.error(e, "Failed to init dialog")
        self ! Kill
    }
  }

  override def receiveRecover = {
    case RecoveryCompleted ⇒
      init()
  }

  override def persistenceId: String = s"dialog_${ApiPeerType.Group.id}"

}
