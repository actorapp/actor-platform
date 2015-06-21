package im.actor.server.models

@SerialVersionUID(1L)
case class AuthId(id: Long, userId: Option[Int], publicKeyHash: Option[Long])
