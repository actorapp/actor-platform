package im.actor.botkit

import akka.actor.{ Status, ActorRef, ActorLogging, Actor }
import akka.pattern.ask
import akka.util.Timeout
import im.actor.bots.BotMessages
import im.actor.bots.macros.BotInterface

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future
import scala.concurrent.forkjoin.ThreadLocalRandom

@BotInterface
private[botkit] abstract class BotBaseBase extends Actor with ActorLogging

abstract class BotBase extends BotBaseBase {
  import BotMessages._
  import context.dispatcher

  protected implicit val timeout: Timeout

  private var requestCounter = 0L
  private var requests = Map.empty[Long, (ActorRef, RequestBody)]
  private val users = TrieMap.empty[Int, User]
  private val groups = TrieMap.empty[Int, Group]
  private var rqSource = context.system.deadLetters

  protected def setRqSource(ref: ActorRef): Unit = {
    this.rqSource = ref
  }

  override final def request[T <: RequestBody](body: T): Future[body.Response] = {
    (self ? body) map (_.asInstanceOf[body.Response])
  }

  protected def onStreamFailure(cause: Throwable): Unit

  protected final def workingBehavior: Receive = {
    case Status.Failure(cause) ⇒
      onStreamFailure(cause)
    case rq: RequestBody ⇒
      requestCounter += 1
      val request = BotRequest(requestCounter, rq.service, rq)
      requests += (requestCounter → (sender() → rq))
      rqSource ! request
    case upd: BotUpdate ⇒
      log.info("Update: {}", upd)

      upd match {
        case BotFatSeqUpdate(_, _, newUsers, newGroups) ⇒
          newUsers foreach {
            case (id, user) ⇒ this.users.put(id, user)
          }

          newGroups foreach {
            case (id, group) ⇒ this.groups.put(id, group)
          }
        case _ ⇒
      }

      onUpdate(upd.body)
    case rsp: BotResponse ⇒
      log.info("Response #{}: {}", rsp.id, rsp.body)
      requests.get(rsp.id) foreach {
        case (replyTo, rq) ⇒

          val reply = rsp.body match {
            case err: BotError   ⇒ Status.Failure(err)
            case BotSuccess(obj) ⇒ rq.readResponse(obj)
          }
          replyTo ! reply
      }
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    val prefix = "Actor will restart."

    message match {
      case Some(msg) ⇒
        log.error(reason, prefix + " Last message received: {}", msg)
      case None ⇒
        log.error(reason, prefix)
    }

    super.preRestart(reason, message)
  }

  protected def nextRandomId() = ThreadLocalRandom.current().nextLong()

  protected def getUser(id: Int) = this.users.getOrElse(id, throw new RuntimeException(s"User $id not found"))

  protected def getGroup(id: Int) = this.groups.getOrElse(id, throw new RuntimeException(s"Group $id not found"))
}
