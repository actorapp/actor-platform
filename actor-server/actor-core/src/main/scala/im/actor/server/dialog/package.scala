package im.actor.server

import scala.util.control.NoStackTrace

package object dialog {
  type AuthIdRandomId = (Long, Long)

  case object ReceiveFailed extends Exception with NoStackTrace

  case object ReadFailed extends Exception with NoStackTrace

  private[dialog] case object StopDialog
}
