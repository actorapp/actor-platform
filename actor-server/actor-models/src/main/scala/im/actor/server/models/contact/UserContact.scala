package im.actor.server.models.contact

@SerialVersionUID(1L)
case class UserContact(ownerUserId: Int, contactUserId: Int, name: Option[String], accessSalt: String, isDeleted: Boolean)

@SerialVersionUID(1L)
case class UserPhoneContact(phoneNumber: Long, ownerUserId: Int, contactUserId: Int, name: Option[String], accessSalt: String, isDeleted: Boolean)

@SerialVersionUID(1L)
case class UserEmailContact(email: String, ownerUserId: Int, contactUserId: Int, name: Option[String], accessSalt: String, isDeleted: Boolean)