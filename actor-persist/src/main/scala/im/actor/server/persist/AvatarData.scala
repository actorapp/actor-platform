package im.actor.server.persist

import im.actor.server.db.Db
import im.actor.server.models
import slick.driver.PostgresDriver.simple._
import Database.dynamicSession

class AvatarDataTable(tag: Tag) extends Table[models.AvatarData](tag, "avatar_datas") {
  def * = ???
}

object AvatarData {
  val table = TableQuery[AvatarDataTable]
}
