package im.actor.server

import scala.util.control.NoStackTrace

package object dialog {
  type AuthSidRandomId = (Int, Long)

  case object ReceiveFailed extends Exception with NoStackTrace

  case object ReadFailed extends Exception with NoStackTrace

  private[dialog] case object StopDialog
}
