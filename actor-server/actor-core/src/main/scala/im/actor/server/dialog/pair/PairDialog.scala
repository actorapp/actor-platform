package im.actor.server.dialog.pair

import akka.actor._
import akka.util.Timeout
import com.github.benmanes.caffeine.cache.Cache
import im.actor.server.commons.serialization.ActorSerializer
import im.actor.server.db.DbExtension
import im.actor.server.dialog.pair.PairDialog.MaxCacheSize
import im.actor.server.dialog.{ Dialog, PairDialogCommands }
import im.actor.server.push.SeqUpdatesExtension
import im.actor.server.sequence.SeqStateDate
import im.actor.server.social.SocialExtension
import im.actor.server.user.{ UserViewRegion, UserExtension }
import slick.driver.PostgresDriver.api.Database
import im.actor.utils.cache.CacheHelpers._
import scala.concurrent.duration._

import scala.concurrent.{ Future, ExecutionContext }

case class DialogPath(a: Int, b: Int) {
  require(a != b, "Private dialog with self is not allowed")

  override def toString =
    if (a > b) s"${b}_${a}" else s"${a}_${b}"
}

trait PairDialogCommand {
  def dialogPath: String
}

case class Direction(from: Int, to: Int)

case class DialogState(userId: Int, lastReceiveDate: Option[Long], lastReadDate: Option[Long])
case class PairDialogState(lastMessageDate: Option[Long], leftState: DialogState, rightState: DialogState)

object PairDialog {

  def register(): Unit = {
    ActorSerializer.register(13000, classOf[PairDialogCommands])
    ActorSerializer.register(13001, classOf[PairDialogCommands.SendMessage])
    ActorSerializer.register(13002, classOf[PairDialogCommands.MessageReceived])
    ActorSerializer.register(13003, classOf[PairDialogCommands.MessageReceivedAck])
    ActorSerializer.register(13004, classOf[PairDialogCommands.MessageRead])
    ActorSerializer.register(13005, classOf[PairDialogCommands.MessageReadAck])
  }

  val MaxCacheSize = 100L

  def props = Props(classOf[PairDialog])
}

class PairDialog extends Dialog with PairDialogHandlers {
  import PairDialogCommands._

  val (left, right) = {
    val lr = self.path.name.toString split "_" map (_.toInt)
    (lr(0), lr(1))
  }
  val toLeft = Direction(right, left)
  val toRight = Direction(left, right)

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

  override type State = PairDialogState

  override def receive: Receive = working(initState)

  def working(state: PairDialogState): Receive = {
    case SendMessage(_, senderUserId, senderAuthId, randomId, message, isFat) ⇒
      val direction = if (senderUserId == left) toRight else toLeft
      sendMessage(direction, state, senderAuthId, randomId, message, isFat)
    case MessageReceived(_, receiverUserId, date) ⇒
      val direction = if (receiverUserId == left) toLeft else toRight
      messageReceived(direction, state, date)
    case MessageRead(_, readerUserId, readerAuthId, date) ⇒
      val direction: Direction = if (readerUserId == left) toLeft else toRight
      messageRead(direction, state, readerAuthId, date)
  }

  private def initState: PairDialogState =
    PairDialogState(
      None,
      DialogState(left, None, None),
      DialogState(right, None, None)
    )
}
