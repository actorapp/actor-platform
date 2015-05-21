package im.actor.server.persist

import slick.driver.PostgresDriver.api._

import im.actor.server.models

class InterestTable(tag: Tag) extends Table[models.Interest](tag, "interests") {
  def id = column[Int]("id", O.PrimaryKey)
  def name = column[String]("name")
  def parentId = column[Int]("parent_id")
  def fullPath = column[String]("full_path")
  def level = column[Int]("level")

  def * = (id, name, parentId, fullPath, level) <> (models.Interest.tupled, models.Interest.unapply)
}

object Interest {
  val interests = TableQuery[UserInterestTable]
}
