package im.actor.server.model.social

final case class RelationStatus(intValue: Int)
object RelationStatus {
  val Approved = RelationStatus(0)
  val Blocked = RelationStatus(1)
}