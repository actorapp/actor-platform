package im.actor.server.dialog.group

import akka.actor._
import akka.cluster.sharding.ShardRegion
import akka.pattern.pipe
import akka.persistence.RecoveryCompleted
import com.github.benmanes.caffeine.cache.Cache
import im.actor.api.rpc.peers.{ ApiPeer, ApiPeerType }
import im.actor.concurrent.FutureExt
import im.actor.server.db.DbExtension
import im.actor.server.dialog._
import im.actor.server.dialog.group.GroupDialogEvents.GroupDialogEvent
import im.actor.server.group.GroupExtension
import im.actor.server.models.{ Peer, PeerType, Dialog }
import im.actor.server.office.ProcessorState
import im.actor.server.persist.DialogRepo
import im.actor.server.sequence.{ SeqStateDate, SeqUpdatesExtension }
import im.actor.server.user.UserExtension
import im.actor.util.cache.CacheHelpers._
import org.joda.time.DateTime
import slick.driver.PostgresDriver.api._

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

case class GroupDialogState(
  lastSenderId:    Option[Int],
  lastReceiveDate: Option[Long],
  lastReadDate:    Option[Long]
) extends ProcessorState

object GroupDialogEvents {
  private[dialog] sealed trait GroupDialogEvent
  private[dialog] case class LastSenderIdChanged(id: Int) extends GroupDialogEvent
  private[dialog] case class LastReceiveDateChanged(date: Long) extends GroupDialogEvent
  private[dialog] case class LastReadDateChanged(date: Long) extends GroupDialogEvent
}

object GroupDialog {
  val MaxCacheSize = 100L

  def props: Props = Props(classOf[GroupDialog])
}

private[group] final class GroupDialog extends DialogProcessor[GroupDialogState, GroupDialogEvent] with GroupDialogHandlers {

  import DialogCommands._
  import GroupDialogEvents._

  protected val groupId = (self.path.name.toString split "_")(1).toInt
  protected val groupPeer = ApiPeer(ApiPeerType.Group, groupId)

  private val initState = GroupDialogState(None, None, None)

  protected implicit val system: ActorSystem = context.system
  protected implicit val ec: ExecutionContext = system.dispatcher

  protected val db: Database = DbExtension(system).db
  protected implicit val seqUpdatesExt: SeqUpdatesExtension = SeqUpdatesExtension(system)
  protected lazy val groupExt = GroupExtension(system)
  protected lazy val userExt = UserExtension(system)

  protected val delivery = new ActorDelivery()

  context.setReceiveTimeout(1.hours)

  protected implicit val sendResponseCache: Cache[AuthIdRandomId, Future[SeqStateDate]] =
    createCache[AuthIdRandomId, Future[SeqStateDate]](GroupDialog.MaxCacheSize)

  override protected def updatedState(evt: GroupDialogEvent, state: GroupDialogState): GroupDialogState = evt match {
    case LastSenderIdChanged(senderUserId) ⇒ state.copy(lastSenderId = Some(senderUserId))
    case LastReceiveDateChanged(date)      ⇒ state.copy(lastReceiveDate = Some(date))
    case LastReadDateChanged(date)         ⇒ state.copy(lastReadDate = Some(date))
  }

  override protected def handleInitCommand: Receive = handleCommand(initState)

  override protected def handleCommand(state: GroupDialogState): Receive = {
    case SendMessage(_, senderUserId, senderAuthId, randomId, message, isFat) ⇒
      sendMessage(state, senderUserId, senderAuthId, randomId, message, isFat)
    case WriteMessage(_, senderUserId, date, randomId, message) ⇒
      writeMessage(state, senderUserId, date, randomId, message)
    case MessageReceived(_, receiverUserId, date) ⇒
      messageReceived(state, receiverUserId, date)
    case MessageRead(_, readerUserId, readerAuthId, date) ⇒
      messageRead(state, readerUserId, readerAuthId, date)
    case CreateForUser(_, userId) ⇒ createForUser(state, userId)
    case StopDialog               ⇒ context stop self
    case ReceiveTimeout           ⇒ context.parent ! ShardRegion.Passivate(stopMessage = StopDialog)
  }

  override protected def handleQuery(state: GroupDialogState): Receive = PartialFunction.empty[Any, Unit]

  override def receiveRecover = Actor.emptyBehavior

  override def persistenceId: String = s"dialog_${ApiPeerType.Group.id}"

}
