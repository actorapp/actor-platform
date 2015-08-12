package im.actor.server.dialog.privat

import akka.actor._
import akka.contrib.pattern.ShardRegion
import akka.util.Timeout
import com.github.benmanes.caffeine.cache.Cache
import im.actor.server.commons.serialization.ActorSerializer
import im.actor.server.db.DbExtension
import im.actor.server.dialog.Dialog.StopDialog
import im.actor.server.dialog.PrivateDialogCommands.Origin
import im.actor.server.dialog.PrivateDialogCommands.Origin.{ LEFT, RIGHT }
import im.actor.server.dialog.privat.PrivateDialog.MaxCacheSize
import im.actor.server.dialog.{ Dialog, PrivateDialogCommands }
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

case class DialogState(userId: Int, peerId: Int, lastReceiveDate: Option[Long], lastReadDate: Option[Long])
case class PrivateDialogState(lastMessageDate: Option[Long], userState: Map[Origin, DialogState])

object PrivateDialog {
  private[dialog] sealed trait StateChange
  private[dialog] case class LastReceiveDate(date: Long) extends StateChange
  private[dialog] case class LastReadDate(date: Long) extends StateChange

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
}

class PrivateDialog extends Dialog with PrivateDialogHandlers {
  import PrivateDialogCommands._

  val (left, right) = {
    val lr = self.path.name.toString split "_" map (_.toInt)
    (lr(0), lr(1))
  }

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

  override type State = PrivateDialogState

  context.setReceiveTimeout(1.hours)

  override def receive: Receive = working(initState)

  def working(state: PrivateDialogState): Receive = {
    case SendMessage(_, _, origin, senderAuthId, randomId, message, isFat) ⇒
      val userState = state.userState(origin)
      sendMessage(userState, state, senderAuthId, randomId, message, isFat)
    case MessageReceived(_, _, origin, date) ⇒
      val userState = state.userState(origin)
      messageReceived(state, origin, date)
    case MessageRead(_, _, origin, readerAuthId, date) ⇒
      messageRead(state, origin, readerAuthId, date)
    case StopDialog     ⇒ context stop self
    case ReceiveTimeout ⇒ context.parent ! ShardRegion.Passivate(stopMessage = StopDialog)
  }

  private def initState: PrivateDialogState =
    PrivateDialogState(
      None,
      Map(
        LEFT → DialogState(left, right, None, None),
        RIGHT → DialogState(right, left, None, None)
      )
    )
}
