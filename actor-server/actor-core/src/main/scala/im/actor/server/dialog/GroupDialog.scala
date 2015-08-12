package im.actor.server.dialog

import akka.actor._
import akka.util.Timeout
import com.github.benmanes.caffeine.cache.Cache
import im.actor.api.rpc.peers.{ Peer, PeerType }
import im.actor.server.commons.serialization.ActorSerializer
import im.actor.server.db.DbExtension
import im.actor.server.group.GroupErrors.NotAMember
import im.actor.server.group.{ GroupExtension, GroupOffice, GroupProcessorRegion, GroupViewRegion }
import im.actor.server.push.SeqUpdatesExtension
import im.actor.server.sequence.SeqStateDate
import im.actor.server.user.{ UserExtension, UserProcessorRegion, UserViewRegion }
import im.actor.utils.cache.CacheHelpers._
import org.joda.time.DateTime
import slick.driver.PostgresDriver.api._

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

trait GroupDialogCommand {
  def groupId: Int
}

case class GroupDialogState(lastSenderId: Option[Int], lastReceiveDate: Option[Long], lastReadDate: Option[Long])

object GroupDialog {

  def register(): Unit = {
    ActorSerializer.register(13000, classOf[GroupDialogCommands])
    ActorSerializer.register(13001, classOf[GroupDialogCommands.SendMessage])
    ActorSerializer.register(13002, classOf[GroupDialogCommands.MessageReceived])
    ActorSerializer.register(13003, classOf[GroupDialogCommands.MessageReceivedAck])
    ActorSerializer.register(13004, classOf[GroupDialogCommands.MessageRead])
    ActorSerializer.register(13005, classOf[GroupDialogCommands.MessageReadAck])
  }

  type AuthIdRandomId = (Long, Long)

  private val MaxCacheSize = 100L

  private[dialog] case object MessageSentComplete extends Serializable

  def props: Props = Props(classOf[GroupDialog])
}

class GroupDialog
  extends Actor
  with ActorLogging
  with GroupDialogHandlers
  with Stash {

  import GroupDialog._
  import GroupDialogCommands._

  protected val groupId = self.path.name.toInt
  protected val groupPeer = Peer(PeerType.Group, groupId)

  protected implicit val system: ActorSystem = context.system
  protected implicit val ec: ExecutionContext = system.dispatcher

  protected implicit val db: Database = DbExtension(system).db
  protected implicit val seqUpdatesExt: SeqUpdatesExtension = SeqUpdatesExtension(system)
  protected implicit val groupViewRegion: GroupViewRegion = GroupExtension(system).viewRegion
  protected implicit val groupProcessorRegion: GroupProcessorRegion = GroupExtension(system).processorRegion
  protected implicit val userViewRegion: UserViewRegion = UserExtension(system).viewRegion
  protected implicit val userProcessorRegion: UserProcessorRegion = UserExtension(context.system).processorRegion
  protected implicit val peerRegion: GroupDialogRegion = GroupDialogExtension(system).region
  protected implicit val timeout = Timeout(5.seconds)

  protected implicit val sendResponseCache: Cache[AuthIdRandomId, Future[SeqStateDate]] =
    createCache[AuthIdRandomId, Future[SeqStateDate]](MaxCacheSize)

  override def receive: Receive = working(initState)

  private def initState: GroupDialogState = GroupDialogState(None, None, None)

  def working(state: GroupDialogState): Receive = {
    case SendMessage(_, senderUserId, senderAuthId, randomId, message, isFat) ⇒
      val replyTo = sender()
      withMemberIds(groupId) { (memberIds, _, botId) ⇒
        if ((memberIds contains senderUserId) || senderUserId == botId) {
          context.become {
            case MessageSentComplete ⇒
              unstashAll()
              context become receive
            case msg ⇒
              stash()
          }
          val date = new DateTime
          sendMessage(state, memberIds, botId, senderUserId, senderAuthId, randomId, date, message, isFat) onComplete {
            case Success(seqstatedate) ⇒
              replyTo ! seqstatedate
              self ! MessageSentComplete
            case Failure(e) ⇒
              replyTo ! Status.Failure(e)
              log.error(e, "Failed to send message")
              self ! MessageSentComplete
          }
        } else {
          replyTo ! Status.Failure(NotAMember)
        }
      }
    case MessageReceived(_, receiverUserId, _, date) ⇒
      val replyTo = sender()
      withMemberIds(groupId) { (memberIds, _, _) ⇒
        messageReceived(replyTo, state, memberIds, receiverUserId, date)
      }

    case MessageRead(_, readerUserId, readerAuthId, date) ⇒
      val replyTo = sender()
      withMemberIds(groupId) { (memberIds, invitedUserIds, _) ⇒
        messageRead(replyTo, state, memberIds, invitedUserIds, readerUserId, readerAuthId, date)
      }
  }

  private def withMemberIds(groupId: Int)(f: (Set[Int], Set[Int], Int) ⇒ Unit): Unit = {
    GroupOffice.getMemberIds(groupId) onComplete {
      case Success((memberIds, invitedUserIds, botId)) ⇒ f(memberIds.toSet, invitedUserIds.toSet, botId)
      case Failure(_)                                  ⇒
    }
  }

}
