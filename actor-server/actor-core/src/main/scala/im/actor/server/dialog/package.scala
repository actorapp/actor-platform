package im.actor.server

import scala.util.control.NoStackTrace

package object dialog {
  type AuthIdRandomId = (Long, Long)
  case object NotUniqueRandomId extends RuntimeException with NoStackTrace
}
