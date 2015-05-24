package im.actor.server.persist.ilectro

import slick.driver.PostgresDriver.api._

import im.actor.server.models

class UserInterestTable(tag: Tag) extends Table[models.ilectro.UserInterest](tag, "ilectro_users_interests") {
  def userId = column[Int]("user_id", O.PrimaryKey)
  def interestId = column[Int]("interest_id", O.PrimaryKey)

  def * = (userId, interestId) <> (models.ilectro.UserInterest.tupled, models.ilectro.UserInterest.unapply)
}

object UserInterest {
  val interests = TableQuery[UserInterestTable]

  def createOrUpdate(userId: Int, interestId: Int) =
    interests.insertOrUpdate(models.ilectro.UserInterest(userId, interestId))

  def findIdsByUserId(userId: Int) =
    interests.filter(_.userId === userId).map(_.interestId).result

  def delete(userId: Int, interestId: Int) =
    interests.filter(ui ⇒ ui.interestId === interestId && ui.userId === userId).delete
}
