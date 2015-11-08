package im.actor.server.model

sealed trait MessageState {
  def toInt: Int
}

object MessageState {
  @SerialVersionUID(1L)
  case object Sent extends MessageState {
    def toInt = 1
  }

  @SerialVersionUID(1L)
  case object Received extends MessageState {
    def toInt = 2
  }

  @SerialVersionUID(1L)
  case object Read extends MessageState {
    def toInt = 3
  }

  def fromInt(i: Int): MessageState = i match {
    case 1 ⇒ Sent
    case 2 ⇒ Received
    case 3 ⇒ Read
  }
}
