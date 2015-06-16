package im.actor.server.persist.llectro

import slick.driver.PostgresDriver.api._

import im.actor.server.models

class InterestTable(tag: Tag) extends Table[models.llectro.Interest](tag, "llectro_interests") {
  def id = column[Int]("id", O.PrimaryKey)
  def name = column[String]("name")
  def parentId = column[Int]("parent_id")
  def fullPath = column[String]("full_path")
  def level = column[Int]("level")

  def * = (id, name, parentId, fullPath, level) <> (models.llectro.Interest.tupled, models.llectro.Interest.unapply)
}

object Interest {
  val interests = TableQuery[InterestTable]

  def createOrUpdate(newInterests: Seq[models.llectro.Interest]) =
    DBIO.sequence(newInterests map { interest ⇒
      interests.insertOrUpdate(interest)
    })

  def find(level: Int, parentId: Int) =
    interests.filter(i ⇒ i.level === level && i.parentId === parentId).result
}
