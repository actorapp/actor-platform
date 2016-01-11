package im.actor.server.api.http.model

final case class DataEntity[A](data: A)

final case class DataEntities[A](data: Seq[A])