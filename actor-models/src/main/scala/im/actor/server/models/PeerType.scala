package im.actor.server.models

sealed trait PeerType {
  def toInt: Int
}

object PeerType {
  @SerialVersionUID(1L)
  case object Private extends PeerType {
    val toInt = 1
  }

  @SerialVersionUID(1L)
  case object Group extends PeerType {
    val toInt = 2
  }

  def fromInt(id: Int) = id match {
    case 1 ⇒ Private
    case 2 ⇒ Group
  }
}
