package im.actor.server.model.contact

@SerialVersionUID(1L)
case class UserContact(
  ownerUserId:   Int,
  contactUserId: Int,
  name:          Option[String],
  isDeleted:     Boolean
)

@SerialVersionUID(1L)
case class UserPhoneContact(
  phoneNumber:   Long,
  ownerUserId:   Int,
  contactUserId: Int,
  name:          Option[String],
  isDeleted:     Boolean
)

@SerialVersionUID(1L)
case class UserEmailContact(
  email:         String,
  ownerUserId:   Int,
  contactUserId: Int,
  name:          Option[String],
  isDeleted:     Boolean
)