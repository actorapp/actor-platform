package im.actor.server.dialog

import akka.actor.{ Actor, ActorLogging, Stash }
import im.actor.server.misc.UpdateCounters

import scala.util.control.NoStackTrace

object Dialog {
  case object ReceiveFailed extends Exception with NoStackTrace

  case object ReadFailed extends Exception with NoStackTrace

}

trait Dialog extends Actor with Stash with ActorLogging with UpdateCounters {

  type State

  private[dialog] case class MessageSentComplete(state: State) extends Serializable

  type AuthIdRandomId = (Long, Long)

}
