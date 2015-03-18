package im.actor.server.models.contact

@SerialVersionUID(1L)
case class UserContact(ownerUserId: Int, contactUserId: Int, phoneNumber: Long, name: Option[String], accessSalt: String, isDeleted: Boolean)
