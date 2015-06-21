package im.actor.server.models

@SerialVersionUID(2L)
case class UnregisteredContact(phoneNumber: Long, ownerUserId: Int, name: Option[String])
