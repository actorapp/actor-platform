package im.actor.server.model.social

@SerialVersionUID(1L)
case class Relation(userId: Int, relatedTo: Int, status: RelationStatus)
