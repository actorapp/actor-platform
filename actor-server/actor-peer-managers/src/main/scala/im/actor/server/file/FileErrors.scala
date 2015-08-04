package im.actor.server.file

import scala.util.control.NoStackTrace

object FileErrors {
  object LocationInvalid extends IllegalArgumentException with NoStackTrace
}
