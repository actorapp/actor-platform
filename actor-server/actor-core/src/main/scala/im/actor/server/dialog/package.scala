package im.actor.server

import scala.util.control.NoStackTrace

package object dialog {
  type AuthSidRandomId = (Int, Long)
  case object NotUniqueRandomId extends RuntimeException with NoStackTrace
}
