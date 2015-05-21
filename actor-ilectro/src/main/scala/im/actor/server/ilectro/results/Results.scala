package im.actor.server.ilectro.results

import java.util.UUID

import upickle.key

case class User(uuid: UUID, name: String)

case class Interest(
  name:                       String,
  id:                         Int,
  @key("parent_id") parentId: Int,
  @key("full_path") fullPath: String,
  level:                      Int
)

case class Banner(advertUrl: String, imageUrl: String)

case class Errors(errors: String, status: Option[Int] = None)

case class ReadyInterest(interest: Interest, children: List[ReadyInterest] = List())