package im.actor.server.model.contact

// TODO: future implementation
//Pending request
//Approved request
//Ignored request
//Block request
//No request
final case class ContactStatus(intValue: Int)
object ContactStatus {
  val Approved = ContactStatus(0)
  val Blocked = ContactStatus(1)
}